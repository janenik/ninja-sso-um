package dto.sso;

import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User sign-in DTO.
 */
public class UserSignInDto implements Serializable {

    @Size(min = Constants.USERNAME_MIN_LENGTH, max = Constants.USERNAME_MAX_LENGTH)
    @NotNull
    String emailOrUsername;
    @Size(max = Constants.PASSWORD_MAX_LENGTH)
    String password;
    @Size(max = Constants.TOKEN_MAX_LENGTH)
    String token;
    @Size(max = Constants.CAPTCHA_MAX_LENGTH)
    String captchaCode;
    // This one to skip the exception in log.
    String lang;
    String redirectUrl;
    String project;

    public UserSignInDto() {
    }

    public UserSignInDto(String defaultValue) {
        emailOrUsername = defaultValue;
        password = defaultValue;
        token = defaultValue;
        captchaCode = defaultValue;
        lang = defaultValue;
    }

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

    public String getToken() {
        return Strings.nullToEmpty(token).trim();
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    private static final long serialVersionUID = 1L;
}
