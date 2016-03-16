package models.sso;

/**
 * Enumeration of events: sign-in, sign-up, entity update (password change, fields change), etc.
 */
public enum UserEventType {
    SIGN_IN,
    SIGN_UP,
    CONFIRMATION,
    PASSWORD_CHANGE,
    UPDATE,
    REMOVE,
    ACCESS
}
