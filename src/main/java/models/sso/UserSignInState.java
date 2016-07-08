package models.sso;

/**
 * User sign in state.
 */
public enum UserSignInState {

    /**
     * Sign in is enabled (default).
     */
    ENABLED,

    /**
     * Sign in as user only. For cases when admin/moderator is suspended.
     */
    ENABLED_AS_USER,

    /**
     * Sign in is disabled.
     */
    DISABLED;
}
