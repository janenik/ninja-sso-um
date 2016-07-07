package dto.sso.admin;

import dto.sso.common.Constants;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User edit DTO.
 */
public class UserEditDto implements Serializable {

    /**
     * Email.
     */
    @Pattern(regexp = Constants.EMAIL_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.EMAIL_MIN_LENGTH, max = Constants.EMAIL_MAX_LENGTH)
    String email;

    /**
     * Username.
     */
    @Pattern(regexp = Constants.USERNAME_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.USERNAME_MIN_LENGTH, max = Constants.USERNAME_MAX_LENGTH)
    String username;

    /**
     * First name.
     */
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.FIRST_NAME_MAX_LENGTH)
    String firstName;

    /**
     * Middle name, optional.
     */
    @Size(max = Constants.MIDDLE_NAME_MAX_LENGTH)
    String middleName;

    /**
     * Last name.
     */
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.LAST_NAME_MAX_LENGTH)
    String lastName;

    /**
     * Phone.
     */
    @Pattern(regexp = Constants.PHONE_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = 5, max = Constants.PHONE_MAX_LENGTH)
    String phone;

    /**
     * Country id (ISO code).
     */
    @NotBlank
    @NotNull
    @Size(min = 2, max = Constants.COUNTRY_ISO_MAX_LENGTH)
    String countryId;

    /**
     * Password in clear form.
     */
    @NotBlank
    @NotNull
    @Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.PASSWORD_MAX_LENGTH)
    String password;

    /**
     * Repeat password in clear form.
     */
    @NotBlank
    @NotNull
    @Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.PASSWORD_MAX_LENGTH)
    String passwordRepeat;

    /**
     * User gender.
     */
    @NotBlank
    @NotNull
    @Size(min = 4, max = Constants.ENUM_MAX_LENGTH)
    String gender;

    /**
     * Birth month.
     */
    @NotNull
    @Range(min = 1, max = 12)
    Integer birthMonth;

    /**
     * Birth day.
     */
    @NotNull
    @Range(min = 1, max = 31)
    Integer birthDay;

    /**
     * Birth year.
     */
    @NotNull
    @Min(1800)
    Integer birthYear;
}
