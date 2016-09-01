package controllers.sso.admin;

import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import dto.sso.admin.UserEditDto;
import models.sso.User;
import models.sso.UserGender;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import org.dozer.Mapper;
import services.sso.CountryService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.LocalDate;

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
    static final String TEMPLATE = "views/sso/admin/users/edit-personal.ftl.html";

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
    final Provider<Result> htmlAdminSecureHeadersProvider;

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
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider for admin.
     * @param properties Application properties.
     */
    @Inject
    public EditUserController(
            UserService userService,
            CountryService countryService,
            Mapper dtoMapper,
            Provider<UrlBuilder> urlBuilderProvider,
            @Named("htmlAdminSecureHeaders") Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        this.userService = userService;
        this.countryService = countryService;
        this.dtoMapper = dtoMapper;
        this.urlBuilderProvider = urlBuilderProvider;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
        this.properties = properties;
    }

    @Transactional
    public Result editGet(@PathParam("userId") long userId, Context context) {
        User user = userService.get(userId);
        if (user == null) {
            return Results.redirect(urlBuilderProvider.get().getAdminUsersUrl(
                    context.getParameter("query"), context.getParameter("page")));
        }
        UserEditDto editDto = dtoMapper.map(user, UserEditDto.class);
        editDto.setBirthDay(user.getDateOfBirth().getDayOfMonth());
        editDto.setBirthMonth(user.getDateOfBirth().getMonthValue());
        editDto.setBirthYear(user.getDateOfBirth().getYear());
        return createEditPersonalResult(editDto, context, Controllers.noViolations());
    }

    @Transactional
    public Result edit(@PathParam("userId") long userId, Context context, Validation validation,
                       @JSR303Validation UserEditDto editDto) {
        // Check existing user.
        User user = userService.get(userId);
        if (user == null) {
            return Results.redirect(urlBuilderProvider.get()
                    .getAdminUsersUrl(context.getParameter("query"), context.getParameter("page")));
        }
        // Validate all fields.
        if (validation.hasViolations()) {
            return createEditPersonalResult(editDto, context, validation);
        }
        // Check with existing username.
        User existingUserWithUsername = userService.getByUsername(editDto.getUsername());
        if (existingUserWithUsername != null && !existingUserWithUsername.equals(user)) {
            return createEditPersonalResult(editDto, context, validation, "usernameDuplicate");
        }
        // Map edit DTO to user entity.
        dtoMapper.map(editDto, user);
        user.setGender(UserGender.valueOf(editDto.getGender()));
        user.setDateOfBirth(LocalDate.of(editDto.getBirthYear(), editDto.getBirthMonth(), editDto.getBirthDay()));
        // Update user.
        userService.update(user);
        // Redirect to same form.
        return Results.redirect(urlBuilderProvider.get()
                .getAdminEditPersonalUrl(user.getId(), context.getParameter("query"), context.getParameter("page")));
    }

    /**
     * Creates response result with given user, validation and field that lead to error.
     *
     * @param user User to use in response.
     * @param context Context.
     * @param validation Validation.
     * @param field Field to report as an error.
     * @return Sign up response object.
     */
    Result createEditPersonalResult(UserEditDto user, Context context, Validation validation, String field) {
        validation.addBeanViolation(new FieldViolation(field, ConstraintViolation.create(field)));
        return createEditPersonalResult(user, context, validation);
    }

    /**
     * Creates response result with given user.
     *
     * @param user User to use in response.
     * @param context Context.
     * @param validation Validation.
     * @return Sign up response object.
     */
    Result createEditPersonalResult(UserEditDto user, Context context, Validation validation) {
        return htmlAdminSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("context", context)
                .render("config", properties)
                .render("errors", validation)
                .render("user", user)
                .render("query", context.getParameter("query", ""))
                .render("page", context.getParameterAs("page", int.class, 1));
    }
}
