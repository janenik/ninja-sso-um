package controllers.sso.filters;

import com.google.common.base.Strings;
import controllers.sso.auth.policy.DeviceAuthPolicy;
import models.sso.UserRole;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenEncryptorException;
import models.sso.token.ExpirableTokenType;
import ninja.Context;
import ninja.Cookie;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Authentication filter that extracts user id and expirable token from request, according to DeviceAuthPolicy
 * and places it into the attributes as {@link AuthenticationFilter#USER_ID}, {@link AuthenticationFilter#USER_ROLE} and
 * {@link AuthenticationFilter#TOKEN}.
 */
@Singleton
public class AuthenticationFilter implements Filter {

    /**
     * Authentication context parameter name for user id.
     */
    public static final String USER_ID = "userId";

    /**
     * Authentication context parameter name for user role.
     */
    public static final String USER_ROLE = "userRole";

    /**
     * Authentication context parameter name for expirable token.
     */
    public static final String TOKEN = "token";

    /**
     * Newly generated XSRF token as string.
     */
    public static final String XSRF_TOKEN = "xsrfToken";

    /**
     * XSRF token time to live.
     */
    public static final String XSRF_TOKEN_TTL = "xsrfTokenTtl";

    /**
     * Encryptor to extract data from token.
     */
    final ExpirableTokenEncryptor encryptor;

    /**
     * XSRF token time to live, in milliseconds.
     */
    final long xsrfTokenTimeToLive;

    /**
     * Device auth policy.
     */
    final DeviceAuthPolicy deviceAuthPolicy;

    /**
     * Parameter name to hold access token.
     */
    final String parameterName;

    /**
     * Cookie name to hold access token.
     */
    final String cookieName;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Constructs authorization filter.
     *
     * @param encryptor Encryptor.
     * @param properties Properties.
     */
    @Inject
    public AuthenticationFilter(ExpirableTokenEncryptor encryptor, DeviceAuthPolicy deviceAuthPolicy,
                                NinjaProperties properties, Logger logger) {
        this.encryptor = encryptor;
        this.deviceAuthPolicy = deviceAuthPolicy;
        this.logger = logger;
        this.parameterName = properties.getOrDie("application.sso.device.auth.policy.append.parameter");
        this.cookieName = properties.getOrDie("application.sso.device.auth.policy.append.cookie");
        this.xsrfTokenTimeToLive = properties.getIntegerOrDie("application.sso.xsrfToken.ttl") * 1000L;
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        try {
            String token = getToken(context);
            ExpirableToken expirableToken = token != null ? encryptor.decrypt(token) : null;
            if (expirableToken != null && ExpirableTokenType.ACCESS.equals(expirableToken.getType())) {
                Long userId = expirableToken.getAttributeAsLong(USER_ID);
                if (userId == null) {
                    throw new IllegalStateException("Access token is expected to contain user id: " + token);
                }
                context.setAttribute(USER_ID, userId);
                context.setAttribute(USER_ROLE, UserRole.fromString(expirableToken.getAttributeValue(USER_ROLE)));
                context.setAttribute(TOKEN, expirableToken);
                ExpirableToken xsrfToken =
                        ExpirableToken.newTokenForUser(ExpirableTokenType.XSRF, userId, xsrfTokenTimeToLive);
                context.setAttribute(XSRF_TOKEN, encryptor.encrypt(xsrfToken));
                context.setAttribute(XSRF_TOKEN_TTL, xsrfTokenTimeToLive);
            }
        } catch (ExpirableTokenEncryptorException ex) {
            logger.info("Error while encrypting/decrypting user or XSRF token.", ex);
        }
        return filterChain.next(context);
    }

    /**
     * Returns token according to application {@link DeviceAuthPolicy} if it exists.
     * Otherwise, returns null.
     *
     * @param context Current context.
     * @return Token according to application {@link DeviceAuthPolicy} if it exists.
     */
    private String getToken(Context context) {
        if (DeviceAuthPolicy.BROWSER.equals(deviceAuthPolicy) || DeviceAuthPolicy.AUTO.equals(deviceAuthPolicy)) {
            Cookie cookie = context.getCookie(cookieName);
            String cookieToken = cookie != null ? Strings.emptyToNull(cookie.getValue()) : null;
            if (cookieToken != null) {
                return cookieToken;
            }
        }
        if (DeviceAuthPolicy.APPLICATION.equals(deviceAuthPolicy) || DeviceAuthPolicy.AUTO.equals(deviceAuthPolicy)) {
            return Strings.emptyToNull(context.getParameter(parameterName));
        }
        return null;
    }
}
