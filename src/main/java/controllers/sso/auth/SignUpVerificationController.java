package controllers.sso.auth;

import com.google.inject.persist.Transactional;
import controllers.annotations.SecureHtmlHeaders;
import controllers.sso.auth.state.SignInState;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireUnauthenticatedUserFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import models.sso.User;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.metrics.Timed;
import ninja.params.Param;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import services.sso.UserService;
import services.sso.limits.GenericCounterService;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;


/**
 * Sign up verification controller. Show page with fields to verify the user created by previous sign up step.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class,
        AuthenticationFilter.class,
        RequireUnauthenticatedUserFilter.class
})
public class SignUpVerificationController {

    /**
     * Controller template to render sign up verification page.
     */
    private static final String TEMPLATE = "views/sso/auth/signUpVerification.ftl.html";

    /**
     * Expirable token encryptor.
     */
    private final ExpirableTokenEncryptor expirableTokenEncryptor;

    /**
     * User service.
     */
    private final UserService userService;

    /**
     * URL builder provider for controller. Instance per request.
     */
    private final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * HTML result with secure headers.
     */
    private final Provider<Result> htmlWithSecureHeadersProvider;

    /**
     * Counter service.
     */
    private final GenericCounterService counterService;

    /**
     * Application properties.
     */
    private final NinjaProperties properties;

    /**
     * Logger.
     */
    private final Logger logger;

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
                                        @SecureHtmlHeaders Provider<Result> htmlWithSecureHeadersProvider,
                                        GenericCounterService counterService,
                                        NinjaProperties properties,
                                        Logger logger) {
        this.expirableTokenEncryptor = expirableTokenEncryptor;
        this.userService = userService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.htmlWithSecureHeadersProvider = htmlWithSecureHeadersProvider;
        this.counterService = counterService;
        this.properties = properties;
        this.logger = logger;
    }

    /**
     * Shows Sign Up verification page after successful user sign up and confirmation email/SMS sent. GET method.
     *
     * @param context Context.
     * @return Rendered sign up page.
     */
    @Timed
    @Transactional
    public Result verifySignUpGet(@Param("token") String tokenAsString, Context context) {
        return verifySignUp(tokenAsString, context);
    }

    /**
     * Verifies Sign Up with user provided code. Redirects to Sign In page. POST method.
     *
     * @param context Context.
     * @return Rendered sign up page.
     */
    @Timed
    @Transactional
    public Result verifySignUp(@Param("token") String tokenAsString, Context context) {
        String continueUrl = urlBuilderProvider.get().getContinueUrlParameter();
        String errorType = null;
        try {
            if (counterService.checkLimit(tokenAsString)) {
                throw new ExpiredTokenException();
            }
            ExpirableToken verificationToken = expirableTokenEncryptor.decrypt(tokenAsString);
            if (!ExpirableTokenType.SIGNUP_VERIFICATION.equals(verificationToken.getType())) {
                throw new IllegalTokenException();
            }
            String verificationCodeFromUser = context.getParameter("verificationCode");
            if ("post".equalsIgnoreCase(context.getMethod())) {
                counterService.increment(tokenAsString);
                if (verificationToken.getAttributeValue("verificationCode").equals(verificationCodeFromUser)) {
                    Long userId = verificationToken.getAttributeAsLong("userId");
                    User userForVerification = userService.get(userId);
                    if (userForVerification == null) {
                        throw new ExpiredTokenException();
                    }
                    userForVerification.confirm();
                    userService.update(userForVerification);
                    String url = urlBuilderProvider.get().getSignInUrl(SignInState.EMAIL_VERIFICATION_CONFIRMED);
                    return Controllers.redirect(url);
                }
                errorType = "wrongVerificationCode";
            }
        } catch (ExpiredTokenException ete) {
            errorType = "tokenExpired";
        } catch (IllegalTokenException ite) {
            errorType = "wrongToken";
        }
        return htmlWithSecureHeadersProvider.get()
                .render(errorType, true)
                .render("context", context)
                .render("config", properties)
                .render("token", tokenAsString)
                .render("continue", continueUrl)
                .template(TEMPLATE);
    }

    /**
     * Verifies user's email: this action is invoked when the user clicks link in email.
     *
     * @param tokenAsString Code (encrypted user id).
     * @return Result.
     */
    @Timed
    @Transactional
    public Result verifyEmail(@Param("token") String tokenAsString) {
        String signInUrl;
        try {
            ExpirableToken emailConfirmationToken = expirableTokenEncryptor.decrypt(tokenAsString);
            if (!ExpirableTokenType.EMAIL_VERIFICATION.equals(emailConfirmationToken.getType())) {
                throw new IllegalTokenException();
            }
            Long userId = emailConfirmationToken.getAttributeAsLong("userId");
            User user = userService.get(userId);
            if (user != null && !user.isConfirmed()) {
                user.confirm();
                userService.update(user);
            }
            signInUrl = urlBuilderProvider.get().getSignInUrl(SignInState.EMAIL_VERIFICATION_CONFIRMED);
            return Controllers.redirect(signInUrl);
        } catch (NumberFormatException | ExpiredTokenException | IllegalTokenException ex) {
            logger.warn("Unable to confirm user by email token: " + tokenAsString, ex);
        }
        signInUrl = urlBuilderProvider.get().getSignInUrl(SignInState.EMAIL_VERIFICATION_FAILED);
        return Controllers.redirect(signInUrl);
    }
}
