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

    /**
     * Returns access token associated with the authentication session.
     *
     * @return Access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets access token.
     *
     * @param accessToken Access token.
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Returns refresh token associated with the authentication session.
     *
     * @return Refresh token.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets refresh token.
     *
     * @param refreshToken Refresh token.
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Returns the user from the authentication session.
     *
     * @return User.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets user for the session.
     *
     * @param user User.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns creation time of the session.
     *
     * @return Creation time.
     */
    public ZonedDateTime getCreated() {
        return created;
    }

    /**
     * Sets creation time.
     *
     * @param created Creation time.
     */
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
