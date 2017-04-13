package services.sso;

import models.sso.User;
import models.sso.UserCredentials;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PasswordService}.
 */
public class PasswordServiceTest {

    PasswordService passwordService = new PasswordService();

    @Test
    public void testShortPassword() {
        String password = "shortPassword!@#$%^&*()_+";

        UserCredentials credentials = new UserCredentials();
        credentials.setPasswordSalt(passwordService.newSalt());
        credentials.setPasswordHash(passwordService.passwordHash(password, credentials.getPasswordSalt()));

        assertTrue("Correct password expected.", passwordService.isValidPassword(password, credentials));
    }

    @Test
    public void testLongPassword() {
        StringBuilder passwordBuilder = new StringBuilder("longPassword!@#$%^&*()_+");
        for (int i = 0; i < 1024; i++) {
            passwordBuilder.append(i);
        }
        String password = passwordBuilder.toString();

        UserCredentials credentials = new UserCredentials();
        credentials.setPasswordSalt(passwordService.newSalt());
        credentials.setPasswordHash(passwordService.passwordHash(password, credentials.getPasswordSalt()));

        assertTrue("Correct password expected.", passwordService.isValidPassword(password, credentials));
    }
}
