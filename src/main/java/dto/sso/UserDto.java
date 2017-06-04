package dto.sso;

import java.io.Serializable;

/**
 * User data transfer object. Public DTO.
 */
public class UserDto implements Serializable {

    /**
     * User id.
     */
    private Long id;

    /**
     * Username.
     */
    private String username;

    /**
     * First name.
     */
    private String firstName;

    /**
     * Middle name.
     */
    private String middleName;

    /**
     * Last name.
     */
    private String lastName;

    /**
     * Country code.
     */
    private String country;

    /**
     * Role as string.
     */
    private String role;

    /**
     * Age.
     */
    private long age;

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
     * Returns age.
     *
     * @return Age.
     */
    public long getAge() {
        return age;
    }

    /**
     * Sets age.
     *
     * @param age Age.
     */
    public void setAge(long age) {
        this.age = age;
    }

    private static final long serialVersionUID = 1L;
}
