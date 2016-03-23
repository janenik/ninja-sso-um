package dto.sso;

import java.io.Serializable;

/**
 * User data transfer object.
 */
public class UserDto implements Serializable {

    /**
     * User id.
     */
    Long id;

    /**
     * Username.
     */
    String username;

    /**
     * Email.
     */
    String email;

    /**
     * First name.
     */
    String firstName;

    /**
     * Middle name.
     */
    String middleName;

    /**
     * Last name.
     */
    String lastName;

    /**
     * Phone.
     */
    String phone;

    /**
     * Country code.
     */
    String country;

    /**
     * Role as string.
     */
    String role;

    /**
     * Date of birth.
     */
    long dateOfBirth;

    /**
     * Created time.
     */
    long created;

    /**
     * Last update time.
     */
    long updated;

    /**
     * Version of the user.
     */
    int version;

    /**
     * Returns id.
     *
     * @return Id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets user id.
     *
     * @param id Id.
     */
    public void setId(Long id) {
        this.id = id;
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
     * Sets Username.
     *
     * @param username Username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns email.
     *
     * @return Email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets Email.
     *
     * @param email Email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns first name.
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
     * Returns middle name.
     *
     * @return MiddleName.
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets MiddleName.
     *
     * @param middleName MiddleName.
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Returns last name.
     *
     * @return Last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets last name.
     *
     * @param lastName LastName.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns phone.
     *
     * @return Phone.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets phone.
     *
     * @param phone Phone.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns country.
     *
     * @return Country.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets country code.
     *
     * @param country Country code.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns role.
     *
     * @return Role.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets role.
     *
     * @param role Role.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns date of birth.
     *
     * @return Date of birth.
     */
    public long getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets date of birth.
     *
     * @param dateOfBirth DateOfBirth.
     */
    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Returns creation time.
     *
     * @return Creation time.
     */
    public long getCreated() {
        return created;
    }

    /**
     * Sets creation time.
     *
     * @param created Creation time.
     */
    public void setCreated(long created) {
        this.created = created;
    }

    /**
     * Returns updated.
     *
     * @return Updated.
     */
    public long getUpdated() {
        return updated;
    }

    /**
     * Sets last update time.
     *
     * @param updated Last update time.
     */
    public void setUpdated(long updated) {
        this.updated = updated;
    }

    /**
     * Returns version.
     *
     * @return Version.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets version.
     *
     * @param version Version.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    private static final long serialVersionUID = 1L;
}
