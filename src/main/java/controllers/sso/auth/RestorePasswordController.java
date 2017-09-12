package controllers.sso.auth;

import com.google.common.base.Strings;
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
import dto.sso.common.Constants;
import models.sso.User;
import models.sso.UserCredentials;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.metrics.Timed;
import ninja.params.Param;
import ninja.utils.NinjaProperties;
import services.sso.UserEventService;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.IllegalFormatException;

/**
 * Restore password controller.
 */
@Singleton
@FilterWith({
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
    private static final String TEMPLATE = "views/sso/auth/restorePassword.ftl.html";

    /**
     * User service.
     */
    private final UserService userService;

    /**
     * User's event service.
     */
    private final UserEventService userEventService;

    /**
     * Expirable token encryptor.
     */
    private final ExpirableTokenEncryptor expirableTokenEncryptor;

    /**
     * URL builder provider for controller. Instance per request.
     */
    private final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Html result with secure headers.
     */
    private final Provider<Result> htmlWithSecureHeadersProvider;

    /**
     * Application properties.
     */
    private final NinjaProperties properties;

    /**
     * Constructs controller.
     *
     * @param userService User service.
     * @param userEventService User's event service.
     * @param expirableTokenEncryptor Expirable token encryptor.
     * @param urlBuilderProvider URL builder provider.
     * @param properties Application properties.
     */
    @Inject
    public RestorePasswordController(UserService userService,
                                     UserEventService userEventService,
                                     ExpirableTokenEncryptor expirableTokenEncryptor,
                                     Provider<UrlBuilder> urlBuilderProvider,
                                     @SecureHtmlHeaders Provider<Result> htmlWithSecureHeadersProvider,
                                     NinjaProperties properties) {
        this.userService = userService;
        this.userEventService = userEventService;
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
    @Timed
    @Transactional
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
    @Timed
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
                UserCredentials credentials = userService.getCredentials(user);
                byte[] oldSalt = credentials.getPasswordSalt();
                byte[] oldHash = credentials.getPasswordHash();
                userService.updatePasswordAndConfirm(user, password);
                userEventService.onUserPasswordUpdate(user, oldSalt, oldHash, ip, context.getHeaders());
                String url = urlBuilderProvider.get().getSignInUrl(SignInState.PASSWORD_CHANGED);
                return Controllers.redirect(url);
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
    private Result createResult(Context context, String restoreToken) {
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
    private static boolean isValidPassword(String password, String confirmPassword) {
        return password.length() >= Constants.PASSWORD_MIN_LENGTH
                && password.length() <= Constants.PASSWORD_MAX_LENGTH
                && password.equals(confirmPassword);
    }
}
