package dto.sso;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Validation tests.
 */
public class ValidationTest {

    @Test
    public void testUsernameRegExp_valid() {
        assertValidUsername("aop1968");
        assertValidUsername("aop_1968");
        assertValidUsername("aop.1968");
        assertValidUsername("test");
        assertValidUsername("test-me-again");
        assertValidUsername("test-me-again-");
        assertValidUsername("test-me-again_");
        assertValidUsername("GATE");
    }

    @Test
    public void testUsernameRegExp_invalid() {
        assertInvalidUsername("4GATE");
        assertInvalidUsername(".GATE");
        assertInvalidUsername("A..GATE");
        assertInvalidUsername("_GATE");
        assertInvalidUsername("GATE.");
    }

    @Test
    public void testEmailRegExp_invalid() {
        assertInvalidEmail("");
        assertInvalidEmail("@.");

        assertInvalidEmail(" user@example.org");
        assertInvalidEmail("user@example.org ");
        assertInvalidEmail("user @example.org");
        assertInvalidEmail("user@ example.org");
        assertInvalidEmail("user@example .org");
        assertInvalidEmail("user@example. org");

        assertInvalidEmail("user[alias]@example.org");

        assertInvalidEmail(".firstname.lastname@example.com");
        assertInvalidEmail(".firstnamelastname@example.com");
        assertInvalidEmail("lastname.@example.com");
    }

    @Test
    public void testEmailRegExp_valid() {
        assertValidEmail("user@example.org");
        assertValidEmail("user+alias@example.org");
        assertValidEmail("user+alias@example.bizinfo");
        assertValidEmail("Firstname_Lastname+alias@example.bizinfo");

        assertValidEmail("firstname.lastname@example.com");
        assertValidEmail("firstname.lastname@example.biz.info");
    }

    @Test
    public void testPhoneRegExp_valid() {
        assertValidPhone("867-5309");

        assertValidPhone("+1 650 699 4933");
        assertValidPhone("+16506994933");
        assertValidPhone("16506994933");

        assertValidPhone("+1 650 699 4933");

        assertValidPhone("+1 650 699 ext 4933");
        assertValidPhone("+1 650 699 ext.4933");
        assertValidPhone("+1 650 699 x4933");
        assertValidPhone("+1 650 699x4933");
    }

    @Test
    public void testPhoneRegExp_invalid() {
        assertInvalidPhone("+1 650 699 [4933]");
        assertInvalidPhone("+1 650 699 4933(");
        assertInvalidPhone(")1 650 699 4933");
    }


    /**
     * Asserts that given username is valid.
     *
     * @param username Username.
     */
    private void assertValidUsername(String username) {
        assertTrue(String.format("Username %s is expected to be valid.", username),
                Constants.USERNAME.matcher(username).matches());
    }

    /**
     * Asserts that given username is invalid.
     *
     * @param username Username.
     */
    private void assertInvalidUsername(String username) {
        assertFalse(String.format("Username %s is expected to be invalid.", username),
                Constants.USERNAME.matcher(username).matches());
    }

    /**
     * Asserts that given phone is valid.
     *
     * @param phone Phone.
     */
    private void assertValidPhone(String phone) {
        assertTrue(String.format("Phone %s is expected to be valid.", phone), Constants.PHONE.matcher(phone).matches());
    }

    /**
     * Asserts that given phone is invalid.
     *
     * @param phone Phone.
     */
    private void assertInvalidPhone(String phone) {
        assertFalse(String.format("Phone %s is expected to be invalid.", phone),
                Constants.PHONE.matcher(phone).matches());
    }

    /**
     * Asserts that given email is invalid.
     *
     * @param email Email to check.
     */
    private void assertInvalidEmail(String email) {
        assertFalse(String.format("Email %s is expected to be invalid.", email),
                Constants.EMAIL.matcher(email).matches());
    }

    /**
     * Asserts that given email is valid.
     *
     * @param email Email to check.
     */
    private void assertValidEmail(String email) {
        assertTrue(String.format("Email %s is expected to be valid.", email), Constants.EMAIL.matcher(email).matches());
    }
}
