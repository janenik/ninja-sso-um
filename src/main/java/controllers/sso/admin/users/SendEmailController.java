package controllers.sso.admin.users;

import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import controllers.annotations.SecureHtmlHeadersForAdmin;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.Validation;
import services.sso.UserService;
import services.sso.mail.EmailService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Send email controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class SendEmailController {

    /**
     * Template to render users' list page.
     */
    static final String TEMPLATE = "views/sso/admin/users/send-email.ftl.html";

    /**
     * Message id for email sent.
     */
    private static final String EMAIL_SENT_MESSAGE = "adminEditUserSendEmailSent";

    /**
     * User service.
     */
    final UserService userService;

    /**
     * Email service.
     */
    final EmailService emailService;

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
     * Creates send email controller.
     *
     * @param userService User service.
     * @param emailService Email service.
     * @param dateTimeFormatter Date time formatter.
     * @param urlBuilderProvider URL builder provider.
     * @param htmlAdminSecureHeadersProvider HTML result provider with secure headers for admin.
     * @param properties Application properties.
     */
    @Inject
    public SendEmailController(
            UserService userService,
            EmailService emailService,
            DateTimeFormatter dateTimeFormatter,
            Provider<UrlBuilder> urlBuilderProvider,
            @SecureHtmlHeadersForAdmin Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        this.userService = userService;
        this.emailService = emailService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.dateTimeFormatter = dateTimeFormatter;
        this.properties = properties;
        this.htmlAdminSecureHeadersProvider = htmlAdminSecureHeadersProvider;
    }

    /**
     * Renders send email form.
     *
     * @param userId User id.
     * @param context Request context.
     * @return Rendered email form.
     */
    @Transactional
    public Result get(@PathParam("userId") long userId, Context context) {
        String query = Strings.nullToEmpty(context.getParameter("query")).trim();
        int page = Math.max(1, context.getParameterAsInteger("page", 1));
        User target = userService.get(userId);
        if (target == null) {
            return Results.redirect(urlBuilderProvider.get().getAdminUsersUrl(query, page));
        }
        return createResult(target, context, Controllers.noViolations());
    }

    /**
     * Validates and sends email to the given user.
     *
     * @param userId User id.
     * @param context Request context.
     * @param validation Validation.
     * @param flashScope Flash scope,
     * @return Form with errors or redirect back to the form in case of success.
     */
    @Transactional
    public Result post(@PathParam("userId") long userId,
                       Context context,
                       Validation validation,
                       FlashScope flashScope) throws IOException, MessagingException {
        String query = context.getParameter("query");
        String page = context.getParameter("page");
        User user = userService.get(userId);
        if (user == null) {
            return Results.redirect(urlBuilderProvider.get().getAdminUsersUrl(query, page));
        }

        String subject = context.getParameter("subject", "").trim();
        String body = context.getParameter("body", "").trim();
        if (subject.isEmpty()) {
            return createPostResult(user, context, validation, "subject", subject, body);
        }
        if (body.isEmpty()) {
            return createPostResult(user, context, validation, "body", subject, body);
        }
        emailService.send(user.getEmail(), subject, body);
        flashScope.success(EMAIL_SENT_MESSAGE);
        return Results.redirect(urlBuilderProvider.get().getAdminSendEmailUrl(userId, query, page));
    }

    /**
     * Creates response result.
     *
     * @param user User.
     * @param context Context.
     * @param validation Validation.
     * @param emailData Current email subject and body.
     * @return Forgot password response object.
     */
    Result createResult(User user, Context context, Validation validation, String... emailData) {
        return htmlAdminSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("now", ZonedDateTime.now(ZoneId.of("UTC")))
                .render("context", context)
                .render("config", properties)
                .render("errors", validation)
                .render("userEntity", user)
                .render("subject", emailData != null && emailData.length > 1 ? emailData[0] : "")
                .render("body", emailData != null && emailData.length > 1 ? emailData[1] : "")
                .render("query", context.getParameter("query", ""))
                .render("page", context.getParameterAs("page", int.class, 1))
                .render("dateTimeFormatter", dateTimeFormatter);
    }

    /**
     * Creates response result, validation and field that lead to error.
     *
     * @param user User.
     * @param context Context.
     * @param validation Validation.
     * @param errorField Field to report as an error.
     * @param emailData Current email subject and body.
     * @return Sign up response object.
     */
    Result createPostResult(User user, Context context, Validation validation, String errorField, String... emailData) {
        validation.addBeanViolation(new FieldViolation(errorField, ConstraintViolation.create(errorField)));
        return createResult(user, context, validation, emailData);
    }
}
