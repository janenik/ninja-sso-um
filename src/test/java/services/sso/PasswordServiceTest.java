package services.sso;

import models.sso.User;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PasswordServiceTest {

    PasswordService passwordService = new PasswordService();

    @Test
    public void testShortPassword() {
        String password = "shortPassword!@#$%^&*()_+";

        User user = new User();
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));

        assertTrue("Correct password expected.", passwordService.isValidPassword(password, user));
    }

    @Test
    public void testLongPassword() {
        StringBuilder passwordBuilder = new StringBuilder("longPassword!@#$%^&*()_+");
        for (int i = 0; i < 1024; i++) {
            passwordBuilder.append(i);
        }
        String password = passwordBuilder.toString();

        User user = new User();
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));

        assertTrue("Correct password expected.", passwordService.isValidPassword(password, user));
    }
}
