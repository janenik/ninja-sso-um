package models.sso;

/**
 * The role of the user. External users coming from OAuth authentication like Google, Facebook, etc. are expected to
 * have their own roles.
 */
public enum UserRole {
    /**
     * Administrator is able to sign in into the admin part of the application.
     */
    ADMIN,

    /**
     * Moderator is a user with abilities to moderate other users' behavior.
     */
    MODERATOR,

    /**
     * Regular user.
     */
    USER;
}
