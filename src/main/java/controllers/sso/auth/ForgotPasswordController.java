package controllers.sso.auth;

import com.google.common.base.Strings;
import controllers.annotations.SecureHtmlHeaders;
import controllers.sso.auth.state.SignInState;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireUnauthenticatedUserFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import dto.sso.ForgotPasswordDto;
import freemarker.template.TemplateException;
import models.sso.User;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenEncryptorException;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Router;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import org.slf4j.Logger;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import services.sso.limits.IPCounterService;
import services.sso.mail.EmailService;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Forgot password controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class,
        AuthenticationFilter.class,
        RequireUnauthenticatedUserFilter.class
})
public class ForgotPasswordController {

    /**
     * Template to render sign up page.
     */
    private static final String TEMPLATE = "views/sso/auth/forgotPassword.ftl.html";

    /**
     * Forgot password email template.
     */
    private static final String FORGOT_PASSWORD_EMAIL_TEMPLATE = "forgotPassword.%s.ftl.html";

    /**
     * Empty forgot password DTO.
     */
    private static final ForgotPasswordDto EMPTY_FORGOT_PASSWORD = new ForgotPasswordDto();

    /**
     * User service.
     */
    private final UserService userService;

    /**
     * Expirable token encryptor.
     */
    private final ExpirableTokenEncryptor expirableTokenEncryptor;

    /**
     * Captcha token service.
     */
    private final CaptchaTokenService captchaTokenService;

    /**
     * IP counter service.
     */
    private final IPCounterService ipCounterService;

    /**
     * Email service.
     */
    private final EmailService emailService;

    /**
     * URL builder provider for controller. Instance per request.
     */
    private final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Html result with secure headers.
     */
    private final Provider<Result> htmlWithSecureHeadersProvider;

    /**
     * Application properties.
     */
    private final NinjaProperties properties;

    /**
     * Application router.
     */
    private final Router router;

    /**
     * Application messages.
     */
    private final Messages messages;

    /**
     * Logger.
     */
    private final Logger logger;

    /**
     * Restore password token time to live, in milliseconds.
     */
    private final long restorePasswordTokenTtl;

    /**
     * Whether to produce fake "email sent" response to non-existing user.
     */
    private final boolean produceFakeResponseForNonExistingUser;

    /**
     * Constructs the forgot password controller.
     *
     * @param userService User service.
     * @param captchaTokenService Captcha token service.
     * @param ipCounterService IP counter service.
     * @param urlBuilderProvider URL builder provider,
     * @param properties Properties.
     * @param router Router.
     * @param messages Messages.
     * @param logger Logger.
     */
    @Inject
    public ForgotPasswordController(
            UserService userService,
            ExpirableTokenEncryptor expirableTokenEncryptor,
            CaptchaTokenService captchaTokenService,
            IPCounterService ipCounterService,
            EmailService emailService,
            Provider<UrlBuilder> urlBuilderProvider,
            @SecureHtmlHeaders Provider<Result> htmlWithSecureHeadersProvider,
            NinjaProperties properties,
            Router router,
            Messages messages,
            Logger logger) {
        this.userService = userService;
        this.expirableTokenEncryptor = expirableTokenEncryptor;
        this.captchaTokenService = captchaTokenService;
        this.ipCounterService = ipCounterService;
        this.emailService = emailService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.htmlWithSecureHeadersProvider = htmlWithSecureHeadersProvider;
        this.properties = properties;
        this.router = router;
        this.messages = messages;
        this.logger = logger;
        this.restorePasswordTokenTtl =
                properties.getIntegerWithDefault("application.sso.restorePasswordToken.ttl", 3600) * 1000L;
        this.produceFakeResponseForNonExistingUser =
                properties.getBooleanWithDefault(
                        "application.sso.restorePassword.fakeResponseForNonExistingUser", true);
    }


    /**
     * Renders forgot password page.
     *
     * @param context Context.
     * @param validation Validation.
     * @return Forgot password page.
     */
    @Transactional
    public Result forgotGet(Context context, Validation validation) {
        return createResult(EMPTY_FORGOT_PASSWORD, context, validation);
    }

    /**
     * Renders forgot password page.
     *
     * @param user User DTO (form).
     * @param context Context.
     * @param validation Validation.
     * @return Forgot password page.
     */
    @Transactional
    public Result forgot(@JSR303Validation ForgotPasswordDto user, Context context, Validation validation) {
        if (validation.hasViolations() || user == null) {
            return createResult(user, context, validation);
        }
        // Verify token.
        try {
            captchaTokenService.verifyCaptchaToken(user.getCaptchaToken(), user.getCaptchaCode());
        } catch (CaptchaTokenService.AlreadyUsedTokenException | CaptchaTokenService.InvalidTokenValueException |
                ExpiredTokenException | IllegalTokenException ex) {
            return createResult(user, context, validation, "captchaCode");
        }
        // Check existing user.
        User userEntity = userService.getUserByEmailOrUsername(user.getEmailOrUsername());
        if (userEntity == null && !produceFakeResponseForNonExistingUser) {
            return createResult(user, context, validation, "emailOrUsernameNotFound");
        }
        if (userEntity != null && !userEntity.isSignInEnabled()) {
            return createResult(user, context, validation, "signInDisabled");
        }
        if (userEntity != null) {
            // Send the email for existing user.
            sendRestorePasswordEmail(userEntity, context);
        }

        String signInUrl = urlBuilderProvider.get().getSignInUrl(SignInState.FORGOT_EMAIL_SENT);
        // Redirect to sign in.
        return Controllers.redirect(signInUrl);
    }

    /**
     * Sends restore password email to given user with given language.
     *
     * @param user User.
     * @param context Context.
     * @throws RuntimeException In case when error happens while creating or sending the email
     */
    private void sendRestorePasswordEmail(User user, Context context) {
        String locale = (String) context.getAttribute(LanguageFilter.LANG);
        try {
            // Create verification token.
            ExpirableToken restorePasswordToken = forgotEmailConfirmationToken(user);
            String restorePasswordTokenAsString = expirableTokenEncryptor.encrypt(restorePasswordToken);

            // Build restore URL password.
            String restorePasswordUrl = urlBuilderProvider.get().getRestorePasswordUrl(restorePasswordTokenAsString);
            logger.info("Sending forgot password url: {}", restorePasswordUrl);

            // Build email template data.
            Map<String, Object> data = new HashMap<>();
            data.put("forgotUrl", restorePasswordUrl);
            data.put("indexUrl", urlBuilderProvider.get().getAbsoluteIndexUrl());
            String subject = messages.get("forgotPasswordSubject", Optional.of(locale)).get();
            String localizedTemplate = String.format(FORGOT_PASSWORD_EMAIL_TEMPLATE, locale);

            // Send the email.
            emailService.send(user.getEmail(), subject, localizedTemplate, data);
        } catch (MessagingException | TemplateException | ExpirableTokenEncryptorException ex) {
            String message = "Error while sending restore password email for user: " + user.getEmail();
            logger.error(message, ex);
        }
    }

    /**
     * Returns restore password token.
     *
     * @param user User to use in token.
     * @return Restore password token.
     */
    private ExpirableToken forgotEmailConfirmationToken(User user) {
        return ExpirableToken.newUserToken(
                ExpirableTokenType.RESTORE_PASSWORD, user.getId(), restorePasswordTokenTtl);
    }

    /**
     * Creates response result with given user, validation and field that lead to error.
     *
     * @param user User to use in response.
     * @param context Context.
     * @param validation Validation.
     * @param field Field to report as an error.
     * @return Forgot password response object.
     */
    private Result createResult(ForgotPasswordDto user, Context context, Validation validation, String field) {
        validation.addViolation(new ConstraintViolation(field, field, field));
        return createResult(user, context, validation);
    }

    /**
     * Creates response result with given user.
     *
     * @param user User to use in response.
     * @param context Context.
     * @param validation Validation.
     * @return Forgot password response object.
     */
    private Result createResult(ForgotPasswordDto user, Context context, Validation validation) {
        Result result = htmlWithSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("context", context)
                .render("user", user)
                .render("config", properties)
                .render("errors", validation)
                .render("continue", urlBuilderProvider.get().getContinueUrlParameter());
        if (Strings.isNullOrEmpty(user.getCaptchaToken()) || validation.hasViolations()) {
            regenerateCaptchaTokenAndUrl(result, context);
        }
        return result;
    }

    /**
     * Adds information about the captcha to given result.
     *
     * @param result Result.
     * @param context Context.
     */
    private void regenerateCaptchaTokenAndUrl(Result result, Context context) {
        String captchaToken = captchaTokenService.newCaptchaToken();
        result.render("captchaToken", captchaToken);
        result.render("captchaUrl", urlBuilderProvider.get().getCaptchaUrl(captchaToken));
    }
}
