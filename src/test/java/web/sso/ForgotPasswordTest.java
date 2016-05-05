package web.sso;

import com.google.inject.Injector;
import controllers.sso.auth.SignInController;
import controllers.sso.auth.state.SignInState;
import models.sso.User;
import models.sso.UserGender;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.postoffice.mock.PostofficeMockImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import services.sso.CaptchaTokenService;
import services.sso.CountryService;
import services.sso.UserService;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Forgot password test.
 */
public class ForgotPasswordTest extends WebDriverTest {

    /**
     * First name for test account.
     */
    static final String FIRST_NAME = "FirstName";

    /**
     * Last name for test account.
     */
    static final String LAST_NAME = "LastName";

    /**
     * Username for test user.
     */
    static final String USERNAME = "forgotPasswordUser";

    /**
     * Password for test user.
     */
    static final String EMAIL = "forgotPasswordEmail@example.org";

    /**
     * PHONE for test user.
     */
    static final String PHONE = "+1 650 999 9999";

    /**
     * Password for test account.
     */
    static final String PASSWORD = "wrongPassword";

    /**
     * Password for test account.
     */
    static final String RESTORED_PASSWORD = "restoredPassword";

    /**
     * Remote IP for test account.
     */
    static final String REMOTE_IP = "1.2.3.4";

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
     * Country service.
     */
    CountryService countryService;

    /**
     * Email service mock.
     */
    PostofficeMockImpl emailServiceMock;


    /**
     * Captcha token service.
     */
    CaptchaTokenService captchaTokenService;

    /**
     * User to restore password.
     */
    User user;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.countryService = injector.getBinding(CountryService.class).getProvider().get();
        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
        this.emailServiceMock = (PostofficeMockImpl) injector.getBinding(Postoffice.class).getProvider().get();

        // Create test user.
        EntityManager em = entityManagerProvider.get();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        this.user = createUser();
        this.userService.createNew(this.user, PASSWORD, REMOTE_IP);
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
    public void testForgotPassword() throws Exception {
        String forgotPasswordUrl = getForgotPasswordUrl(getServerAddress() + "?forgot_password_url=true");
        goTo(forgotPasswordUrl);

        getFormElement("emailOrUsername").sendKeys(USERNAME + "_DOESNT_EXIST");
        getFormElement("captchaCode").sendKeys("WRONG_CAPTCHA");

        click("#forgotSubmit");

        assertNotNull("Error notification exists.", webDriver.findElement(By.className("alert-danger")));

        // Fix form errors.
        getFormElement("emailOrUsername").clear();
        getFormElement("emailOrUsername").sendKeys(USERNAME);

        // Set captcha word and token that are known to test.
        String captchaWord = "forgotPassword";
        String captchaToken = captchaTokenService.newCaptchaToken(captchaWord);

        getFormElement("captchaCode").clear();
        getFormElement("captchaCode").sendKeys(captchaWord);
        getFormElement("captchaToken").clear();
        getFormElement("captchaToken").sendKeys(captchaToken);

        click("#forgotSubmit");

        String url = webDriver.getCurrentUrl();
        assertTrue("Sign in URL expected: " + url,
                url.contains(router.getReverseRoute(SignInController.class, "signInGet")));
        assertTrue("Sign in URL must contain email state: " + url,
                url.contains(SignInState.FORGOT_EMAIL_SENT.toString().toLowerCase()));
        assertTrue("Sign in URL must contain correct continue URL: " + url, url.contains("forgot_password_url"));

        // Now, read sent email and extract restore password token.
        Mail mail = emailServiceMock.getLastSentMail();
        assertNotNull("Email is expected.", mail);

        // Check restore password behavior with wrong token.
        goTo(getRestorePasswordUrl("wrongToken"));
        assertNotNull("Error notification exists.", webDriver.findElement(By.className("alert-danger")));

        // Now check valid token.
        String restoreToken = extractRestoreTokenFromEmail(mail);
        goTo(getRestorePasswordUrl(restoreToken));
        assertTrue("No error notification.", webDriver.findElements(By.className("alert-danger")).isEmpty());

        // Enter new password.
        getFormElement("password").clear();
        getFormElement("password").sendKeys(RESTORED_PASSWORD);

        getFormElement("confirmPassword").clear();
        getFormElement("confirmPassword").sendKeys(RESTORED_PASSWORD);

        click("#restorePasswordSubmit");

        url = webDriver.getCurrentUrl();
        assertTrue("Redirected to Sign In with restored password state: " + url,
                url.contains(SignInState.PASSWORD_CHANGED.toString().toLowerCase()));

        // Re-read the user from database.
        entityManagerProvider.get().detach(user);
        user = userService.getByEmail(EMAIL);
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
        URI restoreURI = new URI(restoreUrl);
        String token = extractParameters(restoreURI).get("restoreToken");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("restoreToken parameter is expected: " + restoreUrl);
        }
        return token;
    }

    /**
     * Creates new user for test purposes.
     *
     * @return New user,
     */
    private User createUser() {
        User user = new User(USERNAME, EMAIL, PHONE);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setCountry(countryService.get(COUNTRY_ID));
        user.setDateOfBirth(LocalDate.of(YEAR, MONTH, DAY_OF_MONTH));
        user.setGender(UserGender.FEMALE);
        return user;
    }
}
