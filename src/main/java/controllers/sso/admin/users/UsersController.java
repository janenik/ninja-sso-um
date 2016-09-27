package controllers.sso.admin.users;

import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import models.sso.PaginationResult;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.utils.NinjaProperties;
import services.sso.UserEventService;
import services.sso.UserService;
import services.sso.token.PasswordBasedEncryptor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.format.DateTimeFormatter;

/**
 * Users' list admin controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class UsersController {

    /**
     * Template to render users' list page.
     */
    static final String TEMPLATE = "views/sso/admin/users/users.ftl.html";

    /**
     * User service.
     */
    final UserService userService;

    /**
     * User event service.
     */
    final UserEventService userEventService;

    /**
     * Date formatter for list of users.
     */
    final DateTimeFormatter dateTimeFormatter;

    /**
     * Html result with secure headers.
     */
    final Provider<Result> htmlAdminSecureHeadersProvider;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Constructs controller.
     *
     * @param userService User service.
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider.
     * @param properties Application properties.
     */
    @Inject
    public UsersController(
            UserService userService,
            UserEventService userEventService,
            @Named("htmlAdminSecureHeaders") Provider<Result> htmlAdminSecureHeadersProvider,
            DateTimeFormatter dateTimeFormatter,
            NinjaProperties properties) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
        this.properties = properties;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Transactional
    public Result users(Context context) throws PasswordBasedEncryptor.EncryptionException {
        String query = Strings.nullToEmpty(context.getParameter("query")).trim();
        int page = Math.max(1, context.getParameterAsInteger("page", 1));
        int objectsPerPage = properties.getIntegerWithDefault("application.sso.admin.users.objectsPerPage", 20);
        // Log access.
        this.logAccess(query, context);
        // Search.
        PaginationResult<User> results = userService.search(query, page, objectsPerPage);
        return htmlAdminSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("context", context)
                .render("config", properties)
                .render("query", query)
                .render("page", page)
                .render("dateTimeFormatter", dateTimeFormatter)
                .render("results", results);
    }

    /**
     * Logs user data access.
     *
     * @param query Search query.
     * @param context Web context.
     */
    void logAccess(String query, Context context) {
        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        User loggedInUser = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        userEventService.onDataAccess(loggedInUser, query, ip, context.getHeaders());
    }
}
