package models.sso;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Auth event. Allows to log times of registration, access, updates, authentication, password changes, etc.
 */
@Entity
@Table(name = "userEvents", indexes = {
        @Index(name = "userTypeTime_idx", columnList = "user_id,type,time")
})
@NamedQueries({
        @NamedQuery(name = "UserEvent.allByUser",
                query = "SELECT ue FROM UserEvent ue WHERE ue.user.id = :userId ORDER BY ue.time DESC"),
        @NamedQuery(name = "UserEvent.countAllByUser",
                query = "SELECT COUNT(*) FROM UserEvent ue WHERE ue.user.id = :userId"),

        @NamedQuery(name = "UserEvent.searchByUser",
                query = "SELECT ue FROM UserEvent ue WHERE ue.user.id = :userId AND ( " +
                        "ue.type = :query " +
                        "OR ue.ip LIKE :query " +
                        "OR ue.url LIKE :query " +
                        "OR ue.data LIKE :query " +
                        ") ORDER BY ue.time DESC"),
        @NamedQuery(name = "UserEvent.countSearchByUser",
                query = "SELECT COUNT(*) FROM UserEvent ue WHERE ue.user.id = :userId AND ( " +
                        "ue.type = :query " +
                        "OR ue.ip LIKE :query " +
                        "OR ue.url LIKE :query " +
                        "OR ue.data LIKE :query " +
                        ")"),

        @NamedQuery(name = "UserEvent.removeByUser",
                query = "DELETE FROM UserEvent ue WHERE ue.user.id = :userId")
})
public class UserEvent implements Serializable {

    /**
     * User id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    Long id;

    /**
     * User who produced the event.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    User user;

    /**
     * Target user of the event. May be null.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    User targetUser;

    /**
     * Event type.
     */
    @Column(nullable = false, updatable = false, length = 15)
    @Enumerated(EnumType.STRING)
    UserEventType type;

    /**
     * Time of event.
     */
    @Column(nullable = false, updatable = false)
    ZonedDateTime time;

    /**
     * IP address of the event.
     */
    @Column(nullable = false, updatable = false, length = 255)
    String ip;

    /**
     * Application URL.
     */
    @Column(nullable = true, updatable = false, length = 2000)
    String url;

    /**
     * Data, associated with event.
     */
    @Column(nullable = true, length = 65536)
    byte[] data;

    /**
     * Before persist.
     */
    @PrePersist
    public void prePersist() {
        if (time == null) {
            time = nowUtc();
        }
    }

    /**
     * Returns event id.
     *
     * @return Id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets event id.
     *
     * @param id Id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns event data.
     *
     * @return Event data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets event data bytes.
     *
     * @param data Event data bytes.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns event data as UTF-8 string.
     *
     * @return Event data as UTF-8 string.
     */
    public String getDataAsUtf8String() {
        return new String(getData(), StandardCharsets.UTF_8);
    }

    /**
     * Sets data as UTF-8 string.
     *
     * @param data Data as UTF-8 string.
     */
    public void setDataAsUtf8String(String data) {
        setData(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns user that produced the event.
     *
     * @return User.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user for the event.
     *
     * @param user User.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns target user. May me null if event is self-inflicted (like sign-in, sign-up, etc).
     *
     * @return Target user.
     */
    public User getTargetUser() {
        return targetUser;
    }

    /**
     * Sets target user.
     *
     * @param targetUser Target user.
     */
    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    /**
     * Returns event type.
     *
     * @return Event type.
     */
    public UserEventType getType() {
        return type;
    }

    /**
     * Sets event type.
     *
     * @param type Event type.
     */
    public void setType(UserEventType type) {
        this.type = type;
    }

    /**
     * Returns event time, UTC.
     *
     * @return Event time.
     */
    public ZonedDateTime getTime() {
        return time;
    }

    /**
     * Sets event time. UTC is expected.
     *
     * @param time Event time.
     */
    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    /**
     * Returns IP associated with the event.
     *
     * @return IP associated with the event.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets IP address of the event.
     *
     * @param ip IP address of the event.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Returns url.
     *
     * @return Url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets url.
     *
     * @param url Url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns current UTC date and time.
     *
     * @return Current UTC date and time.
     */
    private static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    private static final long serialVersionUID = 1L;
}
