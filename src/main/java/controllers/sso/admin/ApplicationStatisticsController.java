package controllers.sso.admin;

import controllers.annotations.SecureHtmlHeadersForAdmin;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.rest.RestResponse;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.utils.NinjaProperties;
import services.sso.UserService;
import services.sso.admin.ApplicationStatisticsService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.management.MBeanException;

/**
 * Statistics controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class ApplicationStatisticsController {
    /**
     * Template to render statistics page.
     */
    private static final String TEMPLATE = "views/sso/admin/statistics.ftl.html";

    /**
     * Application statistics service.
     */
    private final ApplicationStatisticsService applicationStatisticsService;

    /**
     * Html result with secure headers.
     */
    private final Provider<Result> htmlAdminSecureHeadersProvider;

    /**
     * User service.
     */
    private UserService userService;

    /**
     * Application properties.
     */
    private final NinjaProperties properties;

    @Inject
    public ApplicationStatisticsController(
            ApplicationStatisticsService applicationStatisticsService,
            UserService userService,
            @SecureHtmlHeadersForAdmin Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        this.applicationStatisticsService = applicationStatisticsService;
        this.userService = userService;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
        this.properties = properties;
    }

    /**
     * Renders statistics page.
     *
     * @return Result with statistics page.
     */
    public Result get(Context context) {
        return htmlAdminSecureHeadersProvider.get()
                .render("context", context)
                .render("loggedInUser", userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID)))
                .template(TEMPLATE);
    }

    /**
     * Renders statistics JSON.
     *
     * @return Result with statistics JSON.
     */
    public Result json() {
        Result result = htmlAdminSecureHeadersProvider.get().json();
        try {
            return result.render(RestResponse.newResponse(applicationStatisticsService.getStatistics()));
        } catch (MBeanException mbe) {
            return result.render(RestResponse.serverError(mbe));
        }
    }
}
