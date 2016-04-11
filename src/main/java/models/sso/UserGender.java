package models.sso;

/**
 * User gender enumeration.
 */
public enum UserGender {

    /**
     * Male.
     */
    MALE,

    /**
     * Female.
     */
    FEMALE,

    /**
     * Other ("Prefer not to say").
     */
    OTHER;

    /**
     * Checks if the given string matches one of the enum constants.
     *
     * @param gender Gender string.
     * @return Whether the given string matches one of the enum constants.
     */
    public static boolean hasConstant(String gender) {
        if (gender == null) {
            return false;
        }
        try {
            UserGender.valueOf(gender);
            return true;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }
}
