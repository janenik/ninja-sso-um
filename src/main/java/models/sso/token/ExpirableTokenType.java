package models.sso.token;

/**
 * Expirable token type.
 */
public enum ExpirableTokenType {

    /**
     * Auth token.
     */
    AUTH,

    /**
     * Refresh token.
     */
    REFRESH,

    /**
     * XSRF token: token is placed into the cookies and into the form.
     * If submitted token value doesn't match value in cookies, the form is discarded.
     */
    XSRF,

    /**
     * Confirm email (sent to user via email).
     */
    CONFIRM_EMAIL,

    /**
     * Forgot password (sent to user via email).
     * When forgot password email sent.
     */
    FORGOT_PASSWORD,

    /**
     * Confirm password (sent to user via email).
     * When changing emails.
     */
    CONFIRM_PASSWORD_CHANGE,

    /**
     * Custom token type.
     */
    CUSTOM;
}
