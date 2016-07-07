package dto.sso;

import dto.sso.common.Constants;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User sign up DTO.
 */
public class UserSignUpDto implements Serializable {

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
     * Captcha code, entered by user.
     */
    @NotBlank
    @Size(max = Constants.CAPTCHA_MAX_LENGTH)
    String captchaCode;

    /**
     * Captcha token.
     */
    @NotBlank
    @NotNull
    @Size(min = 3, max = Constants.TOKEN_MAX_LENGTH)
    String token;

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

    /**
     * User agreement checkbox value.
     */
    @NotBlank
    @NotNull
    @Size(min = 3, max = Constants.TOKEN_MAX_LENGTH)
    String agreement;

    /**
     * Language field.
     */
    String lang;

    /**
     * Returns email.
     *
     * @return Email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email Email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

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
     * Returns firstName.
     *
     * @return FirstName.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets firstName.
     *
     * @param firstName FirstName.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns middleName.
     *
     * @return MiddleName.
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets middleName.
     *
     * @param middleName MiddleName.
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Returns lastName.
     *
     * @return LastName.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets lastName.
     *
     * @param lastName LastName.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns phone.
     *
     * @return Phone.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets phone.
     *
     * @param phone Phone.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns password.
     *
     * @return Password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets password.
     *
     * @param password Password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns passwordRepeat.
     *
     * @return PasswordRepeat.
     */
    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    /**
     * Sets passwordRepeat.
     *
     * @param passwordRepeat PasswordRepeat.
     */
    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    /**
     * Returns captchaCode.
     *
     * @return CaptchaCode.
     */
    public String getCaptchaCode() {
        return captchaCode;
    }

    /**
     * Sets captchaCode.
     *
     * @param captchaCode CaptchaCode.
     */
    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    /**
     * Returns token.
     *
     * @return Token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets token.
     *
     * @param token Token.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns agreement.
     *
     * @return Agreement.
     */
    public String getAgreement() {
        return agreement;
    }

    /**
     * Sets agreement.
     *
     * @param agreement Agreement.
     */
    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    /**
     * Returns lang.
     *
     * @return Lang.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets lang.
     *
     * @param lang Lang.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Returns country id.
     *
     * @return Country id.
     */
    public String getCountryId() {
        return countryId;
    }

    /**
     * Sets country id.
     *
     * @param countryId Country id.
     */
    public void setCountryId(String countryId) {
        this.countryId = countryId;
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
    public Integer getBirthDay() {
        return birthDay;
    }

    /**
     * Sets birth day.
     *
     * @param birthDay Birth day.
     */
    public void setBirthDay(Integer birthDay) {
        this.birthDay = birthDay;
    }

    /**
     * Returns birth year.
     *
     * @return Birth year.
     */
    public Integer getBirthYear() {
        return birthYear;
    }

    /**
     * Sets birth year.
     *
     * @param birthYear BirthYear.
     */
    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    private static final long serialVersionUID = 1L;
}
