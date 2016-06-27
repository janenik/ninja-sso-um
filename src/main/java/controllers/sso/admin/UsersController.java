package controllers.sso.admin;

import com.google.common.base.Strings;
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
import services.sso.token.PasswordBasedEncryptor;

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
public class UsersController {

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
    public UsersController(UserService userService,
                           NinjaProperties properties) {
        this.userService = userService;
        this.properties = properties;
    }

    @Transactional
    public Result users(Context context) throws PasswordBasedEncryptor.EncryptionException {
        String query = Strings.nullToEmpty(context.getParameter("query")).trim();
        int page = Math.max(1, context.getParameterAsInteger("page", 1));
        int objectsPerPage = properties.getIntegerWithDefault("application.sso.admin.users.objectsPerPage", 20);
        PaginationResult<User> results = userService.search(query, page, objectsPerPage);
        return Results.html().template(TEMPLATE)
                .render("config", properties)
                .render("query", query)
                .render("page", page)
                .render("results", results);
    }
}
