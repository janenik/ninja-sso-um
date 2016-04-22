package controllers.sso.auth;

import com.google.common.base.Strings;
import controllers.sso.auth.state.SignInState;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.web.UrlBuilder;
import dto.sso.ForgotPasswordDto;
import models.sso.User;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import org.slf4j.Logger;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import services.sso.limits.IPCounterService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.transaction.Transactional;

/**
 * Forgot password controller.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class
})
public class ForgotPasswordController {

    /**
     * Template to render sign up page.
     */
    static final String TEMPLATE = "views/sso/auth/forgotPassword.ftl.html";

    /**
     * Empty forgot password DTO.
     */
    static final ForgotPasswordDto EMPTY_FORGOT_PASSWORD = new ForgotPasswordDto();

    /**
     * User service.
     */
    final UserService userService;

    /**
     * Captcha token service.
     */
    final CaptchaTokenService captchaTokenService;

    /**
     * IP counter service.
     */
    final IPCounterService ipCounterService;

    /**
     * URL builder provider for controller. Instance per request.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Application router.
     */
    final Router router;

    /**
     * Application messages.
     */
    final Messages messages;

    /**
     * Logger.
     */
    final Logger logger;

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
            CaptchaTokenService captchaTokenService,
            IPCounterService ipCounterService,
            Provider<UrlBuilder> urlBuilderProvider,
            NinjaProperties properties,
            Router router,
            Messages messages,
            Logger logger) {
        this.userService = userService;
        this.captchaTokenService = captchaTokenService;
        this.ipCounterService = ipCounterService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.properties = properties;
        this.router = router;
        this.messages = messages;
        this.logger = logger;
    }


    /**
     * Renders forgot password page.
     *
     * @param context Context.
     * @param validation Validation.
     * @return Forgot password page.
     */
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

        try {
            captchaTokenService.verifyCaptchaToken(user.getCaptchaToken(), user.getCaptchaCode());
        } catch (CaptchaTokenService.AlreadyUsedTokenException | ExpiredTokenException |
                IllegalTokenException ex) {
            return createResult(user, context, validation, "captchaCode");
        }

        User userEntity = userService.getUserByEmailOrUsername(user.getEmailOrUsername());
        if (userEntity == null) {
            return createResult(user, context, validation, "emailNotFound");
        }
        //sendForgotPasswordEmail(userEntity, context);

        return Results.redirect(urlBuilderProvider.get().
                getSignInUrl(SignInState.FORGOT_EMAIL_SENT.toString()));
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
    Result createResult(ForgotPasswordDto user, Context context, Validation validation, String field) {
        validation.addBeanViolation(new FieldViolation(field, ConstraintViolation.create(field)));
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
    Result createResult(ForgotPasswordDto user, Context context, Validation validation) {
        Result result = Results.html().template(TEMPLATE);
        result.render("user", user);
        result.render("config", properties);
        result.render("errors", validation);
        result.render("continue", urlBuilderProvider.get().getContinueUrlParameter());
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
    void regenerateCaptchaTokenAndUrl(Result result, Context context) {
        String token = captchaTokenService.newCaptchaToken();
        result.render("captchaToken", token);
        result.render("captchaUrl", urlBuilderProvider.get().getCaptchaUrl(token));
    }
}
