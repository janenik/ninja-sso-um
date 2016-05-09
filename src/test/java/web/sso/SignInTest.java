package web.sso;

import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import services.sso.CaptchaTokenService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Selenium tests for sign in.
 */
public class SignInTest extends WebDriverTest {

    /**
     * Captcha token service.
     */
    CaptchaTokenService captchaTokenService;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
    }

    @Test
    public void testSignIn() {
        goTo(getSignInUrl());

        assertTrue("No error notification.", webDriver.findElements(By.className("alert-danger")).isEmpty());
        assertTrue("No success notifications.", webDriver.findElements(By.className("alert-success")).isEmpty());
        assertTrue("No captcha with the first hit.", webDriver.findElements(By.name("captchaCode")).isEmpty());

        getFormElement("emailOrUsername").sendKeys("email@nowhere.org");
        getFormElement("password").sendKeys("wrongPassword");

        click("#signInSubmit");

        assertNotNull("Error notification exists.", webDriver.findElement(By.className("alert-danger")));

        // In test mode all continue URLs are allowed.
        goTo(getSignInUrl(getServerAddress() + "?successful_sign_in=true", null));

        assertTrue("No captcha.", webDriver.findElements(By.name("captchaCode")).isEmpty());

        // Apply existing user.
        getFormElement("emailOrUsername").sendKeys("root");
        getFormElement("password").sendKeys("password");

        click("#signInSubmit");

        assertThatContinueUrlHas("successful_sign_in=true");
    }

    @Test
    public void testSignInWithCaptcha() {
        int numberOfSafeRequests = properties.getIntegerWithDefault("counters.ip.numberOfSafeRequests", 5);

        // Reach IP limit hits so the sign in page shows captcha.
        for (int i = 0; i < numberOfSafeRequests; i++) {
            goTo(getSignInUrl());
        }
        String signInUrl = getSignInUrl(getServerAddress() + "?successful_sign_in=true", null);
        goTo(signInUrl);

        assertTrue("No error notification.", webDriver.findElements(By.className("alert-danger")).isEmpty());
        assertTrue("No success notifications.", webDriver.findElements(By.className("alert-success")).isEmpty());

        // Apply existing user.
        getFormElement("emailOrUsername").sendKeys("root");
        getFormElement("password").sendKeys("password");

        // Set captcha word and token that are known to test.
        String captchaWord = "captchaSecret393";
        String captchaToken = captchaTokenService.newCaptchaToken(captchaWord);

        getFormElement("captchaCode").clear();
        getFormElement("captchaCode").sendKeys(captchaWord);
        getFormElement("captchaToken").clear();
        getFormElement("captchaToken").sendKeys(captchaToken);

        click("#signInSubmit");

        assertThatContinueUrlHas("successful_sign_in=true");
    }

    @Test
    public void testHasSuccessAndDangerNotifications() {
        goTo(getSignInUrl(null, "forgot_email_sent"));
        assertNotNull("Success notification exists.", webDriver.findElement(By.className("alert-success")));

        goTo(getSignInUrl(null, "password_CHANGED"));
        assertNotNull("Success notification exists.", webDriver.findElement(By.className("alert-success")));

        goTo(getSignInUrl(null, "EMAIL_VERIFICATION_FAILED"));
        assertNotNull("Danger notification exists.", webDriver.findElement(By.className("alert-danger")));
    }

    /**
     * Verifies that current URL contains given substring.
     *
     * @param urlPart URL part (substring).
     */
    private void assertThatContinueUrlHas(String urlPart) {
        String messageFormat = "Expected redirect to: %s (must contain '%s')\nPage source: \n%s\n: .";
        String url = webDriver.getCurrentUrl();
        String message = String.format(messageFormat, url, urlPart, webDriver.getPageSource());
        assertTrue(message, url.contains(urlPart));
    }
}
