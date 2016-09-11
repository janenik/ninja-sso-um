package controllers.sso.admin.users;

import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import converters.sso.admin.users.EditUserContactDataConverter;
import dto.sso.admin.users.UserEditContactDataDto;
import models.sso.Country;
import models.sso.User;
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
import services.sso.CountryService;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Edit user personal data controller.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class EditUserContactDataController extends EditUserAbstractController {

    /**
     * Template to render users' list page.
     */
    static final String TEMPLATE = "views/sso/admin/users/edit-contact.ftl.html";

    /**
     * Country service.
     */
    final CountryService countryService;

    /**
     * Contact data converter.
     */
    final EditUserContactDataConverter converter;

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
     * @param userEventService User event service.
     * @param converter Contact data converter.
     * @param urlBuilderProvider URL builder provider.
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider for admin.
     * @param properties Application properties.
     */
    @Inject
    public EditUserContactDataController(
            UserService userService,
            CountryService countryService,
            UserEventService userEventService,
            EditUserContactDataConverter converter,
            Provider<UrlBuilder> urlBuilderProvider,
            @Named("htmlAdminSecureHeaders") Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        super(userService, userEventService, urlBuilderProvider);
        this.countryService = countryService;
        this.converter = converter;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
        this.properties = properties;
    }

    @Transactional
    public Result get(@PathParam("userId") long userId, Context context) {
        User user = userService.get(userId);
        if (user == null) {
            return Results.redirect(urlBuilderProvider.get().getAdminUsersUrl(
                    context.getParameter("query"), context.getParameter("page")));
        }
        this.logAccess(user, context);
        return createResult(converter.fromEntity(user), user, context, Controllers.noViolations());
    }

    @Transactional
    public Result post(
            @PathParam("userId") long userId,
            Context context,
            Validation validation,
            @JSR303Validation UserEditContactDataDto dto) {
        // Check existing user.
        User user = userService.get(userId);
        if (user == null) {
            return Results.redirect(urlBuilderProvider.get()
                    .getAdminUsersUrl(context.getParameter("query"), context.getParameter("page")));
        }
        // Validate all fields.
        if (validation.hasViolations()) {
            return createResult(dto, user, context, validation);
        }
        // Check for existing email.
        User existingUserWithEmail = userService.getByEmail(dto.getEmail());
        if (existingUserWithEmail != null && !existingUserWithEmail.equals(user)) {
            return createResult(dto, user, context, validation, "emailDuplicate");
        }
        // Fetch country.
        Country country = countryService.get(dto.getCountryId());
        if (country == null) {
            return createResult(dto, user, context, validation, "country");
        }
        // Log update event. Must happen before actual data update to fetch data.
        this.logUpdate(user, context);
        // Map edit DTO to user entity.
        converter.update(user, dto);
        // Update user.
        userService.update(user);
        // Redirect to same form.
        return Results.redirect(urlBuilderProvider.get()
                .getAdminEditContactDataUrl(user.getId(), context.getParameter("query"), context.getParameter("page")));
    }

    /**
     * Creates response result with given user, validation and field that lead to error.
     *
     * @param dto User to use in response.
     * @param userEntity Original user entity for read-only data.
     * @param ctx Context.
     * @param validation Validation.
     * @param field Field to report as an error.
     * @return Sign up response object.
     */
    Result createResult(UserEditContactDataDto dto, User userEntity, Context ctx, Validation validation, String field) {
        validation.addBeanViolation(new FieldViolation(field, ConstraintViolation.create(field)));
        return createResult(dto, userEntity, ctx, validation);
    }

    /**
     * Creates response result with given user.
     *
     * @param user User to use in response.
     * @param userEntity Original user entity for read-only data.
     * @param ctx Context.
     * @param validation Validation.
     * @return Sign up response object.
     */
    Result createResult(UserEditContactDataDto user, User userEntity, Context ctx, Validation validation) {
        return htmlAdminSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("context", ctx)
                .render("config", properties)
                .render("errors", validation)
                .render("user", user)
                .render("userEntity", userEntity)
                .render("countries", countryService.getAllSortedByNiceName())
                .render("query", ctx.getParameter("query", ""))
                .render("page", ctx.getParameterAs("page", int.class, 1));
    }
}
