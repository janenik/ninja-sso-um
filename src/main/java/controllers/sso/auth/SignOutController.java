package controllers.sso.auth;

import controllers.sso.filters.LanguageFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import ninja.Cookie;
import ninja.FilterWith;
import ninja.Result;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Sign Out controller that resets cookies for browser based authentication and redirects to continue URL.
 */
@Singleton
@FilterWith({
        LanguageFilter.class
})
public class SignOutController {

    /**
     * URL builder.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Authentication cookie name.
     */
    final String cookieName;

    /**
     * Expired cookie for sign out.
     */
    final Cookie expiredCookieForSignOut;

    /**
     * Controller constructor.
     *
     * @param urlBuilderProvider URL builder provider.
     * @param properties Properties.
     */
    @Inject
    public SignOutController(Provider<UrlBuilder> urlBuilderProvider, NinjaProperties properties) {
        this.urlBuilderProvider = urlBuilderProvider;
        this.properties = properties;
        this.cookieName = properties.getOrDie("application.sso.device.auth.policy.append.cookie");
        this.expiredCookieForSignOut = Cookie.builder(cookieName, "")
                .setMaxAge(0)
                .build();
    }

    /**
     * Signs out current user by resetting authentication cookie.
     *
     * @return Result.
     */
    public Result signOut() {
        return Controllers.redirect(urlBuilderProvider.get().getSignInUrl(), expiredCookieForSignOut);
    }
}
