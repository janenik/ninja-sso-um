package web.sso;

import com.google.inject.Injector;
import controllers.sso.auth.SignInController;
import ninja.NinjaFluentLeniumTest;
import ninja.Router;
import ninja.utils.NinjaProperties;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import services.sso.token.ExpirableTokenEncryptor;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Selenium tests for sign in.
 */
public class SignInTest extends NinjaFluentLeniumTest {

    Router router;
    ExpirableTokenEncryptor encryptor;
    NinjaProperties properties;
    Logger logger;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.router = injector.getBinding(Router.class).getProvider().get();
        this.encryptor = injector.getBinding(ExpirableTokenEncryptor.class).getProvider().get();
        this.properties = injector.getBinding(NinjaProperties.class).getProvider().get();
        this.logger = injector.getBinding(Logger.class).getProvider().get();

        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    @Test
    public void testSubmitWrongUsernameOrPassword() {
        goTo(getSignInUrl());

        assertTrue("No error notification", webDriver.findElements(By.className("alert-danger")).isEmpty());
        assertTrue("No success notifications", webDriver.findElements(By.className("alert-success")).isEmpty());

        webDriver.findElement(By.name("emailOrUsername")).sendKeys("email@nowhere.org");
        webDriver.findElement(By.name("password")).sendKeys("email@nowhere.org");

        click("#signInSubmit");

        assertNotNull("Error notification exists", webDriver.findElement(By.className("alert-danger")));
    }

    @Test
    public void testHasSuccessAndDangerNotification() {
        goTo(getSignInUrl() + "?state=forgot_email_sent");
        assertNotNull("Success notification exists", webDriver.findElement(By.className("alert-success")));

        goTo(getSignInUrl() + "?state=password_CHANGED ");
        assertNotNull("Success notification exists", webDriver.findElement(By.className("alert-success")));

        goTo(getSignInUrl() + "?state=EMAIL_VERIFICATION_FAILED ");
        assertNotNull("Danger notification exists", webDriver.findElement(By.className("alert-danger")));
    }

    private String getSignInUrl() {
        return getServerAddress() + router.getReverseRoute(SignInController.class, "signInGet");
    }
}
