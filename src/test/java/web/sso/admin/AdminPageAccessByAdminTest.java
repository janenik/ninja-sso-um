package web.sso.admin;

import com.google.inject.Injector;
import controllers.sso.admin.users.UsersController;
import models.sso.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import services.sso.UserService;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests access of administration pages by administration user.
 */
public class AdminPageAccessByAdminTest extends AdminWebDriverTest {

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

    /**
     * Administration.
     */
    private User admin;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();
        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.adminUsersRelativeUrl = reverseRouter.with(UsersController::users).build();
        this.username = "root";
        this.password = properties.getWithDefault("application.root.defaultPassword", "+1 650-999-9999");

        // Make root user admin.
        EntityTransaction transaction = entityManagerProvider.get().getTransaction();
        try {
            transaction.begin();
            this.admin = userService.getByUsername(username);

            assertNotNull("Root user is expected to exist", this.admin);
            assertTrue("Password must be valid", userService.isValidPassword(this.admin, password));

            // Update to admin.
            this.admin.setRole(UserRole.ADMIN);
            userService.update(this.admin);
            transaction.commit();

            // Sign in the admin.
            signIn();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        // Restore previous role.
        EntityTransaction transaction = entityManagerProvider.get().getTransaction();
        transaction.begin();
        User regularUser = userService.getByUsername(username);
        regularUser.setRole(UserRole.USER);
        userService.update(regularUser);
        transaction.commit();

        // Log out.
        click("#signOutLink");
    }

    @Test
    public void testAdminHasAccessToUsersPage() {
        List<TableUser> usersOnPage = getUsersFromTable();
        assertTrue("More than 1 users expected", usersOnPage.size() > 1);
        assertTrue("Contains root user",
                usersOnPage.stream()
                        .filter(u -> username.equals(u.username))
                        .findFirst().isPresent());

        TableUser userToEdit = usersOnPage.stream()
                .filter(u -> !username.equals(u.username))
                .findFirst()
                .get();

        performPersonalDataPageTest(userToEdit);
        performContactDataPageTest(userToEdit);
        performAccessPageTest(userToEdit);
        performResetPasswordPageTest(userToEdit);
    }

    /**
     * Performs personal page test.
     *
     * @param user User to edit.
     */
    private void performPersonalDataPageTest(TableUser user) {
        goTo(getAdminPersonalDataPageUrl(user.id));

        WebElement submitButton = webDriver.findElement(By.id("editPersonalSubmit"));
        assertNotNull("Personal data page with submit button expected.", submitButton);

        // Update data.
        String expectedFirstName = user.firstName + "_edited";
        setFormInputValue("firstName", expectedFirstName);

        submitButton.click();

        // Reload and re-read the updated first name.
        goTo(getAdminPersonalDataPageUrl(user.id));

        String updatedFirstName = getFormInputValue("firstName");
        assertEquals("First name must be updated.", updatedFirstName, expectedFirstName);
    }

    /**
     * Performs contact page test.
     *
     * @param user User to edit.
     */
    private void performContactDataPageTest(TableUser user) {
        goTo(getAdminContactDataPageUrl(user.id));

        WebElement submitButton = webDriver.findElement(By.id("editContactSubmit"));
        assertNotNull("Contact data page with submit button expected.", submitButton);

        // Update data.
        String expectedEmail = "edited_" + user.email;
        setFormInputValue("email", expectedEmail);

        submitButton.click();

        // Reload and re-read the updated email.
        goTo(getAdminContactDataPageUrl(user.id));

        String updatedFirstName = getFormInputValue("email");
        assertEquals("First name must be updated.", updatedFirstName, expectedEmail);
    }

    /**
     * Performs access page test.
     *
     * @param user User to edit.
     */
    private void performAccessPageTest(TableUser user) {
        goTo(getAdminAccessPageUrl(user.id));

        WebElement submitButton = webDriver.findElement(By.id("editRoleSubmit"));
        assertNotNull("Access page with submit button expected.", submitButton);

        String currentRole = getFormInputValue("role");
        String currentSignInState = getFormInputValue("signInState");
        String currentConfirmationState = getFormInputValue("confirmationState");

        assertEquals("Test user role is expected to be USER.", UserRole.USER.name(), currentRole);
        assertEquals("Test user sign in state is expected to be ENABLED.",
                UserSignInState.ENABLED.name(), currentSignInState);
        assertEquals("Test user confirmation state is expected to be CONFIRMED.",
                UserConfirmationState.CONFIRMED.name(), currentConfirmationState);

        // Update data.
        setFormInputValue("role", UserRole.MODERATOR.name());
        setFormInputValue("signInState", UserSignInState.DISABLED.name());

        submitButton.click();

        // Reload and re-read the updated data.
        goTo(getAdminAccessPageUrl(user.id));

        String updatedRole = getFormInputValue("role");
        String updatedSignInState = getFormInputValue("signInState");
        String updatedConfirmationState = getFormInputValue("confirmationState");

        assertEquals("Updated test user role is expected to be USER.", UserRole.MODERATOR.name(), updatedRole);
        assertEquals("Updated test user sign in state is expected to be ENABLED.",
                UserSignInState.DISABLED.name(), updatedSignInState);
        assertEquals("Updated test user confirmation state is expected to be CONFIRMED.",
                UserConfirmationState.CONFIRMED.name(), updatedConfirmationState);
    }

    /**
     * Performs reset password page test.
     *
     * @param user User to edit.
     */
    private void performResetPasswordPageTest(TableUser user) {
        goTo(getAdminResetPasswordPageUrl(user.id));

        WebElement submitButton = webDriver.findElement(By.id("restorePasswordSubmit"));
        assertNotNull("Reset password page with submit button expected.", submitButton);

        String expectedPassword = "testPasswordFor" + user.email;
        setFormInputValue("password", expectedPassword);
        setFormInputValue("confirmPassword", expectedPassword);

        submitButton.click();

        EntityManager em = entityManagerProvider.get();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            // Make sure credentials are read from DB, not cached in persistent context.
            em.detach(userService.getCredentials(user.id));
            assertTrue("Updated password is expected to be correct.",
                    userService.isValidPassword(userService.get(user.id), expectedPassword));
        } finally {
            transaction.commit();
        }
    }

    /**
     * Returns users from table on the page.
     *
     * @return Parsed users.
     */
    private List<TableUser> getUsersFromTable() {
        WebElement tableWithUsers = webDriver.findElement(By.id("itemsList"));
        return TableUser.fromWebElements(tableWithUsers.findElements(By.tagName("td")));
    }

    /**
     * Loads sign-in page, populates username and password and authenticates root user.
     */
    private void signIn() {
        assertTrue("Root user must be an admin", this.admin.isAdmin());
        goTo(getSignInUrl(getAdminUsersPageUrl()));
        assertTrue("No captcha.", webDriver.findElements(By.name("captchaCode")).isEmpty());

        // Apply existing user.
        getFormInput("emailOrUsername").sendKeys(username);
        getFormInput("password").sendKeys(password);

        click("#signInSubmit");

        assertTrue("Admin is allowed to access admin page",
                webDriver.getCurrentUrl().contains(adminUsersRelativeUrl));
    }

    /**
     * User data from table of users.
     */
    private static class TableUser {
        Long id;
        String username;
        String email;
        String firstName;
        String lastName;
        String role;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass() || id == null) {
                return false;
            }
            TableUser tableUser = (TableUser) o;
            return id.equals(tableUser.id);
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        /**
         * Builds a list of users from given list of table cells.
         *
         * @param tableCells Table cells.
         * @return Users parsed from table cells.
         */
        static List<TableUser> fromWebElements(List<WebElement> tableCells) {
            int index = 0;
            List<TableUser> users = new ArrayList<>(20);
            while (index < tableCells.size()) {
                TableUser user = fromWebElements(tableCells, index);
                users.add(user);
                index += 8;
            }
            return users;
        }

        /**
         * Constructs user object from given list of HTML table cells.
         *
         * @param tableCells List of table cells.
         * @param index      Index of cell containing ID of the user (starts sequence of cells with user info).
         * @return User object from HTML table cells.
         * @throws IllegalAccessException If tableCells contains less cells than required to read a user or
         *                                user email cell check fails.
         */
        static TableUser fromWebElements(List<WebElement> tableCells, int index) {
            if (tableCells.size() <= index + 5) {
                throw new IllegalArgumentException("Not enough cells in cells' list to build a user.");
            }
            String email = tableCells.get(index + 2).getText();
            if (email.indexOf('@') < 0) {
                throw new IllegalArgumentException("Unexpected index of email cell.");
            }
            TableUser user = new TableUser();
            user.id = Long.parseLong(tableCells.get(index).getText());
            user.username = tableCells.get(index + 1).getText();
            user.email = email;
            user.firstName = tableCells.get(index + 3).getText();
            user.lastName = tableCells.get(index + 4).getText();
            user.role = tableCells.get(index + 5).getText();
            return user;
        }
    }
}