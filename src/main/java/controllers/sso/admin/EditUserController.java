package controllers.sso.admin;

import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.web.UrlBuilder;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;
import org.dozer.Mapper;
import services.sso.CountryService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.transaction.Transactional;

/**
 * Edit user controller.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class EditUserController {

    /**
     * Template to render users' list page.
     */
    static final String TEMPLATE = "views/sso/admin/editUser.ftl.html";

    /**
     * User service.
     */
    final UserService userService;

    /**
     * Country service.
     */
    final CountryService countryService;

    /**
     * DTO mapper.
     */
    final Mapper dtoMapper;

    /**
     * URL builder provider for controller. Instance per request.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Html result with secure headers.
     */
    final Provider<Result> htmlWithSecureHeadersProvider;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Constructs controller.
     *
     * @param userService User service.
     * @param countryService Country service.
     * @param urlBuilderProvider URL builder provider.
     * @param htmlWithSecureHeadersProvider HTML with secure headers provider.
     * @param properties Application properties.
     */
    @Inject
    public EditUserController(
            UserService userService,
            CountryService countryService,
            Mapper dtoMapper,
            Provider<UrlBuilder> urlBuilderProvider,
            Provider<Result> htmlWithSecureHeadersProvider,
            NinjaProperties properties) {
        this.userService = userService;
        this.countryService = countryService;
        this.dtoMapper = dtoMapper;
        this.urlBuilderProvider = urlBuilderProvider;
        this.htmlWithSecureHeadersProvider = htmlWithSecureHeadersProvider;
        this.properties = properties;
    }

    @Transactional
    public Result editGet(Context context) {
        Long userId = context.getParameterAs("userId", Long.class);
        User user = userId != null ? userService.get(userId) : null;
        if (user == null) {
            return Results.redirect(urlBuilderProvider.get().getAdminUsersUrl(
                    context.getParameter("query"), context.getParameter("page")));
        }
        return htmlWithSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("context", context)
                .render("user", user)
                .render("config", properties);
    }
}
