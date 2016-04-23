package controllers.sso.auth;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import dto.sso.UserSignUpDto;
import freemarker.template.TemplateException;
import models.sso.Country;
import models.sso.User;
import models.sso.UserGender;
import models.sso.UserRole;
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
import services.sso.CountryService;
import services.sso.PasswordService;
import services.sso.UserService;
import services.sso.mail.EmailService;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import java.security.SecureRandom;
import java.time.LocalDate;
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
     * Template to render sign up page.
     */
    static final String TEMPLATE = "views/sso/auth/signUp.ftl.html";

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
     * Country service.
     */
    final CountryService countryService;

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
     * @param countryService Country service.
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
                            CountryService countryService,
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
        this.countryService = countryService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.dtoMapper = dtoMapper;
        this.emailService = emailService;
        this.properties = properties;
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
        return createResult(EMPTY_USER, context, Controllers.noViolations());
    }

    /**
     * Sign up action (POST).
     *
     * @param context Context
     * @param userDto User from the form.
     * @param validation Validation object.
     * @return Redirect to a welcome page with welcome message, project information, link to redirect.
     */
    @Transactional
    public Result signUp(Context context, Validation validation, @JSR303Validation UserSignUpDto userDto) {
        if (userDto == null) {
            return signUpGet(context);
        }
        // Validate all fields.
        if (validation.hasViolations()) {
            return createResult(userDto, context, validation);
        }
        // Gender.
        if (!UserGender.hasConstant(userDto.getGender())) {
            return createResult(userDto, context, validation, "gender");
        }
        // Compare 2 passwords.
        if (!userDto.getPassword().equals(userDto.getPasswordRepeat())) {
            return createResult(userDto, context, validation, "passwordRepeat");
        }
        // Check agreement.
        if (!"agree".equals(userDto.getAgreement())) {
            return createResult(userDto, context, validation, "agreement");
        }
        // Check if user has correct token/captcha.
        try {
            captchaTokenService.verifyCaptchaToken(userDto.getToken(), userDto.getCaptchaCode());
        } catch (CaptchaTokenService.AlreadyUsedTokenException | ExpiredTokenException | IllegalTokenException ex) {
            return createResult(userDto, context, validation, "captchaCode");
        }
        // Check with existing username.
        User existingUserWithUsername = userService.getByUsername(userDto.getUsername());
        if (existingUserWithUsername != null) {
            return createResult(userDto, context, validation, "usernameDuplicate");
        }
        // Check with existing email.
        User existingUserWithEmail = userService.getByEmail(userDto.getEmail());
        if (existingUserWithEmail != null) {
            return createResult(userDto, context, validation, "emailDuplicate");
        }
        // Fetch country.
        Country country = countryService.get(userDto.getCountryId());
        if (country == null) {
            return createResult(userDto, context, validation, "country");
        }
        // User to save.
        User userToSave = dtoMapper.map(userDto, User.class);
        userToSave.setCountry(country);
        userToSave.setGender(UserGender.valueOf(userDto.getGender()));
        userToSave.setDateOfBirth(LocalDate.of(userDto.getBirthYear(), userDto.getBirthMonth(), userDto.getBirthDay()));
        userToSave.setRole(UserRole.USER);
        // Remote IP.
        String remoteIP = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        // Save the user.
        userService.createNew(userToSave, userDto.getPassword(), remoteIP);
        // Perform post-sign up actions.
        String redirectURL = invokePostSignUpActions(userToSave, context);
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
    String invokePostSignUpActions(User createdUser, Context context) {
        try {
            String verificationCode = newVerificationCode();
            // Send verification email with code.
            sendConfirmationEmail(createdUser, verificationCode, context);
            // Create new verification token.
            ExpirableToken signUpVerificationPageToken = newSignUpPageVerificationToken(createdUser, verificationCode);
            String verificationTokenAsString = expirableTokenEncryptor.encrypt(signUpVerificationPageToken);
            // Redirect to verification page.
            return urlBuilderProvider.get().getSignUpVerificationPage(verificationTokenAsString);
        } catch (PasswordBasedEncryptor.EncryptionException e) {
            throw new RuntimeException("Unexpected problem with encryption.", e);
        } catch (MessagingException e) {
            throw new RuntimeException("Problem while sending an email.", e);
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
        Result result = Results.html().template(TEMPLATE)
                .render("user", user)
                .render("config", properties)
                .render("errors", validation)
                .render("countries", countryService.getAllSortedByNiceName())
                .render("continue", urlBuilderProvider.get().getContinueUrlParameter());
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
     * @throws MessagingException In case when error happens while creating or sending the email.
     */
    void sendConfirmationEmail(User user, String verificationCode, Context context) throws MessagingException {
        String locale = (String) context.getAttribute(LanguageFilter.LANG);
        try {
            // Create verification token.
            ExpirableToken emailConfirmationToken = newEmailVerificationToken(user, verificationCode);
            String emailTokenAsString = expirableTokenEncryptor.encrypt(emailConfirmationToken);
            // Build email template data.
            Map<String, Object> data = Maps.newHashMap();
            data.put("lang", locale);
            data.put("verificationCode", verificationCode);
            data.put("confirmUrl", urlBuilderProvider.get().getEmailConfirmationUrl(emailTokenAsString));
            // Translate subject and build template.
            String subject = messages.get("confirmationSubject", Optional.<String>of(locale)).get();
            String localizedTemplate = String.format("signUpConfirmation.%s.ftl.html", locale);
            // Send the email.
            emailService.send(user.getEmail(), subject, localizedTemplate, data);
        } catch (MessagingException | TemplateException | PasswordBasedEncryptor.EncryptionException ex) {
            String message = "Error while sending confirmation email for user: " + user.getEmail();
            logger.error(message, ex);
            throw new MessagingException(message, ex);
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
