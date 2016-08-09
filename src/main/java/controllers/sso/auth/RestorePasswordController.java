package controllers.sso.auth;

import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import controllers.sso.auth.state.SignInState;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireUnauthenticatedUserFilter;
import controllers.sso.web.UrlBuilder;
import dto.sso.common.Constants;
import models.sso.User;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import ninja.utils.NinjaProperties;
import services.sso.PasswordService;
import services.sso.UserEventService;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.IllegalFormatException;

/**
 * Restore password controller.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class,
        AuthenticationFilter.class,
        RequireUnauthenticatedUserFilter.class
})
public class RestorePasswordController {

    /**
     * Template to render sign up page.
     */
    static final String TEMPLATE = "views/sso/auth/restorePassword.ftl.html";

    /**
     * User service.
     */
    final UserService userService;

    /**
     * User's event service.
     */
    final UserEventService userEventService;

    /**
     * Password service.
     */
    final PasswordService passwordService;

    /**
     * Expirable token encryptor.
     */
    final ExpirableTokenEncryptor expirableTokenEncryptor;

    /**
     * URL builder provider for controller. Instance per request.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Html result with secure headers.
     */
    final Provider<Result> htmlWithSecureHeadersProvider;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Constructs controller.
     *
     * @param userService User service.
     * @param userEventService User's event service.
     * @param passwordService Password service.
     * @param expirableTokenEncryptor Expirable token encryptor.
     * @param urlBuilderProvider URL builder provider.
     * @param properties Application properties.
     */
    @Inject
    public RestorePasswordController(UserService userService,
                                     UserEventService userEventService,
                                     PasswordService passwordService,
                                     ExpirableTokenEncryptor expirableTokenEncryptor,
                                     Provider<UrlBuilder> urlBuilderProvider,
                                     @Named("htmlSecureHeaders") Provider<Result> htmlWithSecureHeadersProvider,
                                     NinjaProperties properties) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.passwordService = passwordService;
        this.expirableTokenEncryptor = expirableTokenEncryptor;
        this.urlBuilderProvider = urlBuilderProvider;
        this.htmlWithSecureHeadersProvider = htmlWithSecureHeadersProvider;
        this.properties = properties;
    }

    /**
     * Renders restore password form.
     *
     * @param context Context.
     * @param restoreToken Restore token from email sent to user by {@link ForgotPasswordController}.
     * @return Result with data for restore password form.
     */
    public Result restorePasswordGet(Context context, @Param(value = "restoreToken") String restoreToken) {
        Result result = createResult(context, restoreToken);
        try {
            ExpirableToken token = expirableTokenEncryptor.decrypt(restoreToken);
            Long userId = token.getAttributeAsLong("userId");
            User user = userService.get(userId);
            if (user == null) {
                throw new ExpiredTokenException();
            }
            result.render("user", user);
        } catch (ExpiredTokenException ex) {
            result.render("restorePasswordError", "expired");
        } catch (IllegalTokenException | IllegalFormatException ex) {
            result.render("restorePasswordError", "unknown");
        }
        return result;
    }


    /**
     * Processes submit of the restore password form.
     *
     * @param context Context.
     * @param restoreToken Restore token.
     * @param password Password.
     * @param confirmPassword Password confirmation.
     * @return Result.
     */
    @Transactional
    public Result restorePassword(Context context,
                                  @Param(value = "restoreToken") String restoreToken,
                                  @Param(value = "password") String password,
                                  @Param(value = "confirmPassword") String confirmPassword) {
        password = Strings.nullToEmpty(password);
        confirmPassword = Strings.nullToEmpty(confirmPassword);

        Result result = createResult(context, restoreToken);
        try {
            ExpirableToken token = expirableTokenEncryptor.decrypt(restoreToken);
            Long userId = token.getAttributeAsLong("userId");
            User user = userService.get(userId);
            if (user == null) {
                throw new ExpiredTokenException();
            }
            if (isValidPassword(password, confirmPassword)) {
                String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
                byte[] oldSalt = user.getPasswordSalt();
                byte[] oldHash = user.getPasswordHash();
                userService.updatePasswordAndConfirm(user, password);
                userEventService.onUserPasswordUpdate(user, oldSalt, oldHash, ip, context.getHeaders());
                return Results.redirect(urlBuilderProvider.get().getSignInUrl(SignInState.PASSWORD_CHANGED));
            } else {
                result.render("restorePasswordError", "password");
            }
            result.render("user", user);
        } catch (ExpiredTokenException ex) {
            result.render("restorePasswordError", "expired");
        } catch (IllegalTokenException | IllegalFormatException ex) {
            result.render("restorePasswordError", "unknown");
        }
        return result;
    }

    /**
     * Creates common result for GET and POST forms.
     *
     * @param context Context.
     * @param restoreToken Restore token.
     * @return Result with data and template.
     */
    Result createResult(Context context, String restoreToken) {
        String locale = (String) context.getAttribute(LanguageFilter.LANG);
        return htmlWithSecureHeadersProvider.get()
                .render("context", context)
                .render("continue", urlBuilderProvider.get().getContinueUrlParameter())
                .render("config", properties)
                .render("restoreToken", restoreToken)
                .render("lang", locale)
                .template(TEMPLATE);
    }

    /**
     * Validates given password, verifies it with confirmation password.
     *
     * @param password Password.
     * @param confirmPassword Confirm password.
     * @return Whether the given password is valid.
     */
    static boolean isValidPassword(String password, String confirmPassword) {
        return password.length() >= Constants.PASSWORD_MIN_LENGTH
                && password.length() <= Constants.PASSWORD_MAX_LENGTH
                && password.equals(confirmPassword);
    }
}
