package dto.sso;

import com.google.common.base.Strings;
import dto.sso.common.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User sign-in DTO.
 */
public class UserSignInDto implements Serializable {

    /**
     * Email or username field.
     */
    @Size(min = Constants.USERNAME_MIN_LENGTH, max = Constants.USERNAME_MAX_LENGTH)
    @NotNull
    String emailOrUsername;

    /**
     * Password field.
     */
    @Size(max = Constants.PASSWORD_MAX_LENGTH)
    String password;

    /**
     * Captcha token.
     */
    @Size(max = Constants.TOKEN_MAX_LENGTH)
    String captchaToken;

    /**
     * Captcha code, user input.
     */
    @Size(max = Constants.CAPTCHA_MAX_LENGTH)
    String captchaCode;

    /**
     * Returns email or username field.
     *
     * @return Email or username field value.
     */
    public String getEmailOrUsername() {
        return Strings.nullToEmpty(emailOrUsername).trim().toLowerCase();
    }

    /**
     * Returns captcha code, entered by user.
     *
     * @return Captcha code.
     */
    public String getCaptchaCode() {
        return Strings.nullToEmpty(captchaCode).trim();
    }

    /**
     * Returns captcha token.
     *
     * @return Captcha token.
     */
    public String getCaptchaToken() {
        return Strings.nullToEmpty(captchaToken).trim();
    }

    /**
     * Sets emailOrUsername.
     *
     * @param emailOrUsername EmailOrUsername.
     */
    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
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
     * Sets captcha token.
     *
     * @param captchaToken Captcha token.
     */
    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }

    /**
     * Sets captcha code.
     *
     * @param captchaCode Captcha code.
     */
    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    private static final long serialVersionUID = 1L;
}
