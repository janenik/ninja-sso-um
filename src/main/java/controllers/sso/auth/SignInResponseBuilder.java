package controllers.sso.auth;

import com.google.inject.servlet.RequestScoped;
import controllers.annotations.ApplicationPolicy;
import controllers.annotations.BrowserPolicy;
import controllers.annotations.InjectedContext;
import controllers.sso.auth.policy.AppendAuthTokenPolicy;
import controllers.sso.auth.policy.DeviceAuthPolicy;
import controllers.sso.auth.type.DeviceInputType;
import controllers.sso.filters.DeviceTypeFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.Escapers;
import controllers.sso.web.UrlBuilder;
import models.sso.User;
import models.sso.UserSignInState;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenEncryptorException;
import models.sso.token.ExpirableTokenType;
import ninja.Context;
import ninja.Cookie;
import ninja.Result;
import ninja.utils.NinjaProperties;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import javax.inject.Inject;

/**
 * Provides Sign In response with appropriate URL redirect and needed headers.
 */
@RequestScoped
public class SignInResponseBuilder {

    /**
     * Device authentication policy.
     */
    final DeviceAuthPolicy deviceAuthPolicy;

    /**
     * Policy that defines a way to append auth token to browser's response.
     */
    final AppendAuthTokenPolicy browserAppendTokenPolicy;

    /**
     * Policy that defines a way to append auth token to standalone application's response.
     */
    final AppendAuthTokenPolicy applicationAppendTokenPolicy;

    /**
     * URL builder.
     */
    final UrlBuilder urlBuilder;

    /**
     * Context.
     */
    final Context context;

    /**
     * Expirable token encryptor.
     */
    final ExpirableTokenEncryptor encryptor;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Authentication cookie name.
     */
    final String authCookieName;

    /**
     * Constructs response provider.
     *
     * @param deviceAuthPolicy Device authentication policy.
     * @param browserAppendTokenPolicy Browser append token policy.
     * @param applicationAppendAuthTokenPolicy Application append token policy.
     */
    @Inject
    public SignInResponseBuilder(
            DeviceAuthPolicy deviceAuthPolicy,
            @BrowserPolicy AppendAuthTokenPolicy browserAppendTokenPolicy,
            @ApplicationPolicy AppendAuthTokenPolicy applicationAppendAuthTokenPolicy,
            @InjectedContext Context context,
            ExpirableTokenEncryptor encryptor,
            UrlBuilder urlBuilder,
            NinjaProperties properties) {
        this.deviceAuthPolicy = deviceAuthPolicy;
        this.browserAppendTokenPolicy = browserAppendTokenPolicy;
        this.applicationAppendTokenPolicy = applicationAppendAuthTokenPolicy;
        this.context = context;
        this.encryptor = encryptor;
        this.urlBuilder = urlBuilder;
        this.properties = properties;
        this.authCookieName = properties.getOrDie("application.sso.device.auth.policy.append.cookie");
    }

    /**
     * Supplies the Sign In response with authentication, appropriate to device and auth policy.
     *
     * @param user User to use for access token.
     * @return Authentication response.
     */
    public Result getSignInResponse(User user) {
        DeviceInputType inputType = (DeviceInputType) context.getAttribute(DeviceTypeFilter.DEVICE_INPUT_TYPE);
        try {
            if (DeviceInputType.POINTER.equals(inputType)) {
                if (DeviceAuthPolicy.APPLICATION.equals(deviceAuthPolicy)) {
                    return getApplicationSignInResponse(user);
                } else {
                    return getBrowserSignInResponse(user);
                }
            } else {
                if (DeviceAuthPolicy.BROWSER.equals(deviceAuthPolicy)) {
                    return getBrowserSignInResponse(user);
                } else {
                    return getApplicationSignInResponse(user);
                }
            }
        } catch (ExpirableTokenEncryptorException ee) {
            throw new RuntimeException(ee);
        }
    }

    /**
     * Builds sign out response with authentication cookie reset.
     *
     * @return Sign out response.
     */
    public Result getSignOutResponse() {
        Cookie resetCookie = Cookie.builder(authCookieName, "")
                .setSecure(properties.isProd())
                .setDomain(properties.getOrDie("application.domain"))
                .setPath("/")
                .setHttpOnly(true)
                .setMaxAge(1)
                .build();
        return Controllers.redirect(urlBuilder.getSignInUrl(), resetCookie);
    }

    /**
     * Builds browser related authentication with appropriate auth policy.
     *
     * @param user User to use for access token.
     * @return Browser authentication response.
     * @throws PasswordBasedEncryptor.EncryptionException If there was an error related to encryption of the token.
     */
    private Result getBrowserSignInResponse(User user) throws ExpirableTokenEncryptorException {
        String continueUrl = urlBuilder.getContinueUrlParameter();
        String accessTokenAsString = buildNewUserToken(user);

        if (AppendAuthTokenPolicy.COOKIE.equals(browserAppendTokenPolicy)) {
            // Remember access token in secure, HTTP only cookie. HTTP proxy servers are expected to pass it to
            // upstream, ignoring or disabling secure parameter.
            Cookie cookie = Cookie.builder(authCookieName, accessTokenAsString)
                    .setDomain(properties.getOrDie("application.domain"))
                    .setMaxAge(properties.getIntegerOrDie("application.sso.accessToken.ttl"))
                    .setSecure(false)
                    .setHttpOnly(true)
                    .setPath("/")
                    .build();
            return Controllers.redirect(continueUrl, cookie);
        }

        // Otherwise, append access token as a parameter to URL.
        StringBuilder resultUrlBuilder = new StringBuilder(continueUrl);
        if (AppendAuthTokenPolicy.URL_PARAM.equals(browserAppendTokenPolicy)) {
            resultUrlBuilder.append(continueUrl.contains("?") ? "&" : "?");
        } else {
            resultUrlBuilder.append(continueUrl.contains("#") ? "&" : "#");
        }
        String parameterName = properties.getOrDie("application.sso.device.auth.policy.append.parameter");
        resultUrlBuilder
                .append(Escapers.encodePercent(parameterName))
                .append("=")
                .append(Escapers.encodePercent(accessTokenAsString));
        return Controllers.redirect(resultUrlBuilder.toString());
    }

    /**
     * Builds application related authentication with appropriate auth policy.
     *
     * @param user User to use for access token.
     * @return Application authentication response.
     * @throws PasswordBasedEncryptor.EncryptionException If there was an error related to encryption of the token.
     */
    private Result getApplicationSignInResponse(User user) throws ExpirableTokenEncryptorException {
        String applicationDefinedBaseUrl = properties.getOrDie("application.sso.device.auth.policy.application.url");
        StringBuilder resultUrlBuilder = new StringBuilder(applicationDefinedBaseUrl);
        String accessTokenAsString = buildNewUserToken(user);
        if (AppendAuthTokenPolicy.URL_PARAM.equals(applicationAppendTokenPolicy)) {
            resultUrlBuilder.append(applicationDefinedBaseUrl.contains("?") ? "&" : "?");
        } else {
            resultUrlBuilder.append(applicationDefinedBaseUrl.contains("#") ? "&" : "#");
        }
        String parameterName = properties.getOrDie("application.sso.device.auth.policy.append.parameter");
        resultUrlBuilder
                .append(Escapers.encodePercent(parameterName))
                .append("=")
                .append(Escapers.encodePercent(accessTokenAsString));
        return Controllers.redirect(resultUrlBuilder.toString());
    }

    /**
     * Builds new user access token and returns it as encrypted string.
     *
     * @param user User.
     * @return New access token as encrypted string.
     * @throws PasswordBasedEncryptor.EncryptionException If there was an error related to encryption of the token.
     * @throws IllegalStateException When user's sign-in state is not {@link UserSignInState#ENABLED_AS_USER} or
     * {@link UserSignInState#ENABLED}.
     */
    private String buildNewUserToken(User user) throws ExpirableTokenEncryptorException {
        if (!user.isSignInEnabled()) {
            throw new IllegalStateException("Unable to provide access token for the user who's sign-in " +
                    "state is disabled. User id: " + user.getId());
        }
        long ttl = 1000L * properties.getIntegerOrDie("application.sso.accessToken.ttl");
        ExpirableToken token;
        if (user.isModeratorOrAdmin()) {
            token = ExpirableToken.newUserToken(
                    ExpirableTokenType.ACCESS,
                    user.getId(),
                    "role",
                    user.getRole().toString(),
                    ttl
            );
        } else {
            token = ExpirableToken.newUserToken(ExpirableTokenType.ACCESS, user.getId(), ttl);
        }
        return encryptor.encrypt(token);
    }
}
