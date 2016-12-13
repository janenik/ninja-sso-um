package controllers.sso.auth.state;

import com.google.common.base.Optional;
import ninja.i18n.Messages;

/**
 * Sign in state.
 */
public enum SignInState {

    /**
     * Email is confirmed.
     */
    EMAIL_VERIFICATION_CONFIRMED(true),

    /**
     * Email changed.
     */
    EMAIL_VERIFICATION_EMAIL_CHANGED(true),

    /**
     * Email verification failed.
     */
    EMAIL_VERIFICATION_FAILED(false),

    /**
     * Successful sign up state.
     */
    SUCCESSFUL_SIGN_UP(true),

    /**
     * Password was changed successfully.
     */
    PASSWORD_CHANGED(true),

    /**
     * Forgot password email has been sent.
     */
    FORGOT_EMAIL_SENT(true);

    /**
     * Whether the current state is successful.
     */
    final boolean successful;

    /**
     * Constructor for state.
     *
     * @param success Success state.
     */
    SignInState(boolean success) {
        this.successful = success;
    }

    /**
     * Returns corresponding translated message for the state.
     *
     * @param messages Application messages.
     * @param langCode Language code.
     * @return Translated state message.
     */
    public String getMessage(Messages messages, String langCode) {
        return messages.getWithDefault("signInState_" + toString().toLowerCase(), "unknown", Optional.<String>of
                (langCode));
    }

    /**
     * Whether the state is successful state.
     *
     * @return Whether the current state is successful.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Constructs a state from given string. No exceptions are thrown, null returned when a given string
     * is not a valid state.
     *
     * @param stateString State as string.
     * @return Sign in state from string or null if the given string is not a valid state.
     */
    public static SignInState fromString(String stateString) {
        if (stateString == null || stateString.isEmpty()) {
            return null;
        }
        try {
            return SignInState.valueOf(stateString.toUpperCase());
        } catch (Exception e) {
            // Ignore.
        }
        return null;
    }
}
