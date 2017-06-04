package web.sso;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import controllers.sso.admin.users.UsersController;
import controllers.sso.auth.SignInController;
import models.sso.User;
import models.sso.UserRole;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import services.sso.UserService;

import javax.persistence.EntityTransaction;
import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * User/moderator admin page access web-driver test.
 */
public class UserOrModeratorAdminPageAccessTest extends WebDriverTest {

    private static final String REGULAR_USER_USERNAME = "demouser1";
    private static final String REGULAR_USER_PASSWORD = "demouserpassword1";

    /**
     * User service.
     */
    private UserService userService;

    /**
     * Admin users relative URL.
     */
    private String adminUsersRelativeUrl;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();
        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.adminUsersRelativeUrl = reverseRouter.with(UsersController::users).build();
    }

    @Test
    public void testUnauthorizedUserIsRedirectedToSignIn() throws URISyntaxException {
        goTo(getAdminUsersUrl());

        String url = webDriver.getCurrentUrl();
        String signInRelativeUrl = reverseRouter.with(SignInController::signInGet).build();
        String continueParameter = extractParameters(url).get("continue");
        assertTrue("Must be redirected to sign in", url.contains(signInRelativeUrl));
        assertNotNull("Sign in URL must contain continue", continueParameter);
        assertTrue("Continue URL must be admin user URL", continueParameter.contains(adminUsersRelativeUrl));
    }

    @Test
    public void testRegularUserAccess() {
        User regularUser = userService.getByUsername(REGULAR_USER_USERNAME);

        assertNotNull("Regular user is expected to exist", regularUser);
        assertTrue("Regular user must be regular", regularUser.isUser());
        assertTrue("Password must be valid", userService.isValidPassword(regularUser, REGULAR_USER_PASSWORD));

        goTo(getSignInUrl(getAdminUsersUrl()));

        assertTrue("No captcha.", webDriver.findElements(By.name("captchaCode")).isEmpty());

        // Apply existing user.
        getFormElement("emailOrUsername").sendKeys(REGULAR_USER_USERNAME);
        getFormElement("password").sendKeys(REGULAR_USER_PASSWORD);

        click("#signInSubmit");

        assertFalse("Regular user is not allowed to access admin page",
                webDriver.getCurrentUrl().contains(adminUsersRelativeUrl));
    }

    @Test
    public void testModeratorAccess() {
        EntityTransaction transaction = entityManagerProvider.get().getTransaction();
        try {
            transaction.begin();
            User regularUser = userService.getByUsername(REGULAR_USER_USERNAME);

            assertNotNull("Regular user is expected to exist", regularUser);
            assertTrue("Regular user must be regular", regularUser.isUser());
            assertTrue("Password must be valid", userService.isValidPassword(regularUser, REGULAR_USER_PASSWORD));

            // Update to moderator.
            regularUser.setRole(UserRole.MODERATOR);
            userService.update(regularUser);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            Throwables.propagate(e);
        }

        try {
            // Re-read user as moderator.
            User moderator = userService.getByUsername(REGULAR_USER_USERNAME);
            assertNotNull("Updated moderator is expected to exist", moderator);
            assertTrue("Moderator is expected", moderator.isModerator());

            goTo(getSignInUrl(getAdminUsersUrl()));

            assertTrue("No captcha.", webDriver.findElements(By.name("captchaCode")).isEmpty());

            // Apply existing user.
            getFormElement("emailOrUsername").sendKeys(REGULAR_USER_USERNAME);
            getFormElement("password").sendKeys(REGULAR_USER_PASSWORD);

            click("#signInSubmit");

            assertFalse("Moderator is not allowed to access admin page",
                    webDriver.getCurrentUrl().contains(adminUsersRelativeUrl));
        } finally {
            // Restore previous role.
            transaction = entityManagerProvider.get().getTransaction();
            transaction.begin();
            User regularUser = userService.getByUsername(REGULAR_USER_USERNAME);
            regularUser.setRole(UserRole.USER);
            userService.update(regularUser);
            transaction.commit();
        }
    }
}
