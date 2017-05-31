package controllers.sso.admin.users;

import com.google.inject.persist.Transactional;
import controllers.annotations.SecureHtmlHeadersForAdmin;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.filters.XsrfTokenFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import dto.sso.common.Constants;
import models.sso.User;
import models.sso.UserCredentials;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.Validation;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Edit password controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class,
        XsrfTokenFilter.class
})
public class EditPasswordController {

    /**
     * Edit password template.
     */
    static final String TEMPLATE = "views/sso/admin/users/edit-password.ftl.html";

    /**
     * Message id for changed password.
     */
    static final String PASSWORD_CHANGED_MESSAGE = "adminPasswordChanged";

    /**
     * User service.
     */
    final UserService userService;

    /**
     * User event service.
     */
    final UserEventService userEventService;

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
     * Constructs the controller.
     *
     * @param userService User service.
     * @param userEventService User event service.
     * @param urlBuilderProvider Provider for URL builder.
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider for admin.
     * @param properties Application properties.
     */
    @Inject
    public EditPasswordController(
            UserService userService,
            UserEventService userEventService,
            Provider<UrlBuilder> urlBuilderProvider,
            @SecureHtmlHeadersForAdmin Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
        this.properties = properties;
    }

    /**
     * Displays change password form.
     *
     * @param userId User id.
     * @param context Web context.
     * @return Result with form.
     */
    @Transactional
    public Result get(@PathParam("userId") long userId, Context context) {
        User user = userService.get(userId);
        if (user == null) {
            return Controllers.redirect(urlBuilderProvider.get()
                    .getAdminUsersUrl(context.getParameter("query"), context.getParameter("page")));
        }
        return createResult(user, context, Controllers.noViolations());
    }

    /**
     * Changes password for given user and redirects to the list of users.
     *
     * @param userId User id.
     * @param context Web context.
     * @param validation Form validation.
     * @return Form with errors or redirect back to the form in case of success.
     */
    @Transactional
    public Result post(@PathParam("userId") long userId,
                       Context context,
                       Validation validation,
                       FlashScope flashScope) {
        String query = context.getParameter("query");
        String page = context.getParameter("page");

        User user = userService.get(userId);
        if (user == null) {
            return Controllers.redirect(urlBuilderProvider.get().getAdminUsersUrl(query, page));
        }
        String newPassword = context.getParameter("password", "");
        String newPasswordRepeat = context.getParameter("confirmPassword", "");
        if (newPassword.length() < Constants.PASSWORD_MIN_LENGTH ||
                newPassword.length() > Constants.PASSWORD_MAX_LENGTH ||
                !newPassword.equals(newPasswordRepeat)) {
            return createResult(user, context, validation, "password");
        }

        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);

        User admin = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        UserCredentials credentials = userService.getCredentials(user);

        byte[] oldSalt = credentials.getPasswordSalt();
        byte[] oldHash = credentials.getPasswordHash();
        userService.updatePassword(user, newPassword);
        userEventService.onUserPasswordUpdate(admin, user, oldSalt, oldHash, ip, context.getHeaders());

        flashScope.success(PASSWORD_CHANGED_MESSAGE);
        return Controllers.redirect(urlBuilderProvider.get().getAdminEditPasswordUrl(userId, query, page));
    }

    /**
     * Creates response result, validation and field that lead to error.
     *
     * @param user User.
     * @param context Context.
     * @param validation Validation.
     * @param field Field to report as an error.
     * @return Sign up response object.
     */
    Result createResult(User user, Context context, Validation validation, String field) {
        validation.addViolation(new ConstraintViolation(field, field, field));
        return createResult(user, context, validation);
    }

    /**
     * Creates response result.
     *
     * @param user User.
     * @param context Context.
     * @param validation Validation.
     * @return Forgot password response object.
     */
    Result createResult(User user, Context context, Validation validation) {
        User loggedInUser = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        return htmlAdminSecureHeadersProvider.get()
                .render("context", context)
                .render("config", properties)
                .render("errors", validation)
                .render("loggedInUser", loggedInUser)
                .render("userEntity", user)
                .render("query", context.getParameter("query", ""))
                .render("page", context.getParameterAs("page", int.class, 1))
                .template(TEMPLATE);
    }
}

