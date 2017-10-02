package web.sso;

import com.google.inject.Injector;
import controllers.sso.auth.SignUpVerificationController;
import models.sso.User;
import models.sso.UserConfirmationState;
import models.sso.UserGender;
import models.sso.UserRole;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;
import web.sso.common.WebDriverTest;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
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
    private static final String FIRST_NAME = "FirstName";

    /**
     * Last name for test account.
     */
    private static final String LAST_NAME = "LastName";

    /**
     * Username for test account.
     */
    private static final String USERNAME = "webDriverUsername1234567890";

    /**
     * Username in lowercase.
     */
    private static final String USERNAME_LOWERCASED = USERNAME.toLowerCase();

    /**
     * Email for test account.
     */
    private static final String EMAIL = "email@somewhere.org";

    /**
     * Phone for test account.
     */
    private static final String PHONE = "+1 650 9999 999";

    /**
     * Password for test account.
     */
    private static final String PASSWORD = "wrongPassword";

    /**
     * Country for test account.
     */
    private static final String COUNTRY_ID = "US";

    /**
     * Birth year for test account.
     */
    private static final int YEAR = 1988;

    /**
     * Birth month for test account.
     */
    private static final int MONTH = 12;

    /**
     * Birth day for test account.
     */
    private static final int DAY_OF_MONTH = 24;

    /**
     * User service.
     */
    private UserService userService;

    /**
     * Encryptor.
     */
    private ExpirableTokenEncryptor encryptor;

    /**
     * Captcha token service.
     */
    private CaptchaTokenService captchaTokenService;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.encryptor = injector.getBinding(ExpirableTokenEncryptor.class).getProvider().get();
        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
    }

    @Test
    public void testSignUpFieldsPreserved() throws Exception {
        String signUpUrl = getSignUpUrl(getBaseUrl() + "?successful_sign_up=true");
        goTo(signUpUrl);

        assertTrue("Must have continue URL", webDriver.getCurrentUrl().contains("successful_sign_up"));

        fillSignUpFormWithoutCaptchaCodeAndAgreement();

        if (!getFormInput("agreement").isSelected()) {
            click("#agreement");
        }
        // Try to sign up.
        click("#signUpSubmit");

        // form warning is expected.
        List<WebElement> alertElements = webDriver.findElements(By.className("alert-danger"));
        assertEquals("One warning is expected", 1, alertElements.size());

        List<WebElement> errorElements = webDriver.findElements(By.className("ssoFieldErrorDescription"));
        assertEquals("One error element is expected", 1, errorElements.size());
        assertEquals("Please enter correct code.", errorElements.get(0).getText());

        verifyFormValuesPreserved();
    }

    @Test
    public void testSignUpWithCaptcha() throws Exception {
        String signUpUrl = getSignUpUrl(getBaseUrl() + "?successful_sign_up=true");
        goTo(signUpUrl);

        assertTrue("Must have continue URL", webDriver.getCurrentUrl().contains("successful_sign_up"));

        fillSignUpFormWithoutCaptchaCodeAndAgreement();

        // Set captcha code and token values that are known to test.
        String captchaCode = "captchaSecret494";
        String captchaToken = captchaTokenService.newCaptchaToken(captchaCode);
        assertEquals(captchaCode, captchaTokenService.extractCaptchaText(captchaToken));

        setFormInputValue("captchaCode", captchaCode);
        setFormInputValue("token", captchaToken);

        if (!getFormInput("agreement").isSelected()) {
            click("#agreement");
        }

        String fetchedCaptchaCode = getFormInputValue("captchaCode");
        String fetchedToken = getFormInputValue("token");
        assertEquals("Form captcha code is expected to be set.", captchaCode, fetchedCaptchaCode);
        assertEquals("Form captcha token is expected to be set.", captchaToken, fetchedToken);

        click("#signUpSubmit");

        // Verify browser location.
        String url = webDriver.getCurrentUrl();
        Map<String, String> urlParameters = extractParameters(new URI(url));

        // Verify that the browser has opened verification form.
        assertTrue("Verify URL expected: " + url,
                url.contains(reverseRouter.with(SignUpVerificationController::verifySignUp).build()));
        assertTrue("Verify URL contains valid continue URL: " + urlParameters.get("continue"),
                urlParameters.get("continue").contains("successful_sign_up=true"));

        // Verify that user is created.
        User user = userService.getByUsername(USERNAME);
        verifyUser(user, UserConfirmationState.UNCONFIRMED);
    }

    /**
     * Fills in form data, omitting captcha code and agreement.
     */
    private void fillSignUpFormWithoutCaptchaCodeAndAgreement() {
        setFormInputValue("firstName", FIRST_NAME);
        setFormInputValue("lastName", LAST_NAME);

        setFormInputValue("birthMonth", Integer.toString(MONTH));
        setFormInputValue("birthDay", Integer.toString(DAY_OF_MONTH));
        setFormInputValue("birthYear", Integer.toString(YEAR));

        setFormInputValue("username", USERNAME);

        setFormInputValue("gender", UserGender.FEMALE.toString());

        setFormInputValue("email", EMAIL);
        setFormInputValue("password", PASSWORD);
        setFormInputValue("passwordRepeat", PASSWORD);

        setFormInputValue("countryId", COUNTRY_ID);
        setFormInputValue("phone", PHONE);

        setFormInputValue("captchaCode", "@@@@@");
    }


    /**
     * Verifies that current sign up form values are preserved in case of error after submit.
     */
    private void verifyFormValuesPreserved() {
        assertEquals("First name must be preserved.", FIRST_NAME, getFormInputValue("firstName"));
        assertEquals("Last name must be preserved.", LAST_NAME, getFormInputValue("lastName"));

        assertEquals("Gender must be preserved.", UserGender.FEMALE.toString(), getFormInputValue("gender"));

        assertEquals("Birth day must be preserved.", Integer.toString(MONTH), getFormInputValue("birthMonth"));
        assertEquals("Birth month must be preserved.", Integer.toString(DAY_OF_MONTH), getFormInputValue("birthDay"));
        assertEquals("Birth year must be preserved.", Integer.toString(YEAR), getFormInputValue("birthYear"));

        assertEquals("Username must be preserved.", USERNAME, getFormInputValue("username"));

        assertEquals("Country must be preserved.", COUNTRY_ID, getFormInputValue("countryId"));
        assertEquals("Phone must be preserved.", PHONE, getFormInputValue("phone"));
        assertEquals("Email must be preserved.", EMAIL, getFormInputValue("email"));

        assertEquals("Password must be preserved.", PASSWORD, getFormInputValue("password"));
        assertEquals("Password must be preserved.", PASSWORD, getFormInputValue("passwordRepeat"));
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
