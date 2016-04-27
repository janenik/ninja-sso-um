package dto.sso;

import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Forgot password DTO.
 */
public class ForgotPasswordDto implements Serializable {

    /**
     * Email or username.
     */
    @Size(min = Constants.USERNAME_OR_EMAIL_MIN_LENGTH, max = Constants.USERNAME_OR_EMAIL_MAX_LENGTH)
    @NotNull
    String emailOrUsername;

    /**
     * Captcha token.
     */
    @Size(max = Constants.TOKEN_MAX_LENGTH)
    @NotNull
    String captchaToken;

    /**
     * Captcha code, user input.
     */
    @Size(max = Constants.CAPTCHA_MAX_LENGTH)
    @NotNull
    String captchaCode;

    /**
     * Email or username.
     *
     * @return Email or username.
     */
    public String getEmailOrUsername() {
        return Strings.nullToEmpty(emailOrUsername).trim().toLowerCase();
    }

    /**
     * Returns captcha code entered by user.
     *
     * @return Captcha code.
     */
    public String getCaptchaCode() {
        return Strings.nullToEmpty(captchaCode).trim();
    }

    /**
     * Sets email or username.
     *
     * @param emailOrUsername Email or username.
     */
    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    /**
     * Returns captchaToken.
     *
     * @return CaptchaToken.
     */
    public String getCaptchaToken() {
        return captchaToken;
    }

    /**
     * Sets captchaToken.
     *
     * @param captchaToken CaptchaToken.
     */
    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
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
     * Returns serialVersionUID.
     *
     * @return SerialVersionUID.
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    private static final long serialVersionUID = 1L;
}
