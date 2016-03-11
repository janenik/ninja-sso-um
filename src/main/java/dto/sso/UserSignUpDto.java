package dto.sso;

import com.google.common.base.Strings;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User sign-up DTO.
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
    String redirectUrl;
    String scope;

    /**
     * Default constructor.
     */
    public UserSignUpDto() {
    }

    /**
     * Creates DTO object with default value for all fields.
     *
     * @param defaultValue Default value.
     */
    public UserSignUpDto(String defaultValue) {
        email = defaultValue;
        firstName = defaultValue;
        lastName = defaultValue;
        password = defaultValue;
        passwordRepeat = defaultValue;
        captchaCode = defaultValue;
        phone = defaultValue;
        token = defaultValue;
    }

    public String getEmail() {
        return Strings.nullToEmpty(email).trim().toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return Strings.nullToEmpty(firstName).trim();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return Strings.nullToEmpty(lastName).trim();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return Strings.nullToEmpty(phone).trim();
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    private static final long serialVersionUID = 1L;
}
