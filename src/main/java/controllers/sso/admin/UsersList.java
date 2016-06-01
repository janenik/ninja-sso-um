package controllers.sso.admin;

import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import models.sso.PaginationResult;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Users' list admin controller.
 * TODO: add admin session filter.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class
})
public class UsersList {

    /**
     * Template to render users' list page.
     */
    static final String TEMPLATE = "views/sso/admin/users.ftl.html";

    /**
     * User service.
     */
    final UserService userService;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Constructs controller.
     *
     * @param userService User service.
     */
    @Inject
    public UsersList(UserService userService, NinjaProperties properties) {
        this.userService = userService;
        this.properties = properties;
    }

    @Transactional
    public Result list(Context context) {
        String query = context.getParameter("query");
        long page = Math.abs(context.getParameterAsInteger("page", 1));
        long objectsPerPage = properties.getIntegerWithDefault("application.sso.admin.users.objectsPerPage", 20);
        PaginationResult<User> results = userService.search(query, page, objectsPerPage);
        return Results.html().template(TEMPLATE)
                .render("config", properties)
                .render("results", results);
    }
}
