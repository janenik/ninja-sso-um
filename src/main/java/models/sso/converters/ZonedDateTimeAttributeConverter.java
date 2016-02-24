package models.sso.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Converts {@link ZonedDateTime} to {@link Timestamp} and back. Uses UTC time zone to from {@link Timestamp}.
 * Consider removing this code after Hibernate 5.0 is used.
 */
@Converter(autoApply = true)
public class ZonedDateTimeAttributeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    private final ZoneId utc = ZoneId.of("UTC");

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        return (zonedDateTime == null ? null : Timestamp.valueOf(zonedDateTime.toLocalDateTime()));
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp sqlDate) {
        return (sqlDate == null ? null : ZonedDateTime.ofLocal(sqlDate.toLocalDateTime(), utc, null));
    }
}
