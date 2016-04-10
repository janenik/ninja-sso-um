package dto.sso;

import java.util.regex.Pattern;

/**
 * Constants for DTO and validation. Must match the data in {@link models.sso.User}. Please, verify the validation tests
 * after modification.
 */
public interface Constants {

    /**
     * Email pattern, as string. Simple sub-set with US ASCII characters, excluding characters like {}#, etc.
     */
    String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Email pattern.
     */
    Pattern EMAIL = Pattern.compile(EMAIL_PATTERN);

    /**
     * Email min length.
     */
    int EMAIL_MIN_LENGTH = 5;

    /**
     * Email max length.
     */
    int EMAIL_MAX_LENGTH = 255;

    /**
     * Username pattern, as string.
     */
    String USERNAME_PATTERN = "^[A-Za-z]+(\\.?[_A-Za-z0-9-]+)*$";

    /**
     * Username pattern.
     */
    Pattern USERNAME = Pattern.compile(USERNAME_PATTERN);

    /**
     * Min length for the username.
     */
    int USERNAME_MIN_LENGTH = 4;

    /**
     * Max length for the username.
     */
    int USERNAME_MAX_LENGTH = 255;

    /**
     * Pattern for phone, as string.
     */
    String PHONE_PATTERN = "^(?:(?:\\(?(?:00|\\+)([1-4]\\d\\d|[1-9]\\d?)\\)?)?[\\-\\.\\ \\\\\\/]?)?((?:\\(?\\d{1,}\\)" +
            "?[\\-\\.\\ \\\\\\/]?){0,})(?:[\\-\\.\\ \\\\\\/]?(?:#|ext\\.?|extension|x)[\\-\\.\\ \\\\\\/]?(\\d+))?$";

    /**
     * Max length for the phone.
     */
    int PHONE_MAX_LENGTH = 50;

    /**
     * Pattern for phone.
     */
    Pattern PHONE = Pattern.compile(PHONE_PATTERN);

    /**
     * Max length for the first name.
     */
    int FIRST_NAME_MAX_LENGTH = 100;

    /**
     * Max length for the middle name.
     */
    int MIDDLE_NAME_MAX_LENGTH = 100;

    /**
     * Max length for the last name.
     */
    int LAST_NAME_MAX_LENGTH = 100;

    /**
     * Password min length.
     */
    int PASSWORD_MIN_LENGTH = 8;

    /**
     * Password min length.
     */
    int PASSWORD_MAX_LENGTH = 100;

    /**
     * Max length of the captcha field.
     */
    int CAPTCHA_MAX_LENGTH = 20;

    /**
     * Maximum enumeration length (string representation).
     */
    int ENUM_MAX_LENGTH = 64;

    /**
     * Country max ISO code length.
     */
    int COUNTRY_ISO_MAX_LENGTH = 2;

    /**
     * Token max length.
     */
    int TOKEN_MAX_LENGTH = 32 * 1024;
}
