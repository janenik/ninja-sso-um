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

    /**
     * Converts given string to {@link UserSignInState} enum. If the given string doesn't represent enum's
     * string value, then the following value returned: {@link #DISABLED}.
     *
     * @param userSignInStateAsString User sign-in state as string.
     * @return User sign-in state from given string.
     */
    public static UserSignInState fromString(String userSignInStateAsString) {
        try {
            return UserSignInState.valueOf(userSignInStateAsString);
        } catch (Exception e) {
            return UserSignInState.DISABLED;
        }
    }
}
