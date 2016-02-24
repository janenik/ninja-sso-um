package models.sso;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Session object. Access token is the session id as well.
 */
@Entity
@Table(name = "authSessions", indexes = {
        @Index(name = "accessToken_idx", columnList = "accessToken", unique = true),
        @Index(name = "refreshToken_idx", columnList = "refreshToken", unique = false)
})
@NamedQueries({
        @NamedQuery(name = "AuthSession.deleteExpired",
                query = "DELETE FROM AuthSession ls WHERE ls.created < :created")
})
public class AuthSession implements Serializable {

    /**
     * Access token in auth session. Primary key, unique.
     */
    @Id
    @Column(nullable = false, updatable = false)
    @Size(min = 16, max = 255)
    String accessToken;

    /**
     * Refresh token of the session.
     */
    @Column(nullable = false, updatable = false)
    @Size(min = 16, max = 255)
    String refreshToken;

    /**
     * User associated with auth session.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    User user;

    /**
     * Time of creation, in UTC.
     */
    @Column(nullable = false, updatable = false)
    ZonedDateTime created;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    @PrePersist
    public void prePersist() {
        setCreated(nowUtc());
    }

    @PreUpdate
    public void preUpdate() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AuthSession)) {
            return false;
        }
        AuthSession that = (AuthSession) o;
        return accessToken.equals(that.accessToken);
    }

    @Override
    public int hashCode() {
        return accessToken.hashCode();
    }

    /**
     * Returns current UTC date/time.
     *
     * @return Current UTC date/time.
     */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    private static final long serialVersionUID = 1L;
}
