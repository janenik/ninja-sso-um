package models.sso;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * User object entity. Allows to save user defined objects (blobs) up to 64K per object.
 * user + scope (string) is a primary key.
 * userId is an index as well (foreign key constraint).
 */
@Entity
@Table(name = "userObjects", indexes = {
})
@NamedQueries({
        @NamedQuery(name = "UserObject.getAllByUserId",
                query = "SELECT us FROM UserObject us WHERE us.pk.user.id = :userId")
})
public class UserObject implements Serializable {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * User object primary key: user + scope.
     */
    @EmbeddedId
    PK pk;

    /**
     * Data associated with user and scope.
     */
    @Column(length = 64 * 1024)
    byte[] data;

    /**
     * Returns primary key.
     *
     * @return Primary key.
     */
    public PK getPk() {
        return pk;
    }

    /**
     * Setter for primary key.
     *
     * @param pk Primary key.
     */
    public void setPk(PK pk) {
        this.pk = pk;
    }

    /**
     * Returns data
     *
     * @return Data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data Data bytes.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns data as UTF-8 string.
     *
     * @return Data as UTF-8 string.
     */
    public String getDataAsUtf8String() {
        return new String(getData(), UTF8);
    }

    /**
     * Sets data as UTF-8 string.
     *
     * @param data Data as UTF-8 string.
     */
    public void setDataAsUtf8String(String data) {
        setData(data.getBytes(UTF8));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.pk);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UserObject)) {
            return false;
        }
        final UserObject other = (UserObject) obj;
        return Objects.equals(this.pk, other.pk);
    }

    /**
     * Primary key class that defines the user object: user + scope.
     */
    @Embeddable
    public static class PK implements Serializable {

        /**
         * User.
         */
        @ManyToOne(fetch = FetchType.LAZY)
        User user;

        /**
         * Scope.
         */
        @Column(length = 255, nullable = false, unique = false, updatable = false)
        String scope;

        /**
         * Public constructor.
         */
        public PK() {
        }

        /**
         * Constructs primary key.
         *
         * @param user User.
         * @param scope Scope.
         */
        public PK(User user, String scope) {
            this.user = user;
            this.scope = scope.toLowerCase().trim();
        }

        /**
         * Owner of the object.
         *
         * @return Owner.
         */
        public User getUser() {
            return user;
        }

        /**
         * Sets the owner of the object.
         *
         * @param user Owner.
         */
        public void setUser(User user) {
            this.user = user;
        }

        /**
         * Returns scope for the user object.
         *
         * @return Scope.
         */
        public String getScope() {
            return scope;
        }

        /**
         * Sets scope.
         *
         * @param scope Scope.
         */
        public void setScope(String scope) {
            this.scope = scope.toLowerCase().trim();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.user);
            hash = 29 * hash + Objects.hashCode(this.scope);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof PK)) {
                return false;
            }
            final PK other = (PK) obj;
            if (this.user.equals(other.user)) {
                return false;
            }
            return Objects.equals(this.scope, other.scope);
        }

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
