package controllers.sso.auth;

import com.google.inject.servlet.RequestScoped;
import controllers.sso.auth.policy.AppendAuthTokenPolicy;
import controllers.sso.auth.policy.DeviceAuthPolicy;
import controllers.sso.auth.type.DeviceInputType;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.DeviceTypeFilter;
import controllers.sso.web.Escapers;
import controllers.sso.web.UrlBuilder;
import models.sso.User;
import models.sso.UserRole;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenEncryptorException;
import models.sso.token.ExpirableTokenType;
import ninja.Context;
import ninja.Cookie;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import javax.inject.Inject;
import javax.inject.Named;

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
     * Constructs response provider.
     *
     * @param deviceAuthPolicy Device authentication policy.
     * @param browserAppendTokenPolicy Browser append token policy.
     * @param applicationAppendAuthTokenPolicy Application append token policy.
     */
    @Inject
    public SignInResponseBuilder(
            DeviceAuthPolicy deviceAuthPolicy,
            @Named("browser") AppendAuthTokenPolicy browserAppendTokenPolicy,
            @Named("application") AppendAuthTokenPolicy applicationAppendAuthTokenPolicy,
            @Named("ssoContext") Context context,
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
     * Supplies browser related authentication with appropriate auth policy.
     *
     * @param user User to use for access token.
     * @return Browser authentication response.
     * @throws PasswordBasedEncryptor.EncryptionException If there was an error related to encryption of the token.
     */
    private Result getBrowserSignInResponse(User user) throws ExpirableTokenEncryptorException {
        String continueUrl = urlBuilder.getContinueUrlParameter();
        String accessTokenAsString = buildNewUserToken(user);

        if (AppendAuthTokenPolicy.COOKIE.equals(browserAppendTokenPolicy)) {
            String cookieName = properties.getOrDie("application.sso.device.auth.policy.append.cookie");
            // Remember access token in secure, HTTP only cookie. HTTP proxy servers are expected to pass it to
            // upstream, ignoring or disabling secure parameter.
            Cookie cookie = Cookie.builder(cookieName, accessTokenAsString)
                    .setSecure(properties.isProd())
                    .setHttpOnly(true)
                    .setPath("/")
                    .setDomain(properties.getOrDie("application.domain"))
                    .setMaxAge(properties.getIntegerOrDie("application.sso.accessToken.ttl"))
                    .build();
            return Results.redirect(continueUrl)
                    .addCookie(cookie);
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
        return Results.redirect(resultUrlBuilder.toString());
    }

    /**
     * Supplies application related authentication with appropriate auth policy.
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
        return Results.redirect(resultUrlBuilder.toString());
    }

    /**
     * Builds new access token and returns it as encrypted string.
     *
     * @param user User.
     * @return New access token as encrypted string.
     * @throws PasswordBasedEncryptor.EncryptionException If there was an error related to encryption of the token.
     */
    private String buildNewUserToken(User user) throws ExpirableTokenEncryptorException {
        long ttl = 1000L * properties.getIntegerOrDie("application.sso.accessToken.ttl");
        ExpirableToken token;
        if (!UserRole.USER.equals(user.getRole()) && user.getRole() != null) {
            token = ExpirableToken.newTokenForUser(ExpirableTokenType.ACCESS,
                    user.getId(), AuthenticationFilter.USER_ROLE, user.getRole().toString(), ttl);
        } else {
            token = ExpirableToken.newTokenForUser(ExpirableTokenType.ACCESS, user.getId(), ttl);
        }
        return encryptor.encrypt(token);
    }
}
