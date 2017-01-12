package controllers.sso.filters;

import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import org.slf4j.Logger;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * XSRF token filter: verifies presence of valid XSRF token in POST request during form submission.
 * Must be used after {@link AuthenticationFilter}.
 */
@Singleton
public class XsrfTokenFilter implements Filter {

    /**
     * Invalid XSRF token parameter name.
     */
    public static final String INVALID_XSRF_TOKEN_PARAMETER_NAME = "invalidXsrfToken";

    /**
     * Encryptor to extract data from token.
     */
    final ExpirableTokenEncryptor encryptor;

    /**
     * URL builder provider to get redirection URL.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Constructs XSRF token filter.
     *
     * @param encryptor Expirable token encryptor/decryptor.
     */
    @Inject
    public XsrfTokenFilter(ExpirableTokenEncryptor encryptor, Logger logger, Provider<UrlBuilder> urlBuilderProvider) {
        this.encryptor = encryptor;
        this.logger = logger;
        this.urlBuilderProvider = urlBuilderProvider;
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        Long authorizedUserId = context.getAttribute(AuthenticationFilter.USER_ID, Long.class);

        // If there is no authorized user or method is GET then other filters or controllers should
        // make a decision.
        if (authorizedUserId == null || "GET".equalsIgnoreCase(context.getMethod())) {
            String errorParameter = context.getParameter(INVALID_XSRF_TOKEN_PARAMETER_NAME);
            if (Boolean.TRUE.toString().equals(errorParameter)) {
                context.setAttribute(INVALID_XSRF_TOKEN_PARAMETER_NAME, true);
            }
            return filterChain.next(context);
        }

        // Now check XSRF.
        String xsrfTokenAsString = context.getParameter(AuthenticationFilter.XSRF_TOKEN);
        try {
            ExpirableToken xsrfToken = encryptor.decrypt(xsrfTokenAsString);
            Long tokenUserId = xsrfToken.getAttributeAsLong("userId");
            if (ExpirableTokenType.XSRF.equals(xsrfToken.getType())
                    && !xsrfToken.isExpired()
                    && authorizedUserId.equals(tokenUserId)) {
                // XSRF token is valid.
                return filterChain.next(context);
            }
        } catch (ExpiredTokenException | IllegalTokenException ex) {
            logger.info("Error while decrypting XSRF token.", ex);
        }

        // Redirect to same URL with invalid XSRF token parameter.
        // Please note that this should be reviewed for AJAX requests. Currently SSO doesn't use one.
        String sameUrl = urlBuilderProvider.get()
                .getCurrentUrl(INVALID_XSRF_TOKEN_PARAMETER_NAME, Boolean.TRUE.toString());
        return Controllers.redirect(sameUrl);

    }
}
