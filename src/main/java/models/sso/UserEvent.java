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
import java.nio.charset.Charset;
import java.time.ZonedDateTime;

/**
 * Auth event. Allows to log times of registration, access, updates, authentication, password changes, etc.
 */
@Entity
@Table(name = "userEvents", indexes = {
        @Index(name = "userTypeTime_idx", columnList = "user_id,type,time")
})
@NamedQueries({
        @NamedQuery(name = "UserEvents.getByUser",
                query = "SELECT ue FROM UserEvent ue WHERE ue.user.id = :userId ORDER BY ue.time DESC"),
        @NamedQuery(name = "UserEvents.getByUserAndEventType",
                query = "SELECT ue FROM UserEvent ue WHERE ue.user.id = :userId AND ue.type = :eventType "
                        + "ORDER BY ue.time DESC")
})
public class UserEvent implements Serializable {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * User id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    Long id;

    /**
     * User event.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    User user;

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
            time = User.nowUtc();
        }
    }

    public byte[] getData() {
        return data;
    }

    public String getDataAsUtfString() {
        return new String(getData(), UTF8);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDataAsUtf8String(String data) {
        setData(data.getBytes(UTF8));
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserEventType getType() {
        return type;
    }

    public void setType(UserEventType type) {
        this.type = type;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    private static final long serialVersionUID = 1L;
}
