package controllers.sso.filters;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.Cookie;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import services.sso.token.ExpirableTokenEncryptor;

/**
 * Authorization filter that extracts user id and expirable token from request, according to AuthPolicy
 * and places it into the attributes as {@link AuthorizationFilter#USER_ID} and
 * {@link AuthorizationFilter#TOKEN}.
 */
public class AuthorizationFilter implements Filter {

    /**
     * Authorization context parameter name for user id.
     */
    public static final String USER_ID = "userId";

    /**
     * Authorization context parameter name for expirable token.
     */
    public static final String TOKEN = "token";

    /**
     * Encryptor to extract data from token.
     */
    ExpirableTokenEncryptor encryptor;

    /**
     * Logger.
     */
    Logger logger;

    /**
     * Parameter or cookie name to hold the token.
     */
    String parameterName;

    /**
     * Constructs authorization filter.
     *
     * @param encryptor Encryptor.
     * @param properties Properties.
     */
    @Inject
    public AuthorizationFilter(ExpirableTokenEncryptor encryptor, NinjaProperties properties, Logger logger) {
        this.encryptor = encryptor;
        this.logger = logger;
        this.parameterName = properties.getOrDie("application.sso.tokens.access.cookie.name");
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        try {
            Cookie tokenCookie = context.getCookie(parameterName);
            String token = tokenCookie != null ? tokenCookie.getValue() : null;
            if (token == null || token.isEmpty()) {
                token = Strings.emptyToNull(context.getParameter(parameterName));
            }

            ExpirableToken expirableToken = token != null ? encryptor.decrypt(token) : null;
            if (expirableToken != null && ExpirableTokenType.ACCESS.equals(expirableToken.getType())) {
                context.setAttribute(USER_ID, expirableToken.getAttributeAsLong(USER_ID));
                context.setAttribute(TOKEN, expirableToken);
            }
        } catch (ExpiredTokenException | IllegalTokenException ex) {
            logger.info("Error while decrypting token.", ex);
        }
        return filterChain.next(context);
    }
}
