package dto.sso.admin.users;

import dto.sso.common.Constants;
import dto.sso.common.WithBirthDay;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User edit personal data DTO.
 */
public final class EditPersonalDataDto implements WithBirthDay, Serializable {

    /**
     * Username.
     */
    @Pattern(regexp = Constants.USERNAME_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.USERNAME_MIN_LENGTH, max = Constants.USERNAME_MAX_LENGTH)
    private String username;

    /**
     * First name.
     */
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.FIRST_NAME_MAX_LENGTH)
    private String firstName;

    /**
     * Middle name, optional.
     */
    @Size(max = Constants.MIDDLE_NAME_MAX_LENGTH)
    private String middleName;

    /**
     * Last name.
     */
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.LAST_NAME_MAX_LENGTH)
    private String lastName;

    /**
     * User gender.
     */
    @NotBlank
    @NotNull
    @Size(min = 4, max = Constants.ENUM_MAX_LENGTH)
    private String gender;

    /**
     * Birth month.
     */
    @NotNull
    @Range(min = 1, max = 12)
    private Integer birthMonth;

    /**
     * Birth day.
     */
    @NotNull
    @Range(min = 1, max = 31)
    private Integer birthDay;

    /**
     * Birth year.
     */
    @NotNull
    @Min(1800)
    private Integer birthYear;

    /**
     * Returns username.
     *
     * @return Username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username Username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns first name.
     *
     * @return First name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets first name.
     *
     * @param firstName First name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns middle name.
     *
     * @return Middle name.
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets middle name.
     *
     * @param middleName Middle name.
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Returns last name.
     *
     * @return Last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets last name.
     *
     * @param lastName Last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns gender.
     *
     * @return Gender.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets gender.
     *
     * @param gender Gender.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Returns birth month.
     *
     * @return Birth month.
     */
    @Override
    public Integer getBirthMonth() {
        return birthMonth;
    }

    /**
     * Sets birth month.
     *
     * @param birthMonth Birth month.
     */
    public void setBirthMonth(Integer birthMonth) {
        this.birthMonth = birthMonth;
    }

    /**
     * Returns birth day.
     *
     * @return Birth day.
     */
    @Override
    public Integer getBirthDay() {
        return birthDay;
    }

    /**
     * Sets birthDay.
     *
     * @param birthDay BirthDay.
     */
    public void setBirthDay(Integer birthDay) {
        this.birthDay = birthDay;
    }

    /**
     * Returns birth year.
     *
     * @return Birth year.
     */
    @Override
    public Integer getBirthYear() {
        return birthYear;
    }

    /**
     * Sets birth year.
     *
     * @param birthYear Birth year.
     */
    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }
}
