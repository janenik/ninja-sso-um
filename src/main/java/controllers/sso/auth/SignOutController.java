package controllers.sso.auth;

import controllers.sso.web.UrlBuilder;
import ninja.Cookie;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Sign Out controller that resets cookies for browser based authentication and redirects to continue URL.
 */
@Singleton
public class SignOutController {

    /**
     * URL builder.
     */
    @Inject
    UrlBuilder urlBuilder;

    /**
     * Application properties.
     */
    @Inject
    NinjaProperties properties;

    /**
     * Signs out current user by resetting authentication cookie.
     *
     * @return Result.
     */
    public Result signOut() {
        String cookieName = properties.getOrDie("application.sso.device.auth.policy.append.cookie");
        return Results.redirect(urlBuilder.getContinueUrlParameter())
                .addCookie(Cookie.builder(cookieName, "")
                        .setMaxAge(0)
                        .build());
    }
}
