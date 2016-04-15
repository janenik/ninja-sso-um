package dto.sso;

import com.google.common.base.Strings;

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

    public String getEmailOrUsername() {
        return Strings.nullToEmpty(emailOrUsername).trim().toLowerCase();
    }

    public void setEmailOrUsername(String email) {
        this.emailOrUsername = email;
    }

    public String getCaptchaCode() {
        return Strings.nullToEmpty(captchaCode).trim();
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getCaptchaToken() {
        return Strings.nullToEmpty(captchaToken).trim();
    }

    public void setCaptchaToken(String token) {
        this.captchaToken = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private static final long serialVersionUID = 1L;
}
