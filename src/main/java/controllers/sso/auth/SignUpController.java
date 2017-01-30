package controllers.sso.auth;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import controllers.annotations.SecureHtmlHeaders;
import controllers.sso.auth.state.SignInState;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.DeviceTypeFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireUnauthenticatedUserFilter;
import controllers.sso.web.Controllers;
import controllers.sso.web.UrlBuilder;
import dto.sso.UserSignUpDto;
import freemarker.template.TemplateException;
import models.sso.Country;
import models.sso.User;
import models.sso.UserGender;
import models.sso.UserRole;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenEncryptorException;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
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
import services.sso.UserEventService;
import services.sso.UserService;
import services.sso.mail.EmailService;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Sign up controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class,
        DeviceTypeFilter.class,
        AuthenticationFilter.class,
        RequireUnauthenticatedUserFilter.class
})
public class SignUpController {

    /**
     * Template to render sign up page.
     */
    static final String TEMPLATE = "views/sso/auth/signUp.ftl.html";

    /**
     * Verification email template pattern.
     */
    static final String EMAIL_VERIFICATION_TEMPLATE = "signUpConfirmation.%s.ftl.html";

    /**
     * Welcome email template pattern.
     */
    static final String EMAIL_WELCOME_TEMPLATE = "signUpWelcome.%s.ftl.html";

    /**
     * Empty user DTO for get request. Effectively immutable.
     */
    static final UserSignUpDto EMPTY_USER = new UserSignUpDto();

    /**
     * Secure random.
     */
    static final Random random = new SecureRandom();

    /**
     * Expirable token encryptor.
     */
    final ExpirableTokenEncryptor expirableTokenEncryptor;

    /**
     * User service.
     */
    final UserService userService;

    /**
     * User's event service.
     */
    final UserEventService userEventService;

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
     * URL builder provider for controller. Instance per request.
     */
    final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Html result with secure headers.
     */
    final Provider<Result> htmlWithSecureHeadersProvider;

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
     * Minimum registration age.
     */
    final int minimumRegistrationAge;

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
     * Email notification type.
     */
    final EmailNotificationType emailNotificationType;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Constructs sign up controller.
     *
     * @param expirableTokenEncryptor Encryptor.
     * @param captchaTokenService Captcha token service.
     * @param userService User service.
     * @param userEventService User's event service.
     * @param countryService Country service.
     * @param emailService Email service.
     * @param urlBuilderProvider URL builder provider.
     * @param dtoMapper DTO mapper for user.
     * @param properties Application properties.
     * @param logger Logger.
     * @param lang Language.
     * @param messages Application messages.
     */
    @Inject
    public SignUpController(ExpirableTokenEncryptor expirableTokenEncryptor,
                            CaptchaTokenService captchaTokenService,
                            UserService userService,
                            UserEventService userEventService,
                            CountryService countryService,
                            EmailService emailService,
                            Provider<UrlBuilder> urlBuilderProvider,
                            @SecureHtmlHeaders Provider<Result> htmlWithSecureHeadersProvider,
                            Mapper dtoMapper,
                            NinjaProperties properties,
                            Logger logger,
                            Lang lang,
                            Messages messages) {
        this.captchaTokenService = captchaTokenService;
        this.expirableTokenEncryptor = expirableTokenEncryptor;
        this.userService = userService;
        this.userEventService = userEventService;
        this.countryService = countryService;
        this.urlBuilderProvider = urlBuilderProvider;
        this.htmlWithSecureHeadersProvider = htmlWithSecureHeadersProvider;
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
        this.minimumRegistrationAge = properties
                .getIntegerWithDefault("application.sso.minimumRegistrationAge", 13);
        this.emailNotificationType =
                EmailNotificationType.fromString(
                        properties.getWithDefault("application.sso.signUpEmailNotificationType",
                                EmailNotificationType.CONFIRMATION.toString()));
    }

    /**
     * Sign up (GET). Remembers continue URL and shows sign up page.
     *
     * @param context Context.
     * @return Sing up response object.
     */
    @Transactional
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

        // Check if the birthday is a valid date.
        if (!userDto.isValidBirthday()) {
            return createResult(userDto, context, validation, "birthDay");
        }

        // Check if the user is old enough.
        LocalDate birthDate = userDto.getLocalDateBirthday();
        if (birthDate.plusYears(minimumRegistrationAge).isAfter(LocalDate.now())) {
            return createResult(userDto, context, validation, "age");
        }

        // Check if user has correct token/captcha.
        try {
            captchaTokenService.verifyCaptchaToken(userDto.getToken(), userDto.getCaptchaCode());
        } catch (CaptchaTokenService.AlreadyUsedTokenException | CaptchaTokenService.InvalidTokenValueException |
                ExpiredTokenException | IllegalTokenException ex) {
            return createResult(userDto, context, validation, "captchaCode");
        }

        // Check username is acceptable.
        if (!userService.isUsernameAcceptable(userDto.getUsername())) {
            return createResult(userDto, context, validation, "usernameDuplicate");
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
        userToSave.setDateOfBirth(birthDate);
        userToSave.setRole(UserRole.USER);
        userToSave.setLastUsedLocale((String) context.getAttribute(LanguageFilter.LANG));

        // Make user confirmed in case of welcome or no email.
        if (!EmailNotificationType.CONFIRMATION.equals(this.emailNotificationType)) {
            userToSave.confirm();
        }

        // Save the user.
        userService.createNew(userToSave, userDto.getPassword());

        // Remote IP.
        String remoteIp = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        userEventService.onUserSignUp(userToSave, remoteIp, context.getHeaders());

        // Perform post-sign up actions.
        String redirectURL = invokePostSignUpActions(userToSave, context);

        // Redirect.
        return Controllers.redirect(redirectURL);
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
        // Make user confirmed in case of no email.
        if (EmailNotificationType.NONE.equals(this.emailNotificationType)) {
            return urlBuilderProvider.get().getSignInUrl(SignInState.SUCCESSFUL_SIGN_UP);
        }
        try {
            if (EmailNotificationType.WELCOME.equals(this.emailNotificationType)) {
                sendSignUpNotification(createdUser, context, Optional.absent());
                return urlBuilderProvider.get().getSignInUrl(SignInState.SUCCESSFUL_SIGN_UP);
            }
            return sendConfirmationEmailAndBuildVerificationPageUrl(createdUser, context);
        } catch (ExpirableTokenEncryptorException e) {
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
        String locale = (String) context.getAttribute(LanguageFilter.LANG);
        List<Country> countries = "en".equals(locale) ?
                countryService.getAllSortedByName() : countryService.getAllSortedByNativeName();
        Result result = htmlWithSecureHeadersProvider.get()
                .template(TEMPLATE)
                .render("context", context)
                .render("user", user)
                .render("config", properties)
                .render("errors", validation)
                .render("countries", countries)
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
     * Sends confirmation  email and constructs URL to verification page.
     *
     * @param createdUser Created user.
     * @param context Web application context.
     * @return Verification page URL for redirection.
     * @throws MessagingException In case of email exception.
     * @throws ExpirableTokenEncryptorException Encryption exception.
     */
    private String sendConfirmationEmailAndBuildVerificationPageUrl(User createdUser, Context context)
            throws MessagingException, ExpirableTokenEncryptorException {
        String verificationCode = newVerificationCode();

        // Send verification or welcome email with code.
        sendSignUpNotification(createdUser, context, Optional.of(verificationCode));

        // Create new verification token.
        ExpirableToken signUpVerificationPageToken = newSignUpPageVerificationToken(createdUser, verificationCode);
        String verificationTokenAsString = expirableTokenEncryptor.encrypt(signUpVerificationPageToken);

        // Redirect to verification page.
        return urlBuilderProvider.get().getSignUpVerificationPage(verificationTokenAsString);
    }

    /**
     * Sends sign up notification email to given user with given language. If the verification code is given
     * then confirmation email is sent. Otherwise welcome email is sent.
     *
     * @param user Newly registered user.
     * @param context Web application context.
     * @param verificationCode Optional verification code for confirmation email.
     * @throws MessagingException In case when error happens while creating or sending the email.
     */
    void sendSignUpNotification(User user, Context context, Optional<String> verificationCode)
            throws MessagingException {
        String locale = (String) context.getAttribute(LanguageFilter.LANG);
        try {
            // Build email template data.
            Map<String, Object> data = new HashMap<>();
            data.put("lang", locale);
            data.put("user", user);
            data.put("verificationCode", verificationCode.get());
            data.put("indexUrl", urlBuilderProvider.get().getAbsoluteIndexUrl());
            data.put("signInUrl", urlBuilderProvider.get().getAbsoluteSignInUrl());

            // Translate subject and build template.
            String subject;
            String localizedTemplate;
            if (verificationCode.isPresent()) {
                // Create verification token.
                ExpirableToken emailConfirmationToken = newEmailVerificationToken(user, verificationCode.get());
                String emailTokenAsString = expirableTokenEncryptor.encrypt(emailConfirmationToken);
                data.put("confirmUrl", urlBuilderProvider.get().getEmailConfirmationUrl(emailTokenAsString));
                subject = messages.get("signUpEmailConfirmationSubject", Optional.<String>of(locale)).get();
                localizedTemplate = String.format(EMAIL_VERIFICATION_TEMPLATE, locale);
            } else {
                subject = messages.get("signUpEmailWelcomeSubject", Optional.<String>of(locale)).get();
                localizedTemplate = String.format(EMAIL_WELCOME_TEMPLATE, locale);
            }

            // Send the email.
            emailService.send(user.getEmail(), subject, localizedTemplate, data);
        } catch (MessagingException | TemplateException | ExpirableTokenEncryptorException ex) {
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
        return ExpirableToken.newUserToken(ExpirableTokenType.EMAIL_VERIFICATION, user.getId(),
                "verificationCode", verificationCode, emailTokenTtl);
    }

    /**
     * Returns new sign up page verification token. Contains user id, verification code sent by email or SMS.
     *
     * @param user Registered user.
     * @param verificationCode Verification code.
     * @return Sign up page verification token.
     */
    ExpirableToken newSignUpPageVerificationToken(User user, String verificationCode) {
        return ExpirableToken.newUserToken(ExpirableTokenType.SIGNUP_VERIFICATION, user.getId(),
                "verificationCode", verificationCode, signUpVerificationTokenTtl);
    }

    /**
     * Email notification type for sign up completion.
     */
    enum EmailNotificationType {

        /**
         * Email confirmation is required.
         */
        CONFIRMATION,

        /**
         * Simple welcome email is sent.
         */
        WELCOME,

        /**
         * No emails are sent.
         */
        NONE;

        /**
         * Returns email notification type from string. {@link EmailNotificationType#CONFIRMATION} by default.
         *
         * @param typeAsString Notification type as string.
         * @return Notification type from string.
         */
        public static EmailNotificationType fromString(String typeAsString) {
            try {
                return valueOf(typeAsString.toUpperCase());
            } catch (Exception e) {
                return CONFIRMATION;
            }
        }
    }
}
