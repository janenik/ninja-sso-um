package dto.sso.admin.users;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Data transfer object for user event to view access log of a single user.
 */
public final class ViewAccessLogDto implements Serializable {

    Long id;

    String eventType;

    String ip;

    String url;

    ZonedDateTime time;

    Long targetUserId;

    Long targetUsername;

    Long targetUserFullName;

    String data;
}
