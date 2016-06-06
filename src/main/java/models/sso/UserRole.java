package models.sso;

/**
 * The role of the user. External users coming from OAuth authentication like Google, Facebook, etc. are expected to
 * have their own roles.
 */
public enum UserRole {

    /**
     * Administrator is able to sign in into the admin part of the application. Highest possible privilege.
     */
    ADMIN,

    /**
     * Moderator is a user with abilities to moderate other users' behavior.
     */
    MODERATOR,

    /**
     * Regular user. Least possible privilege.
     */
    USER;

    /**
     * Converts given string to {@link UserRole} enum. If the given string doesn't represent enum's
     * string value, then the least possible privilege returned {@link #USER}.
     *
     * @param userRoleAsString User role as string.
     * @return User role from given string.
     */
    public static UserRole fromString(String userRoleAsString) {
        try {
            return UserRole.valueOf(userRoleAsString);
        } catch (Exception e) {
            return UserRole.USER;
        }
    }
}
