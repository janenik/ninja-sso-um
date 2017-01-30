package dto.sso.common;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;

/**
 * Mixin for entities with birthday.
 */
public interface WithBirthDay {

    /**
     * Getter for birth year.
     *
     * @return Birth year or null if not set.
     */
    Integer getBirthYear();

    /**
     * Getter for birth month.
     *
     * @return Birth month or null if not set.
     */
    Integer getBirthMonth();

    /**
     * Getter for birth day of month.
     *
     * @return Birth day of month or null if not set.
     */
    Integer getBirthDay();

    default LocalDate getLocalDateBirthday() {
        return LocalDate.of(getBirthYear(), getBirthMonth(), getBirthDay());
    }

    /**
     * Checks if the current birthday is a valid date.
     *
     * @return Whether the current birthday is a valid date.
     */
    default boolean isValidBirthday() {
        Integer year = getBirthYear();
        Integer month = getBirthMonth();
        Integer day = getBirthDay();
        if (year == null || month == null || day == null) {
            return false;
        }
        if (!ChronoField.YEAR.range().isValidIntValue(year)
                || !ChronoField.MONTH_OF_YEAR.range().isValidIntValue(month)
                || !ChronoField.DAY_OF_MONTH.range().isValidIntValue(day)) {
            return false;
        }
        if (day <= 28) {
            return true;
        }
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (DateTimeException dte) {
            return false;
        }
    }
}
