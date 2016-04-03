package dto.sso;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User sign up DTO.
 */
public class UserSignUpDto implements Serializable {

    @Pattern(regexp = Constants.EMAIL_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.EMAIL_MIN_LENGTH, max = Constants.EMAIL_MAX_LENGTH)
    String email;
    @Pattern(regexp = Constants.USERNAME_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.USERNAME_MIN_LENGTH, max = Constants.USERNAME_MAX_LENGTH)
    String username;
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.FIRST_NAME_MAX_LENGTH)
    String firstName;
    @Size(max = Constants.MIDDLE_NAME_MAX_LENGTH)
    String middleName;
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.LAST_NAME_MAX_LENGTH)
    String lastName;
    @Pattern(regexp = Constants.PHONE_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.PHONE_MAX_LENGTH)
    String phone;
    @NotBlank
    @NotNull
    @Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.PASSWORD_MAX_LENGTH)
    String password;
    @NotBlank
    @NotNull
    @Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.PASSWORD_MAX_LENGTH)
    String passwordRepeat;
    @NotBlank
    @Size(max = Constants.CAPTCHA_MAX_LENGTH)
    String captchaCode;
    @NotNull
    @Size(min = 3, max = Constants.TOKEN_MAX_LENGTH)
    String token;
    String agreement; // To check after.
    // This one to skip the exception in log.
    String lang;
    String scope;
    Integer countryId;

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
     * Returns scope.
     *
     * @return Scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets scope.
     *
     * @param scope Scope.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Returns country id.
     *
     * @return Country id.
     */
    public Integer getCountryId() {
        return countryId;
    }

    /**
     * Sets country id.
     *
     * @param countryId Country id.
     */
    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    private static final long serialVersionUID = 1L;
}
