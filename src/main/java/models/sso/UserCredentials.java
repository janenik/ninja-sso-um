package models.sso;

import javax.persistence.*;

/**
 * User credentials.
 */
@Entity
@Table(name = "userCredentials")
public class UserCredentials {

    /**
     * User id.
     */
    @Id
    long userId;

    /**
     * Password salt.
     */
    @Column(nullable = false, length = 512)
    byte[] passwordSalt;

    /**
     * Password hash.
     */
    @Column(nullable = false, length = 512)
    byte[] passwordHash;

    /**
     * User id.
     *
     * @return User id.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Sets user id.
     *
     * @param userId user id.
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Password hash code.
     *
     * @return Password hash code.
     */
    public byte[] getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets password hash code.
     *
     * @param passwordHash Password hash code.
     */
    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Password salt.
     *
     * @return Password salt.
     */
    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * Sets password salt.
     *
     * @param passwordSalt Password salt.
     */
    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return userId == ((UserCredentials) o).userId;
    }

    @Override
    public int hashCode() {
        return (int) (userId ^ (userId >>> 32));
    }
}
