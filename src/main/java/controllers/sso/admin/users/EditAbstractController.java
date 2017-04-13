package controllers.sso.admin.users;

import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import converters.Converter;
import models.sso.Country;
import models.sso.User;
import models.sso.UserConfirmationState;
import models.sso.UserRole;
import models.sso.UserSignInState;
import ninja.Context;
import ninja.Result;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.Validation;
import services.sso.CountryService;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Provider;
import java.util.List;

/**
 * Edit user data abstract controller with common method and services.
 *
 * @param <C> Converter type.
 * @param <DTO> Data transfer object type.
 */
public abstract class EditAbstractController<C extends Converter<User, DTO>, DTO> {

    /**
     * Message id for changed password.
     */
    protected static final String USER_DATA_SAVED_MESSAGE = "adminUserDataSaved";

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
     * Html result with secure headers.
     */
    private final Provider<Result> htmlAdminSecureHeadersProvider;

    /**
     * Application properties.
     */
    protected final NinjaProperties properties;

    /**
     * Constructs the controller.
     *
     * @param userService User service.
     * @param userEventService User event service.
     * @param countryService Country service.
     * @param converter Converter.
     * @param urlBuilderProvider Provider for URL builder.
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider for admin.
     * @param properties Application properties.
     */
    public EditAbstractController(
            UserService userService,
            UserEventService userEventService,
            CountryService countryService,
            C converter,
            Provider<UrlBuilder> urlBuilderProvider,
            Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.countryService = countryService;
        this.urlBuilderProvider = urlBuilderProvider;
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
            return Controllers.redirect(urlBuilderProvider.get()
                    .getAdminUsersUrl(context.getParameter("query"), context.getParameter("page")));
        }
        User loggedInUser = userService.get((long) context.getAttribute(AuthenticationFilter.USER_ID));
        this.logAccess(user, loggedInUser, context);
        return createResult(converter.fromEntity(user), user, context, Controllers.noViolations());
    }

    /**
     * Updates user entity with data from given Data Transfer Object (form), logging update or redirects to the list of
     * users if the given user was not found.
     *
     * @param userId User's id whose entity to update.
     * @param dto Data Transfer Object to get data from.
     * @param context Application context.
     * @param flashScope Flash scope.
     * @param validation DTO/form validation.
     * @return Result with redirect to the URL provided by {@link #getSuccessRedirectUrl(User, Context)} or redirection
     * to the list of users if the user was not found.
     */
    protected final Result updateUserOrRedirectToList(
            long userId,
            DTO dto,
            Context context,
            FlashScope flashScope,
            Validation validation) {
        // Check existing user.
        User user = userService.get(userId);
        if (user == null) {
            return Controllers.redirect(urlBuilderProvider.get()
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

        // Remote IP.
        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        // Logged in user.
        User loggedInUser = userService.get((long) context.getAttribute(AuthenticationFilter.USER_ID));
        // Log update event. Must happen before actual data update to fetch data.
        this.logUpdate(user, loggedInUser, context);

        // Remember old states and roles.
        UserRole oldRole = user.getRole();
        UserSignInState oldSignInState = user.getSignInState();
        UserConfirmationState oldConfirmationState = user.getConfirmationState();

        // Map edit DTO to user entity.
        converter.update(user, dto);

        // Update user.
        userService.update(user);

        // Produce change events.
        UserRole newRole = user.getRole();
        UserSignInState newSignInState = user.getSignInState();
        UserConfirmationState newConfirmationState = user.getConfirmationState();
        if (!oldRole.equals(newRole)) {
            userEventService.onRoleChange(user, oldRole, loggedInUser, ip, context.getHeaders());
        }
        if (!oldSignInState.equals(newSignInState)) {
            if (UserSignInState.ENABLED.equals(newSignInState)) {
                userEventService.onSignInEnable(user, ip, context.getHeaders());
            } else {
                userEventService.onSignInEnable(user, ip, context.getHeaders());
            }
        }
        if (!oldConfirmationState.equals(newConfirmationState)
                && UserConfirmationState.CONFIRMED.equals(newConfirmationState)) {
            userEventService.onConfirmation(user, ip, context.getHeaders());
        }

        // Set message.
        flashScope.success(USER_DATA_SAVED_MESSAGE);
        // Redirect to same form.
        return Controllers.redirect(getSuccessRedirectUrl(user, context));
    }

    /**
     * Creates response result with given user, validation and field that lead to error.
     *
     * @param dto User to use in response.
     * @param user Original user entity for read-only data.
     * @param ctx Context.
     * @param validation Validation.
     * @param field Field to report as an error.
     * @return Sign up response object.
     */
    private Result createResult(DTO dto, User user, Context ctx, Validation validation, String field) {
        validation.addBeanViolation(new FieldViolation(field, ConstraintViolation.create(field)));
        return createResult(dto, user, ctx, validation);
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
        User loggedInUser = userService.get((long) ctx.getAttribute(AuthenticationFilter.USER_ID));
        String locale = (String) ctx.getAttribute(LanguageFilter.LANG);
        List<Country> countries = "en".equals(locale) ?
                countryService.getAllSortedByName() : countryService.getAllSortedByNativeName();
        return htmlAdminSecureHeadersProvider.get()
                .render("context", ctx)
                .render("config", properties)
                .render("errors", validation)
                .render("loggedInUser", loggedInUser)
                .render("user", user)
                .render("userEntity", userEntity)
                .render("countries", countries)
                .render("roles", UserRole.values())
                .render("signInStates", UserSignInState.values())
                .render("confirmationStates", UserConfirmationState.values())
                .render("query", ctx.getParameter("query", ""))
                .render("page", ctx.getParameterAsInteger("page", 1))
                .template(getTemplate());
    }

    /**
     * Log access event.
     *
     * @param user User who's information is accessed.
     * @param loggedInUser Logged-in user.
     * @param context Context.
     */
    private void logAccess(User user, User loggedInUser, Context context) {
        String remoteIp = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        String currentUrl = urlBuilderProvider.get().getCurrentUrl();
        userEventService.onUserDataAccess(loggedInUser, user, currentUrl, remoteIp, context.getHeaders());
    }

    /**
     * Log update event.
     *
     * @param user User who's information is updated.
     * @param loggedInUser Logged-in user.
     * @param context Context.
     */
    private void logUpdate(User user, User loggedInUser, Context context) {
        String remoteIp = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        String currentUrl = urlBuilderProvider.get().getCurrentUrl();
        userEventService.onUserDataUpdate(loggedInUser, user, currentUrl, remoteIp, context.getHeaders());
    }
}
