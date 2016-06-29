package controllers.sso.filters;

import controllers.sso.web.UrlBuilder;
import models.sso.UserRole;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Filter that required admin priveleges for access. Must be used in all admin related controllers.
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
        Object roleAsObject = context.getAttribute(AuthenticationFilter.USER_ROLE);
        if (!UserRole.ADMIN.equals(roleAsObject)) {
            return Results.redirect(urlBuilderProvider.get().getSignInUrl());
        }
        return filterChain.next(context);
    }
}
