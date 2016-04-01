package controllers.sso.auth;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import dto.sso.UserSignUpDto;
import freemarker.template.TemplateException;
import models.sso.User;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import org.dozer.Mapper;
import org.slf4j.Logger;
import services.sso.CaptchaTokenService;
import services.sso.PasswordService;
import services.sso.UserService;
import services.sso.mail.EmailService;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import javax.mail.MessagingException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

/**
 * Sign up controller.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class
})
public class SignUpController {

    /**
     * Empty effectively immutable user DTO.
     */
    static final UserSignUpDto EMPTY_USER = new UserSignUpDto();

    /**
     * Secure random.
     */
    static final Random random = new SecureRandom();

    /**
     * Hit per IP check filter.
     */
    final HitsPerIpCheckFilter hitsPerIpCheckFilter;

    /**
     * Expirable token encryptor.
     */
    final ExpirableTokenEncryptor expirableTokenEncryptor;

    /**
     * User service.
     */
    final UserService userService;

    /**
     * DTO mapper.
     */
    final Mapper dtoMapper;

    /**
     * Email service to send emails with templates.
     */
    final EmailService emailService;

    /**
     * Captcha token service.
     */
    final CaptchaTokenService captchaTokenService;

    /**
     * Password service to generate salts and hashes for newly created user.
     */
    final PasswordService passwordService;

    /**
     * URL builder provider for controller. Instance per request.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Router.
     */
    final Router router;

    /**
     * Language.
     */
    final Lang lang;

    /**
     * Application messages.
     */
    final Messages messages;

    /**
     * Base URL to application, including scheme, domain and port.
     */
    final String baseUrl;

    /**
     * Access token time to live, in millis.
     */
    final long accessTokenTtl;

    /**
     * Email token time to live, in millis.
     */
    final long emailTokenTtl;

    /**
     * Sign up verification token time to live, in millis.
     */
    final long signUpVerificationTokenTtl;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Constructs sign up controller.
     *
     * @param expirableTokenEncryptor Encryptor.
     * @param hitsPerIpCheckFilter Hits per IP filter.
     * @param captchaTokenService Captcha token service.
     * @param passwordService Password service.
     * @param userService User service.
     * @param emailService Email service.
     * @param urlBuilderProvider URL builder provider.
     * @param dtoMapper DTO mapper for user.
     * @param router Router.
     * @param properties Application properties.
     * @param logger Logger.
     * @param lang Language.
     * @param messages Application messages.
     */
    @Inject
    public SignUpController(ExpirableTokenEncryptor expirableTokenEncryptor,
                            HitsPerIpCheckFilter hitsPerIpCheckFilter,
                            CaptchaTokenService captchaTokenService,
                            PasswordService passwordService,
                            UserService userService,
                            EmailService emailService,
                            Provider<UrlBuilder> urlBuilderProvider,
                            Mapper dtoMapper,
                            Router router,
                            NinjaProperties properties,
                            Logger logger,
                            Lang lang,
                            Messages messages) {
        this.hitsPerIpCheckFilter = hitsPerIpCheckFilter;
        this.captchaTokenService = captchaTokenService;
        this.expirableTokenEncryptor = expirableTokenEncryptor;
        this.passwordService = passwordService;
        this.userService = userService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.dtoMapper = dtoMapper;
        this.emailService = emailService;
        this.properties = properties;
        this.router = router;
        this.logger = logger;
        this.lang = lang;
        this.messages = messages;
        this.baseUrl = properties.get("application.baseUrl");
        this.accessTokenTtl = 1000L * properties.
                getIntegerWithDefault("application.sso.accessToken.ttl", 24 * 3600);
        this.emailTokenTtl = 1000L * properties.
                getIntegerWithDefault("application.sso.emailToken.ttl", 24 * 3600);
        this.signUpVerificationTokenTtl = 1000L * properties.
                getIntegerWithDefault("application.sso.signUpVerificationToken.ttl", 30 * 60);
    }

    /**
     * Sign up (GET). Remembers continue URL and shows sign up page.
     *
     * @param context Context.
     * @return Sing up response object.
     */
    public Result signUpGet(Context context) {
        return createResult(EMPTY_USER, context, Controllers.NO_VIOLATIONS);
    }

    /**
     * Sign up action (POST).
     *
     * @param context Context
     * @param user User from the form.
     * @param validation Validation object.
     * @return Redirect to a welcome page with welcome message, project information, link to redirect.
     */
    @Transactional
    public Result signUp(Context context, Validation validation, @JSR303Validation UserSignUpDto user) {
        if (user == null) {
            return signUpGet(context);
        }
        // Validate all fields.
        if (validation.hasViolations()) {
            return createResult(user, context, validation);
        }
        // Compare 2 passwords.
        if (!user.getPassword().equals(user.getPasswordRepeat())) {
            return createResult(user, context, validation, "passwordRepeat");
        }
        // Check if user has correct token/captcha.
        try {
            captchaTokenService.verifyCaptchaToken(user.getToken(), user.getCaptchaCode());
        } catch (CaptchaTokenService.AlreadyUsedTokenException | ExpiredTokenException | IllegalTokenException ex) {
            return createResult(user, context, validation, "captchaCode");
        }
        // Check agreement.
        if (!"agree".equals(user.getAgreement())) {
            return createResult(user, context, validation, "agreement");
        }
        // Check with existing email.
        User existingUserWithEmail = userService.getByEmail(user.getEmail());
        if (existingUserWithEmail != null) {
            return createResult(user, context, validation, "emailDuplicate");
        }
        // Check with existing username.
        User existingUserWithUsername = userService.getByUsername(user.getUsername());
        if (existingUserWithUsername != null) {
            return createResult(user, context, validation, "usernameDuplicate");
        }
        // Save the user.
        User createdUser = userService.createNew(dtoMapper.map(user, User.class), user.getPassword());

        // Perform post-sign up actions.
        String redirectURL = postSignUpActions(createdUser, context);
        // Redirect.
        return Results.redirect(redirectURL);
    }

    /**
     * Set of operations to perform after sign up. If this method throws an exception, transaction will
     * be rolled back. Must return a valid redirect URL  for redirection with appropriate parameters.
     * This method has right to decide whether to redirect directly to resource that requires auth token or
     * required some verification with sign up verification page (email or SMS).
     *
     * @param createdUser Created user.
     * @param context Web context.
     * @return URL to redirect.
     */
    String postSignUpActions(User createdUser, Context context) {
        try {
            String verificationCode = newVerificationCode();
            // Send confirmation email.
            sendConfirmationEmail(createdUser, verificationCode, context);

            ExpirableToken signUpVerificationPageToke = newSignUpPageVerificationToken(createdUser, verificationCode);
            String verificationTokenAsString = expirableTokenEncryptor.encrypt(signUpVerificationPageToke);
            // Redirect to verification page.
            return urlBuilderProvider.get().getSignUpVerificationPage(verificationTokenAsString);
        } catch (PasswordBasedEncryptor.EncryptionException | MessagingException ee) {
            throw new RuntimeException("Unexpected problem with encryption.", ee);
        }
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
    Result createResult(UserSignUpDto user, Context context, Validation validation, String field) {
        validation.addBeanViolation(new FieldViolation(field, ConstraintViolation.create(field)));
        return createResult(user, context, validation);
    }

    /**
     * Creates response result with given user.
     *
     * @param user User to use in response.
     * @param context Context.
     * @param validation Validation.
     * @return Sign up response object.
     */
    Result createResult(UserSignUpDto user, Context context, Validation validation) {
        Result result = Results.html().template("views/auth/SignUpController/signUp.ftl.html");
        result.render("user", user);
        result.render("config", properties);
        result.render("errors", validation);
        result.render("continue", urlBuilderProvider.get().getContinueUrlParameter());
        if (Strings.isNullOrEmpty(user.getToken()) || validation.hasViolations()) {
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
        result.render("token", token);
        result.render("captchaUrl", urlBuilderProvider.get().getCaptchaUrl(token));
    }

    /**
     * Generates new verification code for tokens.
     *
     * @return New verification code for tokens.
     */
    String newVerificationCode() {
        return Integer.toString(100000 + random.nextInt(899999));
    }

    /**
     * Sends confirmation email to given user with given language.
     *
     * @param user Newly registered user.
     * @param verificationCode Verification code.
     * @param context Web application context.
     * @throws PasswordBasedEncryptor.EncryptionException
     * @throws MessagingException
     */
    void sendConfirmationEmail(User user, String verificationCode, Context context)
            throws PasswordBasedEncryptor.EncryptionException, MessagingException {
        ExpirableToken emailConfirmationToken = newEmailVerificationToken(user, verificationCode);
        String langCode = (String) context.getAttribute(LanguageFilter.LANG);
        Map<String, Object> data = Maps.newHashMap();
        data.put("lang", langCode);
        data.put("verificationCode", verificationCode);
        data.put("confirmUrl", urlBuilderProvider.get().
                getEmailConfirmationUrl(expirableTokenEncryptor.encrypt(emailConfirmationToken)));
        String subject = messages.get("confirmationSubject", Optional.<String>of(langCode)).get();
        try {
            String localizedTemplate = String.format("signUpConfirmation.%s.ftl.html", langCode);
            emailService.send(user.getEmail(), subject, localizedTemplate, data);
        } catch (MessagingException | TemplateException ex) {
            throw new MessagingException("Error while sending confirmation email for user: " + user.getEmail(), ex);
        }
    }

    /**
     * Returns new verification token that contains user id, email and verification code to send with email.
     *
     * @param user User to use in token.
     * @param verificationCode Verification code.
     * @return Verification token.
     */
    ExpirableToken newEmailVerificationToken(User user, String verificationCode) {
        Map<String, String> params = Maps.newHashMap();
        params.put("userId", Long.toString(user.getId()));
        params.put("email", user.getEmail());
        params.put("verificationCode", verificationCode);
        return ExpirableToken.newToken(ExpirableTokenType.EMAIL_VERIFICATION, params, emailTokenTtl);
    }

    /**
     * Returns new sign up page verification token. Contains user id, verification code sent by email or SMS.
     *
     * @param user Registered user.
     * @param verificationCode Verification code.
     * @return Sign up page verification token.
     */
    ExpirableToken newSignUpPageVerificationToken(User user, String verificationCode) {
        Map<String, String> params = Maps.newHashMap();
        params.put("userId", Long.toString(user.getId()));
        params.put("verificationCode", verificationCode);
        return ExpirableToken.newToken(ExpirableTokenType.SIGNUP_VERIFICATION, params, signUpVerificationTokenTtl);
    }
}
