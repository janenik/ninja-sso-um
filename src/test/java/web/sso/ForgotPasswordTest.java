package web.sso;

import com.google.inject.Injector;
import controllers.sso.auth.SignInController;
import controllers.sso.auth.state.SignInState;
import models.sso.User;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.postoffice.mock.PostofficeMockImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import web.sso.common.TestEntitiesFactory;
import web.sso.common.WebDriverTest;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Forgot password test.
 */
public class ForgotPasswordTest extends WebDriverTest {

    /**
     * Password for test account.
     */
    private static final String RESTORED_PASSWORD = "restoredPassword";

    /**
     * User service.
     */
    private UserService userService;

    /**
     * Email service mock.
     */
    private PostofficeMockImpl emailServiceMock;

    /**
     * Captcha token service.
     */
    private CaptchaTokenService captchaTokenService;

    /**
     * Test entities factory.
     */
    private TestEntitiesFactory testEntitiesFactory;

    /**
     * User to restore password.
     */
    private User user;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
        this.emailServiceMock = (PostofficeMockImpl) injector.getBinding(Postoffice.class).getProvider().get();
        this.testEntitiesFactory = injector.getBinding(TestEntitiesFactory.class).getProvider().get();

        // Create test user.
        EntityManager em = entityManagerProvider.get();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        this.user = testEntitiesFactory.createNewUser();
        transaction.commit();
    }

    @After
    public void tearDown() {
        // Remove test user.
        EntityManager em = entityManagerProvider.get();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        userService.removeUserEvents(user);
        em.remove(em.merge(user));
        transaction.commit();
    }

    @Test
    public void testForgotPasswordForUserThatDoesntExist() throws Exception {
        String forgotPasswordUrl = getForgotPasswordUrl(getBaseUrl() + "?forgot_password_url=true");
        goTo(forgotPasswordUrl);

        getFormInput("emailOrUsername").sendKeys(TestEntitiesFactory.USERNAME + "_DOESNT_EXIST");
        getFormInput("captchaCode").sendKeys("WRONG_CAPTCHA");

        click("#forgotSubmit");

        assertNotNull("Error notification exists.", webDriver.findElement(By.className("alert-danger")));
    }

    @Test
    public void testForgotPassword() throws Exception {
        String forgotPasswordUrl = getForgotPasswordUrl(getBaseUrl() + "?forgot_password_url=true");
        goTo(forgotPasswordUrl);

        // Fix form errors.
        setFormInputValue("emailOrUsername", TestEntitiesFactory.USERNAME);

        // Set captcha word and token that are known to test.
        String captchaWord = "forgotPassword";
        String captchaToken = captchaTokenService.newCaptchaToken(captchaWord);

        setFormInputValue("captchaCode", captchaWord);
        setFormInputValue("captchaToken", captchaToken);

        click("#forgotSubmit");

        String url = webDriver.getCurrentUrl();
        assertTrue("Sign in URL expected: " + url,
                url.contains(reverseRouter.with(SignInController::signInGet).build()));
        assertTrue("Sign in URL must contain email state: " + url,
                url.contains(SignInState.FORGOT_EMAIL_SENT.toString().toLowerCase()));
        assertTrue("Sign in URL must contain correct continue URL: " + url, url.contains("forgot_password_url"));

        // Now, read sent email and extract restore password token.
        Mail mail = emailServiceMock.getLastSentMail();
        assertNotNull("Email is expected.", mail);

        // Now check valid token.
        String restoreToken = extractRestoreTokenFromEmail(mail);
        goTo(getRestorePasswordUrl(restoreToken));
        assertTrue("No error notification.", webDriver.findElements(By.className("alert-danger")).isEmpty());

        // Enter new password.
        setFormInputValue("password", RESTORED_PASSWORD);
        setFormInputValue("confirmPassword", RESTORED_PASSWORD);

        submitFormWithJavascript();

        url = webDriver.getCurrentUrl();
        assertTrue("Redirected to Sign In with restored password state: " + url,
                url.contains(SignInState.PASSWORD_CHANGED.toString().toLowerCase()));

        // Re-read the user and credentials from database.
        entityManagerProvider.get().detach(user);
        entityManagerProvider.get().detach(userService.getCredentials(user));

        user = userService.getByEmail(TestEntitiesFactory.EMAIL);
        assertTrue("New password is expected.",  userService.isValidPassword(user, RESTORED_PASSWORD));
    }

    /**
     * Extracts restore token from given email.
     *
     * @param mail Email.
     * @return Restore token for email restoration.
     */
    private String extractRestoreTokenFromEmail(Mail mail) throws URISyntaxException {
        String html = mail.getBodyHtml();
        int indexOfFirstRefIndex = html.indexOf("href=\"");
        if (indexOfFirstRefIndex < 0) {
            throw new IllegalStateException("Email " + html + " doesn't contain a reference.");
        }
        int endOfRefIndex = html.indexOf('"', indexOfFirstRefIndex + 6);
        String restoreUrl = html.substring(indexOfFirstRefIndex + 6, endOfRefIndex);
        String token = extractParameters(restoreUrl).get("restoreToken");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("restoreToken parameter is expected: " + restoreUrl);
        }
        return token;
    }
}
