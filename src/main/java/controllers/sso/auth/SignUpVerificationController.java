package controllers.sso.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.web.UrlBuilder;
import models.sso.User;
import models.sso.UserConfirmationState;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;

/**
 * Sign up verification controller. Show page with fields to verify the user created by previous sign up step.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class
})
public class SignUpVerificationController {

    /**
     * Controller template to render sign up verification page.
     */
    static final String TEMPLATE = "views/sso/auth/signUpVerification.ftl.html";

    /**
     * Expirable token encryptor.
     */
    final ExpirableTokenEncryptor expirableTokenEncryptor;

    /**
     * User service.
     */
    final UserService userService;

    /**
     * URL builder provider for controller. Instance per request.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Constructs verification controller.
     *
     * @param expirableTokenEncryptor Expirable token encryptor.
     * @param userService User service.
     * @param urlBuilderProvider URL builder provider.
     * @param properties Properties.
     * @param logger Logger.
     */
    @Inject
    public SignUpVerificationController(ExpirableTokenEncryptor expirableTokenEncryptor,
                                        UserService userService,
                                        Provider<UrlBuilder> urlBuilderProvider,
                                        NinjaProperties properties,
                                        Logger logger) {
        this.expirableTokenEncryptor = expirableTokenEncryptor;
        this.userService = userService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.properties = properties;
        this.logger = logger;
    }

    /**
     * Shows Sign Up verification page after successful user sign up and confirmation email/SMS sent. GET.
     *
     * @param context Context.
     * @return Rendered sign up page.
     */
    @Transactional
    public Result signUpVerificationGet(@Param("token") String tokenAsString, Context context) {
        return signUpVerification(tokenAsString, context);
    }

    /**
     * Submits Sign Up verification page. POST.
     *
     * @param context Context.
     * @return Rendered sign up page.
     */
    @Transactional
    public Result signUpVerification(@Param("token") String tokenAsString, Context context) {
        String redirectUrl = urlBuilderProvider.get().getContinueUrlParameter();
        String errorType = null;
        try {
            ExpirableToken verificationToken = expirableTokenEncryptor.decrypt(tokenAsString);
            if (!ExpirableTokenType.SIGNUP_VERIFICATION.equals(verificationToken.getType())) {
                throw new IllegalTokenException();
            }
            String verificationCodeFromUser = context.getParameter("verificationCode");
            if ("post".equalsIgnoreCase(context.getMethod())
                    && verificationToken.getAttributeValue("verificationCode").equals(verificationCodeFromUser)) {
                Long userId = verificationToken.getAttributeAsLong("userId");
                User userForVerification = userService.get(userId);
                userForVerification.setConfirmationState(UserConfirmationState.CONFIRMED);
                userService.save(userForVerification);
                return Results.redirect(redirectUrl);
            }
        } catch (ExpiredTokenException ete) {
            errorType = "tokenExpired";
        } catch (IllegalTokenException ite) {
            errorType = "wrongToken";
        }

        Result result = Results.html().template(TEMPLATE);
        result.render(errorType, true);
        result.render("config", properties);
        result.render("redirectUrl", redirectUrl);

        return result;
    }

    /**
     * Confirms user email: this action is invoked when the user clicks link in email.
     *
     * @param tokenAsString Code (encrypted user id).
     * @param context Context.
     * @return Result.
     */
    @Transactional
    public Result confirmEmail(@Param("token") String tokenAsString, Context context) {
        try {
            ExpirableToken emailConfirmationToken = expirableTokenEncryptor.decrypt(tokenAsString);
            Long userId = emailConfirmationToken.getAttributeAsLong("userId");
            String email = emailConfirmationToken.getAttributeValue("email", "");

            User user = userService.get(userId);
            if (user != null) {
                if (email.equals(user.getEmail())) {
                    if (!user.isConfirmed()) {
                        user.confirm();
                        userService.update(user);
                    }
                    return Results.redirect(urlBuilderProvider.get().getContinueUrlParameter());
                } else {
                    logger.warn("Email confirmaiton: user {} has different email {}, {} expected.",
                            userId, user.getEmail(), email);
                }
            } else {
                logger.warn("Email confirmaiton: user {} was not found.", userId);
            }
        } catch (NumberFormatException | ExpiredTokenException | IllegalTokenException ex) {
            logger.warn("Unable to confirm user by email token: " + tokenAsString, ex);
        }
        return Results.redirect(urlBuilderProvider.get().getSignInUrl());
    }
}
