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
import javax.persistence.Version;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * User object entity. Allows to save user defined objects (blobs) up to 64K per object.
 * userId (long) + scope (string) is a primary key.
 * userId is index as well (foreign key constraint).
 * Versioned..
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

    @EmbeddedId
    PK pk;

    @Column(length = 32 * 1024 * 1024)
    byte[] data;

    @Version
    int version;

    public PK getPk() {
        return pk;
    }

    public void setPk(PK pk) {
        this.pk = pk;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    @Embeddable
    public static class PK implements Serializable {

        @ManyToOne(fetch = FetchType.LAZY)
        User user;

        @Column(length = 255, nullable = false, unique = false, updatable = false)
        String scope;

        public PK() {
        }

        public PK(User user, String scope) {
            this.user = user;
            this.scope = scope;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
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
