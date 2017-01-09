package web.sso;

import com.google.inject.Injector;
import controllers.sso.auth.SignInController;
import controllers.sso.auth.SignUpVerificationController;
import controllers.sso.auth.state.SignInState;
import models.sso.User;
import models.sso.UserConfirmationState;
import models.sso.UserGender;
import models.sso.UserRole;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Selenium tests for sign up.
 */
public class SignUpTest extends WebDriverTest {

    /**
     * First name for test account.
     */
    static final String FIRST_NAME = "FirstName";

    /**
     * Last name for test account.
     */
    static final String LAST_NAME = "LastName";

    /**
     * Username for test account.
     */
    static final String USERNAME = "webDriverUsername1234567890";

    /**
     * Username in lowercase.
     */
    static final String USERNAME_LOWERCASED = USERNAME.toLowerCase();

    /**
     * Email for test account.
     */
    static final String EMAIL = "email@somewhere.org";

    /**
     * Phone for test account.
     */
    static final String PHONE = "+1 650 9999 999";

    /**
     * Password for test account.
     */
    static final String PASSWORD = "wrongPassword";

    /**
     * Country for test account.
     */
    static final String COUNTRY_ID = "US";

    /**
     * Birth year for test account.
     */
    static final int YEAR = 1988;

    /**
     * Birth month for test account.
     */
    static final int MONTH = 12;

    /**
     * Birth day for test account.
     */
    static final int DAY_OF_MONTH = 24;

    /**
     * User service.
     */
    UserService userService;

    /**
     * Encryptor.
     */
    ExpirableTokenEncryptor encryptor;

    /**
     * Captcha token service.
     */
    CaptchaTokenService captchaTokenService;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.encryptor = injector.getBinding(ExpirableTokenEncryptor.class).getProvider().get();
        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
    }

    @Test
    public void testSignUp() throws Exception {
        String signUpUrl = getSignUpUrl(getServerAddress() + "?successful_sign_up=true");
        goTo(signUpUrl);

        assertTrue("Must have continue URL", webDriver.getCurrentUrl().contains("successful_sign_up"));

        fillSignUpFormWithoutCaptchaCodeAndAgreement();

        if (!getFormElement("agreement").isSelected()) {
            click("#agreement");
        }

        // Try to sign up.
        click("#signUpSubmit");

        // noscript + form warning are expected.
        assertEquals("warning is expected", 2, webDriver.findElements(By.className("alert-danger")).size());

        verifyFormValuesPreserved();

        // Continue to fill the form.
        // Set captcha word and token values that are known to test.
        String captchaWord = "12345";
        String captchaToken = captchaTokenService.newCaptchaToken(captchaWord);
        assertEquals(captchaWord, captchaTokenService.extractCaptchaText(captchaToken));

        getFormElement("captchaCode").clear();
        getFormElement("captchaCode").sendKeys(captchaWord);
        getFormElement("token").clear();
        getFormElement("token").sendKeys(captchaToken);

        if (!getFormElement("agreement").isSelected()) {
            click("#agreement");
        }

        click("#signUpSubmit");

        // Verify browser location.
        String url = webDriver.getCurrentUrl();
        Map<String, String> urlParameters = extractParameters(new URI(url));

        // Verify that the browser has opened verification form.
        assertTrue("Verify URL expected: " + url,
                url.contains(router.getReverseRoute(SignUpVerificationController.class, "verifySignUp")));
        assertTrue("Verify URL contains valid continue URL: " + urlParameters.get("continue"),
                urlParameters.get("continue").contains("successful_sign_up=true"));

        // Verify that user is created.
        User user = userService.getByUsername(USERNAME);
        verifyUser(user, UserConfirmationState.UNCONFIRMED);

        // Decrypt the verification token.
        ExpirableToken token = encryptor.decrypt(urlParameters.get("token"));
        assertEquals(ExpirableTokenType.SIGNUP_VERIFICATION, token.getType());

        // Use verification code to confirm the account.
        String verificationCode = token.getAttributeValue("verificationCode");
        assertNotNull("Verification code is expected in token.", verificationCode);

        getFormElement("verificationCode").sendKeys(verificationCode);
        click("#verifySignUpSubmit");

        // Verify that the browser has opened sign in form and the URL has valid continue parameter..
        url = webDriver.getCurrentUrl();
        urlParameters = extractParameters(new URI(url));
        assertTrue("Sign in URL expected: " + url,
                url.contains(router.getReverseRoute(SignInController.class, "signInGet")));
        assertTrue("Sign in URL with message: " + url,
                url.contains(SignInState.EMAIL_VERIFICATION_CONFIRMED.toString().toLowerCase()));
        assertTrue("Verify URL contains valid continue URL: " + urlParameters.get("continue"),
                urlParameters.get("continue").contains("successful_sign_up=true"));

        // Need to detach the user object to re-read from database.
        entityManagerProvider.get().detach(user);

        // Verify that the user has confirmed the account.
        user = userService.getByEmail(EMAIL);
        verifyUser(user, UserConfirmationState.CONFIRMED);
    }

    /**
     * Fills in form data, omitting captcha code and agreement.
     */
    private void fillSignUpFormWithoutCaptchaCodeAndAgreement() {
        getFormElement("firstName").sendKeys(FIRST_NAME);
        getFormElement("lastName").sendKeys(LAST_NAME);

        getFormElement("birthMonth").sendKeys(Integer.toString(MONTH));
        getFormElement("birthDay").sendKeys(Integer.toString(DAY_OF_MONTH));
        getFormElement("birthYear").sendKeys(Integer.toString(YEAR));

        getFormElement("username").sendKeys(USERNAME);

        getFormElement("gender").sendKeys(UserGender.FEMALE.toString());

        getFormElement("email").sendKeys(EMAIL);
        getFormElement("password").sendKeys(PASSWORD);
        getFormElement("passwordRepeat").sendKeys(PASSWORD);

        getFormElement("countryId").sendKeys(COUNTRY_ID);
        getFormElement("phone").sendKeys(PHONE);

        getFormElement("captchaCode").clear();
        getFormElement("captchaCode").sendKeys("@@@@@");
    }


    /**
     * Verifies that current sign up form values are preserved in case of error after submit.
     */
    private void verifyFormValuesPreserved() {
        assertEquals("First name must be preserved.", FIRST_NAME, getFormElementValue("firstName"));
        assertEquals("Last name must be preserved.", LAST_NAME, getFormElementValue("lastName"));

        assertEquals("Gender must be preserved.", UserGender.FEMALE.toString(), getFormElementValue("gender"));

        assertEquals("Birth day must be preserved.", Integer.toString(MONTH), getFormElementValue("birthMonth"));
        assertEquals("Birth month must be preserved.", Integer.toString(DAY_OF_MONTH), getFormElementValue("birthDay"));
        assertEquals("Birth year must be preserved.", Integer.toString(YEAR), getFormElementValue("birthYear"));

        assertEquals("Username must be preserved.", USERNAME, getFormElementValue("username"));

        assertEquals("Country must be preserved.", COUNTRY_ID, getFormElementValue("countryId"));
        assertEquals("Phone must be preserved.", PHONE, getFormElementValue("phone"));
        assertEquals("Email must be preserved.", EMAIL, getFormElementValue("email"));

        assertEquals("Password must be preserved.", PASSWORD, getFormElementValue("password"));
        assertEquals("Password must be preserved.", PASSWORD, getFormElementValue("passwordRepeat"));
    }

    /**
     * Verifies data for created user.
     *
     * @param user Created user.
     */
    private void verifyUser(User user, UserConfirmationState confirmationState) {
        assertNotNull("User is expected to be created.", user);
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());

        assertEquals(USERNAME_LOWERCASED, user.getUsername());
        assertEquals(EMAIL, user.getEmail());

        assertEquals(LocalDate.of(YEAR, MONTH, DAY_OF_MONTH), user.getDateOfBirth());
        assertNull("Middle name is not used.", user.getMiddleName());
        assertEquals(PHONE, user.getPhone());
        assertEquals(COUNTRY_ID, user.getCountry().getIso());
        assertEquals(UserRole.USER, user.getRole());

        assertEquals("Expected confirmation state: " + confirmationState,
                confirmationState, user.getConfirmationState());

        assertTrue("Valid password is expected.", userService.isValidPassword(user, PASSWORD));
    }
}
