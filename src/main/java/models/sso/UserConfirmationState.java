package models.sso;

/**
 * The confirmation state of the user: whether the user confirmed his contact (phone or email).
 */
public enum UserConfirmationState {

    /**
     * Email or phone confirmed.
     */
    CONFIRMED,

    /**
     * Unconfirmed.
     */
    UNCONFIRMED;

    /**
     * Converts given string to {@link UserConfirmationState} enum. If the given string doesn't represent enum's
     * string value, then the following value returned: {@link #UNCONFIRMED}.
     *
     * @param userConfirmationStateAsString User confirmation state as string.
     * @return User confirmation state from given string.
     */
    public static UserConfirmationState fromString(String userConfirmationStateAsString) {
        try {
            return UserConfirmationState.valueOf(userConfirmationStateAsString);
        } catch (Exception e) {
            return UserConfirmationState.UNCONFIRMED;
        }
    }
}
