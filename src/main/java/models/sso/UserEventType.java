package models.sso;

/**
 * Enumeration of events: sign-in, sign-up, entity update (password change, fields change), etc.
 */
public enum UserEventType {

    /**
     * Sign in event.
     */
    SIGN_IN,

    /**
     * Sign up event.
     */
    SIGN_UP,

    /**
     * Confirmation.
     */
    CONFIRMATION,

    /**
     * Password change event.
     */
    PASSWORD_CHANGE,

    /**
     * Update event.
     */
    UPDATE,

    /**
     * Disable sign in event.
     */
    DISABLE_SIGN,

    /**
     * Enable sign in event.
     */
    ENABLE_SIGN,

    /**
     * Access event.
     */
    ACCESS,

    /**
     * Access to user events.
     */
    EVENTS_ACCESS,

    /**
     * Role change event.
     */
    ROLE_CHANGE;
}