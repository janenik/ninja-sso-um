package controllers.sso.admin.users;

import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import controllers.annotations.SecureHtmlHeadersForAdmin;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.web.UrlBuilder;
import models.sso.PaginationResult;
import models.sso.User;
import models.sso.UserEvent;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.format.DateTimeFormatter;

/**
 * View access log controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class ViewAccessLogController {

    /**
     * Template to render users' list page.
     */
    static final String TEMPLATE = "views/sso/admin/users/view-access-log.ftl.html";

    /**
     * User service.
     */
    final UserService userService;

    /**
     * User event service.
     */
    final UserEventService userEventService;

    /**
     * Date-time formatter for list of users.
     */
    final DateTimeFormatter dateTimeFormatter;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * URL builder provider for controller. Instance per request.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Html result with secure headers.
     */
    final Provider<Result> htmlAdminSecureHeadersProvider;

    /**
     * Objects per page.
     */
    final int objectsPerPage;

    /**
     * Constructs access log controller.
     *
     * @param userService User service.
     * @param userEventService User event service.
     * @param dateTimeFormatter Date-time dateTimeFormatter.
     * @param urlBuilderProvider URL builder provider.
     * @param properties Application properties.
     * @param htmlAdminSecureHeadersProvider HTML result provider with secure headers for admin.
     */
    @Inject
    public ViewAccessLogController(
            UserService userService,
            UserEventService userEventService,
            DateTimeFormatter dateTimeFormatter,
            Provider<UrlBuilder> urlBuilderProvider,
            NinjaProperties properties,
            @SecureHtmlHeadersForAdmin Provider<Result> htmlAdminSecureHeadersProvider) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.dateTimeFormatter = dateTimeFormatter;
        this.properties = properties;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
        this.objectsPerPage = properties.getIntegerWithDefault("application.sso.admin.events.objectsPerPage", 10);
    }

    /**
     * Fetches list of user events.
     *
     * @param userId Target user id.
     * @param context Request context.
     * @return Results of rendering.
     */
    @Transactional
    public Result get(@PathParam("userId") long userId, Context context) {
        String query = Strings.nullToEmpty(context.getParameter("eventsQuery")).trim();
        int page = Math.max(1, context.getParameterAsInteger("eventsPage", 1));
        // Fetch target user.
        User target = userService.get(userId);
        if (target == null) {
            return Results.redirect(urlBuilderProvider.get().getAdminUsersUrl(query, page));
        }
        // Log access.
        logEventsAccess(target, context);
        // Search.
        PaginationResult<UserEvent> results = userEventService.searchByUser(target, query, page, objectsPerPage);
        return htmlAdminSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("context", context)
                .render("config", properties)
                .render("userEntity", target)
                .render("query", Strings.nullToEmpty(context.getParameter("query")).trim())
                .render("page", Math.max(1, context.getParameterAsInteger("page", 1)))
                .render("eventsQuery", query)
                .render("eventsPage", page)
                .render("dateTimeFormatter", dateTimeFormatter)
                .render("results", results);
    }

    /**
     * Logs user data access.
     *
     * @param target Target user whose data is accessed.
     * @param context Web context.
     */
    void logEventsAccess(User target, Context context) {
        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        String currentUrl = urlBuilderProvider.get().getCurrentUrl();
        User loggedInUser = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        userEventService.onUserLogAccess(loggedInUser, target, currentUrl, ip, context.getHeaders());
    }
}
