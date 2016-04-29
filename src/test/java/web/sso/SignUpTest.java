package web.sso;

import com.google.inject.Injector;
import controllers.sso.auth.SignUpController;
import controllers.sso.web.Escapers;
import models.sso.User;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.NinjaFluentLeniumTest;
import ninja.Router;
import ninja.utils.NinjaProperties;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Selenium tests for sign up.
 */
public class SignUpTest extends NinjaFluentLeniumTest {

    /**
     * Application router.
     */
    Router router;

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

    /**
     * Application properties.
     */
    NinjaProperties properties;

    /**
     * Logger.
     */
    Logger logger;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.router = injector.getBinding(Router.class).getProvider().get();
        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.encryptor = injector.getBinding(ExpirableTokenEncryptor.class).getProvider().get();
        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
        this.properties = injector.getBinding(NinjaProperties.class).getProvider().get();
        this.logger = injector.getBinding(Logger.class).getProvider().get();

        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    @Test
    public void testSignUp() throws ExpiredTokenException, IllegalTokenException, CaptchaTokenService.AlreadyUsedTokenException {
        goTo(getSignUpUrl(getServerAddress() + "?successful_sign_up=true"));

        assertTrue("Must have continue URL", webDriver.getCurrentUrl().contains("successful_sign_up"));

        // Single alert-danger is expected (noscript warning).
        assertEquals("noscript warning is expected", 1, webDriver.findElements(By.className("alert-danger")).size());

        getFormElement("firstName").sendKeys("FirstName");
        getFormElement("lastName").sendKeys("LastName");

        getFormElement("birthMonth").sendKeys("12");
        getFormElement("birthDay").sendKeys("24");
        getFormElement("birthYear").sendKeys("1988");

        getFormElement("username").sendKeys("webDriverUsername1234567890");

        getFormElement("gender").sendKeys("FEMALE");

        getFormElement("email").sendKeys("email@somewhere.org");
        getFormElement("password").sendKeys("wrongPassword");
        getFormElement("passwordRepeat").sendKeys("wrongPassword");

        getFormElement("countryId").sendKeys("US");
        getFormElement("phone").sendKeys("+1 650 9999 999");


        // Try to sign up.
        click("#signUpSubmit");

        // noscript + form warning are expected.
        assertEquals("warning is expected", 2, webDriver.findElements(By.className("alert-danger")).size());

        // Verify that form values are preserved.
        assertEquals("First name must be preserved", "FirstName", getFormElementValue("firstName"));
        assertEquals("Last name must be preserved", "LastName", getFormElementValue("lastName"));

        assertEquals("Gender must be preserved", "FEMALE", getFormElementValue("gender"));

        assertEquals("Birth day must be preserved", "12", getFormElementValue("birthMonth"));
        assertEquals("Birth month must be preserved", "24", getFormElementValue("birthDay"));
        assertEquals("Birth year must be preserved", "1988", getFormElementValue("birthYear"));

        assertEquals("Username must be preserved", "webDriverUsername1234567890", getFormElementValue("username"));

        assertEquals("Country must be preserved", "US", getFormElementValue("countryId"));
        assertEquals("Phone must be preserved", "+1 650 9999 999", getFormElementValue("phone"));
        assertEquals("Email must be preserved", "email@somewhere.org", getFormElementValue("email"));

        assertEquals("Password must be preserved", "wrongPassword", getFormElementValue("password"));
        assertEquals("Password must be preserved", "wrongPassword", getFormElementValue("passwordRepeat"));

        // Continue to fill the form.
        // Set captcha word and token that are known to test.
        String captchaWord = "12345";
        String captchaToken = captchaTokenService.newCaptchaToken(captchaWord);
        assertEquals(captchaWord, captchaTokenService.extractCaptchaText(captchaToken));

        getFormElement("captchaCode").clear();
        getFormElement("captchaCode").sendKeys(captchaWord);
        getFormElement("token").clear();
        getFormElement("token").sendKeys(captchaToken);

        click("#agreement");

        click("#signUpSubmit");

        String url = webDriver.getCurrentUrl();
        assertTrue("Verify URL expected: " + url, url.contains("/verify"));
        assertTrue("Verify URL contains continue URL: " + url, url.contains("successful_sign_up"));

        User user = userService.getByUsername("webDriverUsername1234567890");
        assertNotNull("User is expected to be created", user);
        assertEquals("FirstName", user.getFirstName());
        assertEquals("LastName", user.getLastName());
        assertEquals(LocalDate.of(1988, 12, 24), user.getDateOfBirth());
    }

    /**
     * Returns form element value by name.
     *
     * @param name Name of the element.
     * @return Form element value.
     */
    private String getFormElementValue(String name) {
        return getFormElement(name).getAttribute("value");
    }

    /**
     * Returns form element by name.
     *
     * @param name Name of the element.
     * @return Web element on the page.
     */
    private WebElement getFormElement(String name) {
        return webDriver.findElement(By.name(name));
    }

    /**
     * Constructs sign up URL with optional continue URL.
     *
     * @param continueUrl Continue URL. Optional.
     * @return Continue URL.
     */
    private String getSignUpUrl(String... continueUrl) {
        StringBuilder sb = new StringBuilder(getServerAddress())
                .append(router.getReverseRoute(SignUpController.class, "signUpGet"))
                .append("?");
        if (continueUrl.length > 0 && continueUrl[0] != null) {
            sb.append("&continue=");
            sb.append(Escapers.encodePercent(continueUrl[0]));
        }
        return sb.toString();
    }
}
