package controllers.sso.admin.users;

import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import converters.Converter;
import models.sso.User;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.Validation;
import services.sso.CountryService;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Provider;

/**
 * Edit user data abstract controller with common method and services.
 *
 * @param <C> Converter type.
 * @param <DTO> Data transfer object type.
 */
public abstract class EditUserAbstractController<C extends Converter<User, DTO>, DTO> {

    /**
     * User service.
     */
    protected final UserService userService;

    /**
     * User event service.
     */
    protected final UserEventService userEventService;

    /**
     * Country service.
     */
    protected final CountryService countryService;

    /**
     * Converter.
     */
    protected final C converter;

    /**
     * URL builder provider for controller. Instance per request.
     */
    protected final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Application properties.
     */
    protected final NinjaProperties properties;

    /**
     * Html result with secure headers.
     */
    private final Provider<Result> htmlAdminSecureHeadersProvider;

    /**
     * Constructs the controller.
     *
     * @param userService User service.
     * @param userEventService User event service.
     * @param countryService Coutry service.
     * @param converter Converter.
     * @param urlBuilderProvider Provider for URL builder.
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider for admin.
     * @param properties Application properties.
     */
    public EditUserAbstractController(
            UserService userService,
            UserEventService userEventService,
            CountryService countryService,
            C converter,
            Provider<UrlBuilder> urlBuilderProvider,
            Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.countryService = countryService;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
        this.properties = properties;
        this.converter = converter;
    }

    /**
     * Provides additional validation for Data Transfer Object.
     * <p>
     * Return null if the DTO is valid or return violation id for invalid field in DTO/form.
     *
     * @param user User entity that was updated by this controller.
     * @param dto Data Transfer Object for form.
     * @return Null if the DTO is valid or violation id for invalid field in DTO/form.
     */
    protected abstract String validate(User user, DTO dto);

    /**
     * Returns absolute URL for a redirect after successful form submission.
     *
     * @param user User entity that was updated by this controller.
     * @param context Application context.
     * @return Absolute URL for a redirect after successful form submission.
     */
    protected abstract String getSuccessRedirectUrl(User user, Context context);

    /**
     * Path to application template to be rendered.
     *
     * @return Path to application template to be rendered.
     */
    protected abstract String getTemplate();

    /**
     * Renders edit user data template for given user, logging access or redirects to the list of users if the given
     * user was not found.
     *
     * @param userId User's id whose data to render.
     * @param context Application context.
     * @return Result with rendered template or redirection to the list of users if the user was not found.
     */
    protected final Result renderUserOrRedirectToList(long userId, Context context) {
        User user = userService.get(userId);
        if (user == null) {
            return Results.redirect(urlBuilderProvider.get().getAdminUsersUrl(
                    context.getParameter("query"), context.getParameter("page")));
        }
        this.logAccess(user, context);
        return createResult(converter.fromEntity(user), user, context, Controllers.noViolations());
    }

    /**
     * Updates user entity with data from given Data Transfer Object (form), logging update or redirects to the list of
     * users if the given user was not found.
     *
     * @param userId User's id whose entity to update.
     * @param dto Data Transfer Object to get data from.
     * @param context Application context.
     * @param validation DTO/form validation.
     * @return Result with redirect to the URL provided by {@link #getSuccessRedirectUrl(User, Context)} or redirection
     * to the list of users if the user was not found.
     */
    protected final Result updateUserOrRedirectToList(long userId, DTO dto, Context context, Validation validation) {
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
        // Validate DTO specifics.
        String specificViolation = validate(user, dto);
        if (specificViolation != null) {
            return createResult(dto, user, context, validation, specificViolation);
        }
        // Log update event. Must happen before actual data update to fetch data.
        this.logUpdate(user, context);
        // Map edit DTO to user entity.
        converter.update(user, dto);
        // Update user.
        userService.update(user);
        // Redirect to same form.
        return Results.redirect(getSuccessRedirectUrl(user, context));
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
    private Result createResult(DTO dto, User userEntity, Context ctx, Validation validation, String field) {
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
    private Result createResult(DTO user, User userEntity, Context ctx, Validation validation) {
        return htmlAdminSecureHeadersProvider.get()
                .template(getTemplate())
                .render("context", ctx)
                .render("config", properties)
                .render("errors", validation)
                .render("user", user)
                .render("userEntity", userEntity)
                .render("countries", countryService.getAllSortedByNiceName())
                .render("query", ctx.getParameter("query", ""))
                .render("page", ctx.getParameterAs("page", int.class, 1));
    }

    /**
     * Log access event.
     *
     * @param user User who's information is accessed.
     * @param context Context.
     */
    private void logAccess(User user, Context context) {
        User loggedInUser = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        String remoteIp = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        String currentUrl = urlBuilderProvider.get().getCurrentUrl();
        userEventService.onUserDataAccess(loggedInUser, user, currentUrl, remoteIp, context.getHeaders());
    }

    /**
     * Log update event.
     *
     * @param user User who's information is updated.
     * @param context Context.
     */
    private void logUpdate(User user, Context context) {
        User loggedInUser = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        String remoteIp = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        String currentUrl = urlBuilderProvider.get().getCurrentUrl();
        userEventService.onUserDataUpdate(loggedInUser, user, currentUrl, remoteIp, context.getHeaders());
    }
}
