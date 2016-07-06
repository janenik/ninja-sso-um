package controllers.sso.filters;

import controllers.sso.web.UrlBuilder;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Filter that requires unauthenticated user for access. Must be used in all pages that require unauthenticated
 * user.
 * Must be used after {@link AuthenticationFilter}.
 */
public class RequireUnauthenticatedUserFilter implements Filter {

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
    public RequireUnauthenticatedUserFilter(Provider<UrlBuilder> urlBuilderProvider) {
        this.urlBuilderProvider = urlBuilderProvider;
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        Object role = context.getAttribute(AuthenticationFilter.USER_ROLE);
        // Redirect to index page if user is logged in.
        if (role != null) {
            return Results.redirect(urlBuilderProvider.get().getIndexUrl());
        }
        return filterChain.next(context);
    }
}
