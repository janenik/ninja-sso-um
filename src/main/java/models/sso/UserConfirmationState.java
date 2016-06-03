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
}
