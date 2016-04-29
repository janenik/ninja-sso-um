package web.sso;

import com.google.inject.Injector;
import controllers.sso.auth.SignInController;
import controllers.sso.web.Escapers;
import ninja.NinjaFluentLeniumTest;
import ninja.Router;
import ninja.utils.NinjaProperties;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import services.sso.CaptchaTokenService;
import services.sso.token.ExpirableTokenEncryptor;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Selenium tests for sign in.
 */
public class SignInTest extends NinjaFluentLeniumTest {

    /**
     * Application router.
     */
    Router router;

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
        this.encryptor = injector.getBinding(ExpirableTokenEncryptor.class).getProvider().get();
        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
        this.properties = injector.getBinding(NinjaProperties.class).getProvider().get();
        this.logger = injector.getBinding(Logger.class).getProvider().get();

        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    @Test
    public void testSignIn() {
        goTo(getSignInUrl());

        assertTrue("No error notification", webDriver.findElements(By.className("alert-danger")).isEmpty());
        assertTrue("No success notifications", webDriver.findElements(By.className("alert-success")).isEmpty());
        assertTrue("No captcha with the first hit", webDriver.findElements(By.name("captchaCode")).isEmpty());

        getFormElement("emailOrUsername").sendKeys("email@nowhere.org");
        getFormElement("password").sendKeys("wrongPassword");

        click("#signInSubmit");

        assertNotNull("Error notification exists", webDriver.findElement(By.className("alert-danger")));

        // In test mode all continue URLs are allowed.
        goTo(getSignInUrl(getServerAddress() + "?successful_sign_in=true", null));

        assertTrue("No captcha", webDriver.findElements(By.name("captchaCode")).isEmpty());

        // Apply existing user.
        getFormElement("emailOrUsername").sendKeys("root");
        getFormElement("password").sendKeys("password");

        click("#signInSubmit");

        assertTrue("Redirected to continue URL", webDriver.getCurrentUrl().contains("successful_sign_in=true"));
    }

    @Test
    public void testSignInWithCaptcha() {
        int numberOfSafeRequests = properties.getIntegerWithDefault("counters.ip.numberOfSafeRequests", 5);

        // Reach IP limit hits so the sign in page shows captcha.
        for (int i = 0; i < numberOfSafeRequests; i++) {
            goTo(getSignInUrl());
        }
        goTo(getSignInUrl(getServerAddress() + "?successful_sign_in=true", null));

        assertTrue("No error notification", webDriver.findElements(By.className("alert-danger")).isEmpty());
        assertTrue("No success notifications", webDriver.findElements(By.className("alert-success")).isEmpty());

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

        assertTrue("Redirected to continue URL", webDriver.getCurrentUrl().contains("successful_sign_in"));
    }

    @Test
    public void testHasSuccessAndDangerNotifications() {
        goTo(getSignInUrl(null, "forgot_email_sent"));
        assertNotNull("Success notification exists", webDriver.findElement(By.className("alert-success")));

        goTo(getSignInUrl(null, "password_CHANGED"));
        assertNotNull("Success notification exists", webDriver.findElement(By.className("alert-success")));

        goTo(getSignInUrl(null, "EMAIL_VERIFICATION_FAILED"));
        assertNotNull("Danger notification exists", webDriver.findElement(By.className("alert-danger")));
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
     * Constructs sign in URL.
     *
     * @return Sign in URL.
     */
    private String getSignInUrl() {
        return getSignInUrl(null, null);
    }

    /**
     * Constructs sign in URL with optional continue URL and optional state.
     *
     * @param continueUrl Continue URL. Optional.
     * @param state State of the sign in. Optional.
     * @return Continue URL.
     */
    private String getSignInUrl(String continueUrl, String state) {
        StringBuilder sb = new StringBuilder(getServerAddress())
                .append(router.getReverseRoute(SignInController.class, "signInGet"))
                .append("?");
        if (continueUrl != null) {
            sb.append("&continue=");
            sb.append(Escapers.encodePercent(continueUrl));
        }
        if (state != null) {
            sb.append("&state=");
            sb.append(Escapers.encodePercent(state));
        }
        return sb.toString();
    }
}
