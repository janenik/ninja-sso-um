package web.sso.admin;

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
 * User/moderator admin page access web driver test.
 */
public class AdminPageAccessByRegularUserOrModeratorTest extends AdminWebDriverTest {

    /**
     * User service.
     */
    private UserService userService;

    /**
     * Username.
     */
    private String username;

    /**
     * Password for the user above.
     */
    private String password;

    /**
     * Admin users relative URL.
     */
    private String adminUsersRelativeUrl;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();
        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.adminUsersRelativeUrl = this.reverseRouter.with(UsersController::users).build();
        this.username = this.properties.getWithDefault("application.demo.usernameprefix", "demouser");
        this.password = this.username + "password1";
        this.username += "1";
    }

    @Test
    public void testUnauthorizedUserIsRedirectedToSignIn() throws URISyntaxException {
        goTo(getAdminUsersPageUrl());

        String url = webDriver.getCurrentUrl();
        String signInRelativeUrl = reverseRouter.with(SignInController::signInGet).build();
        String continueParameter = extractParameters(url).get("continue");
        assertTrue("Must be redirected to sign in", url.contains(signInRelativeUrl));
        assertNotNull("Sign in URL must contain continue", continueParameter);
        assertTrue("Continue URL must be admin user URL", continueParameter.contains(adminUsersRelativeUrl));
    }

    @Test
    public void testRegularUserAccess() {
        User regularUser = userService.getByUsername(username);

        assertNotNull("Regular user is expected to exist", regularUser);
        assertTrue("Regular user must be regular", regularUser.isUser());
        assertTrue("Password must be valid", userService.isValidPassword(regularUser, password));

        goTo(getSignInUrl(getAdminUsersPageUrl()));

        assertTrue("No captcha.", webDriver.findElements(By.name("captchaCode")).isEmpty());

        // Apply existing user.
        getFormInput("emailOrUsername").sendKeys(username);
        getFormInput("password").sendKeys(password);

        click("#signInSubmit");

        assertFalse("Regular user is not allowed to access admin page",
                webDriver.getCurrentUrl().contains(adminUsersRelativeUrl));
    }

    @Test
    public void testModeratorAccess() {
        EntityTransaction transaction = entityManagerProvider.get().getTransaction();
        try {
            transaction.begin();
            User regularUser = userService.getByUsername(username);

            assertNotNull("Regular user is expected to exist", regularUser);
            assertTrue("Regular user must be regular", regularUser.isUser());
            assertTrue("Password must be valid", userService.isValidPassword(regularUser, password));

            // Update to moderator.
            regularUser.setRole(UserRole.MODERATOR);
            userService.update(regularUser);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }

        try {
            // Re-read user as moderator.
            User moderator = userService.getByUsername(username);
            assertNotNull("Updated moderator is expected to exist", moderator);
            assertTrue("Moderator is expected", moderator.isModerator());

            goTo(getSignInUrl(getAdminUsersPageUrl()));

            assertTrue("No captcha.", webDriver.findElements(By.name("captchaCode")).isEmpty());

            // Apply existing user.
            getFormInput("emailOrUsername").sendKeys(username);
            getFormInput("password").sendKeys(password);

            click("#signInSubmit");

            assertFalse("Moderator is not allowed to access admin page",
                    webDriver.getCurrentUrl().contains(adminUsersRelativeUrl));
        } finally {
            // Restore previous role.
            transaction = entityManagerProvider.get().getTransaction();
            transaction.begin();
            User regularUser = userService.getByUsername(username);
            regularUser.setRole(UserRole.USER);
            userService.update(regularUser);
            transaction.commit();
        }
    }
}
