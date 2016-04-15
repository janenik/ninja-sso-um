package controllers.sso.auth;

import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import dto.sso.UserSignInDto;
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

/**
 * Sign in controller.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class
})
public class SignInController {

    /**
     * Template to render sign up page.
     */
    static final String TEMPLATE = "views/sso/auth/signIn.ftl.html";

    /**
     * Empty effectively immutable user DTO.
     */
    static UserSignInDto EMPTY_USER = new UserSignInDto();

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
    final Messages messages;
    final Logger logger;

    /**
     * Constructs sign in controller.
     *
     * @param userService User service.
     * @param captchaTokenService Captcha token service.
     * @param urlBuilderProvider URL builder provider.
     * @param properties Application properties./
     * @param router Router.
     * @param messages Messages.
     * @param logger Logger.
     */
    @Inject
    public SignInController(UserService userService,
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
        this.logger = logger;
        this.router = router;
        this.messages = messages;
    }

    /**
     * Sign in (GET). Remembers continue URL and shows sign up page.
     *
     * @param context Context.
     * @return Sing up response object.
     */
    public Result signInGet(Context context) {
        return createResult(EMPTY_USER, context, Controllers.noViolations());
    }

    /**
     * Sign in (POST).
     *
     * @param context Application context.
     * @param validation Validation object.
     * @param userSignInDto User sign in DTO.
     * @return Result of sign in.
     */
    @Transactional
    public Result signIn(Context context, Validation validation, @JSR303Validation UserSignInDto userSignInDto) {
        if (userSignInDto == null) {
            return signInGet(context);
        }
        if (validation.hasViolations()) {
            return createResult(userSignInDto, context, validation);
        }
        // Check abuse and if abuse suspect check the entered captcha code.
        boolean abuse = context.getAttribute(AbuseCheckFilter.ABUSE, Boolean.class);
        if ((abuse || authenticationTokenService.isAlwaysAsked())) {
            try {
                authenticationTokenService.verifyCaptchaToken(userSignInDto.getToken(), userSignInDto.getCaptchaCode());
                // Invalidate token.
                authenticationTokenService.invalidateToken(userSignInDto.getToken());
            } catch (CaptchaAuthenticationTokenService.AlreadyUsedTokenException | ExpiredTokenException |
                    IllegalTokenException ex) {
                return createResult(userSignInDto, context, validation, "captchaCode");
            }
        }
        User user = userService.getUserByEmailOrUsername(userSignInDto.getEmailOrUsername());
        if (user == null || !userService.isValidPassword(user, userSignInDto.getPassword())) {
            return createResult(userSignInDto, context, validation, "emailOrPassword");
        }
        if (!user.isConfirmed()) {
            return createResult(userSignInDto, context, validation, "emailNotConfirmed");
        }
        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        userService.updateSignInTime(user, ip);
        return Results.redirect(Controllers.getProjectRedirectUrl(redirectUrl, context, project, session));
    }

    /**
     * Creates response result with given user.
     *
     * @param user User to use in response.
     * @param context Context.
     * @param validation Validation.
     * @return Sign in response object.
     */
    Result createResult(UserSignInDto user, Context context, Validation validation) {
        String langCode = (String) context.getAttribute(LanguageFilter.LANG);
        Result result = Results.html().template(TEMPLATE);
        result.render("user", user);
        result.render("errors", validation);
        result.render("ipHitsExceeded", context.getAttribute(HitsPerIpCheckFilter.HITS_PER_IP_LIMIT_EXCEEDED));
        result.render("continue", Controllers.getRedirectUrlParameter(context, properties));
        result.render("config", properties);
        if (isAbuse) {
            regenerateCaptchaTokenAndUrl(result, context);
        }
        if (status != null) {
            result.render("status", status.getMessage(messages, langCode));
            result.render("statusSuccess", status.isSuccessful());
        }
        return result;
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
    Result createResult(UserSignInDto user, Context context, Validation validation, String field) {
        validation.addBeanViolation(new FieldViolation(field, ConstraintViolation.create(field)));
        return createResult(user, context, validation);
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
        result.render("captchaUrl", CaptchaController.getRoute(basePath, context, router, token));
    }
}