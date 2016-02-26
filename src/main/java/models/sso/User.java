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
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * User entity.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "username_idx", columnList = "username", unique = true),
        @Index(name = "email_idx", columnList = "email", unique = true),
        @Index(name = "phone_idx", columnList = "phone", unique = true)
})
@NamedQueries({
        @NamedQuery(name = "User.getByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
        @NamedQuery(name = "User.getByUsername", query = "SELECT u FROM User u WHERE u.username = :username"),
        @NamedQuery(name = "User.getByPhone", query = "SELECT u FROM User u WHERE u.phone = :phone")
})
public class User implements Serializable {

    /**
     * User id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    Long id;

    /**
     * Username.
     */
    @Column(nullable = true)
    @Size(min = 3, max = 255)
    String username;

    /**
     * User email.
     */
    @Column(nullable = false)
    @Size(min = 3, max = 255)
    String email;

    /**
     * Primary phone number.
     */
    @Column(nullable = true)
    @Size(max = 50)
    String phone;

    /**
     * First name.
     */
    @Column(nullable = false)
    @Size(min = 2, max = 100)
    String firstName;

    /**
     * Middle name. Optional.
     */
    @Column(nullable = true)
    @Size(min = 0, max = 100)
    String middleName;

    /**
     * Last name.
     */
    @Column(nullable = false)
    @Size(min = 2, max = 100)
    String lastName;

    /**
     * Country.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    Country country;

    /**
     * Date of birth.
     */
    @Column(nullable = false, updatable = false)
    LocalDate dateOfBirth;

    /**
     * Confirmation state.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    UserConfirmationState confirmationState;

    /**
     * Role of the user.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    UserRole role;

    /**
     * Password salt.
     */
    @Column(nullable = false, length = 64)
    byte[] passwordSalt;

    /**
     * Password hash.
     */
    @Column(nullable = false, length = 64)
    byte[] passwordHash;

    /**
     * Time of sign up, UTC.
     */
    @Column(nullable = false, updatable = false)
    ZonedDateTime created;

    /**
     * Time of the latest update, UTC.
     */
    @Column(nullable = false)
    ZonedDateTime updated;

    /**
     * Version.
     */
    @Version
    int version;

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Constructs user object by given parameters.
     *
     * @param id User id.
     * @param username Username.
     * @param email User email.
     * @param phone Phone.
     */
    public User(Long id, String username, String email, String phone) {
        username = username.trim().toLowerCase();
        email = email.trim().toLowerCase();
        if (username.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Username and email must not be empty");
        }
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Constructs user object by given parameters.
     *
     * @param username Username
     * @param email User email.
     * @param phone Phone.
     */
    public User(String username, String email, String phone) {
        this(null, username, email, phone);
    }

    /**
     * Before persist.
     */
    @PrePersist
    public void prePersist() {
        ZonedDateTime now = nowUtc();
        if (created == null) {
            created = now;
        }
        if (updated == null) {
            updated = now;
        }
        if (role == null) {
            role = UserRole.USER;
        }
        if (confirmationState == null) {
            confirmationState = UserConfirmationState.UNCONFIRMED;
        }
    }

    /**
     * Before update.
     */
    @PreUpdate
    public void preUpdate() {
        updated = nowUtc();
    }

    /**
     * Returns user id.
     *
     * @return User id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets user id.
     *
     * @param id User id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns email.
     *
     * @return User email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns user phone number.
     *
     * @return Phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets user phone number.
     *
     * @param phone Phone number.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets user email.
     *
     * @param email Email.
     */
    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    /**
     * User first name.
     *
     * @return First name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets first name.
     *
     * @param firstName First name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * User last name.
     *
     * @return Last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets user last name.
     *
     * @param lastName Last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * User middle name.
     *
     * @return Middle name.
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets middle name. Null is possible.
     *
     * @param middleName Middle name.
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Returns username.
     *
     * @return Username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username. Username will be trimmed and lowercased.
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username.toLowerCase().trim();
    }

    /**
     * Returns country of the user.
     *
     * @return Country.
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets country for the user.
     *
     * @param country Country.
     */
    public void setCountry(Country country) {
        this.country = country;
    }

    /**
     * Returns user date of birth.
     *
     * @return Date of birth.
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets user date of birth.
     *
     * @param dateOfBirth Date of birth.
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Returns user role.
     *
     * @return User role.
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets user role.
     *
     * @param role User role.
     */
    public void setRole(UserRole role) {
        this.role = role;
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


    /**
     * Returns creation time since 1970 in seconds.
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

    /**
     * Returns update time since 1970 in seconds.
     *
     * @return Update time.
     */
    public ZonedDateTime getUpdated() {
        return updated;
    }

    /**
     * Sets updated time.
     *
     * @param updated Updated time.
     */
    public void setUpdated(ZonedDateTime updated) {
        this.updated = updated;
    }

    /**
     * Returns user confirmation status.
     *
     * @return User confirmation status.
     */
    public UserConfirmationState getConfirmationState() {
        return confirmationState;
    }

    /**
     * Sets user confirmation status.
     *
     * @param confirmationState User confirmation status.
     */
    public void setConfirmationState(UserConfirmationState confirmationState) {
        this.confirmationState = confirmationState;
    }

    /**
     * Confirms the user: sets the status to {@link UserConfirmationState#CONFIRMED}.
     */
    public void confirm() {
        confirmationState = UserConfirmationState.CONFIRMED;
    }

    /**
     * Sets the status to {@link UserConfirmationState#UNCONFIRMED}.
     */
    public void unconfirm() {
        confirmationState = UserConfirmationState.UNCONFIRMED;
    }

    /**
     * Checks if the user has confirmed his/her email or phone.
     *
     * @return Whether the status is confirmed.
     */
    public boolean isConfirmed() {
        return UserConfirmationState.CONFIRMED.equals(confirmationState);
    }

    /**
     * Version of the object.
     *
     * @return Version.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets object version.
     *
     * @param version Version.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return 67 * 7 + Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof User)) {
            return false;
        }
        return Objects.equals(this.id, ((User) obj).id);
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
