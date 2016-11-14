package controllers.sso.filters;

import controllers.sso.web.UrlBuilder;
import models.sso.UserRole;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Filter that requires admin priveleges for access. Must be used in all admin related controllers.
 * Must be used after {@link AuthenticationFilter}.
 */
public class RequireAdminPrivelegesFilter implements Filter {

    /**
     * Attribute name for current logged in user.
     */
    public static String LOGGED_IN_USER = "loggedInUser";

    /**
     * User service.
     */
    UserService userService;

    /**
     * URL builder provider to get redirection URL.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Constructs filter.
     *
     * @param userService User service.
     * @param urlBuilderProvider URL builder provider.
     */
    @Inject
    public RequireAdminPrivelegesFilter(UserService userService, Provider<UrlBuilder> urlBuilderProvider) {
        this.userService = userService;
        this.urlBuilderProvider = urlBuilderProvider;
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        Object role = context.getAttribute(AuthenticationFilter.USER_ROLE);
        // Redirect to login page if the current user is not authenticated.
        if (role == null) {
            return Results.redirect(urlBuilderProvider.get().getSignInUrlForCurrentUrl());
        }
        // Redirect to index page if the current user role is not ADMIN (not authorized).
        if (!UserRole.ADMIN.equals(role)) {
            return Results.redirect(urlBuilderProvider.get().getIndexUrl());
        }
        long userId = (long) context.getAttribute(AuthenticationFilter.USER_ID);
        context.setAttribute(LOGGED_IN_USER, userService.get(userId));
        return filterChain.next(context);
    }
}
