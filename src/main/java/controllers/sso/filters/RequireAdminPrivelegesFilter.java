package controllers.sso.filters;

import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import models.sso.UserRole;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Filter that requires admin priveleges for access. Must be used in all admin related controllers.
 * Must be used after {@link AuthenticationFilter}.
 */
public class RequireAdminPrivelegesFilter implements Filter {

    /**
     * URL builder provider to get redirection URL.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Constructs filter.
     *
     * @param urlBuilderProvider URL builder provider.
     */
    @Inject
    public RequireAdminPrivelegesFilter(Provider<UrlBuilder> urlBuilderProvider) {
        this.urlBuilderProvider = urlBuilderProvider;
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        Object role = context.getAttribute(AuthenticationFilter.USER_ROLE);
        // Redirect to login page if the current user is not authenticated.
        if (role == null) {
            return Controllers.redirect(urlBuilderProvider.get().getSignInUrlForCurrentUrl());
        }
        // Redirect to index page if the current user role is not ADMIN (not authorized).
        if (!UserRole.ADMIN.equals(role)) {
            return Controllers.redirect(urlBuilderProvider.get().getIndexUrl());
        }
        return filterChain.next(context);
    }
}
