package dto.sso;

import java.io.Serializable;

/**
 * Authentication response.
 */
public class AuthenticationResponseDto implements Serializable {

    private String captchaUrl;
    private String token;
    private Long expires;
    private boolean captchaRequired;

    public String getCaptchaUrl() {
        return captchaUrl;
    }

    public void setCaptchaUrl(String captchaUrl) {
        this.captchaUrl = captchaUrl;
    }

    public boolean isCaptchaRequired() {
        return captchaRequired;
    }

    public void setCaptchaRequired(boolean captchaRequired) {
        this.captchaRequired = captchaRequired;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpires() {
        return expires;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }

    private static final long serialVersionUID = 1L;
}
