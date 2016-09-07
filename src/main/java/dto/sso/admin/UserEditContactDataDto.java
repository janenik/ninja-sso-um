package dto.sso.admin;

import dto.sso.common.Constants;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User edit contact data DTO.
 */
public class UserEditContactDataDto implements Serializable {

    /**
     * User id.
     */
    long id;

    /**
     * Email.
     */
    @Pattern(regexp = Constants.EMAIL_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.EMAIL_MIN_LENGTH, max = Constants.EMAIL_MAX_LENGTH)
    String email;

    /**
     * Phone.
     */
    @Pattern(regexp = Constants.PHONE_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = 5, max = Constants.PHONE_MAX_LENGTH)
    String phone;

    /**
     * Country id (ISO code).
     */
    @NotBlank
    @NotNull
    @Size(min = 2, max = Constants.COUNTRY_ISO_MAX_LENGTH)
    String countryId;

    /**
     * Returns id.
     *
     * @return Id.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id Id.
     */
    public void setId(long id) {
        this.id = id;
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
     * Sets email.
     *
     * @param email Email.
     */
    public void setEmail(String email) {
        this.email = email;
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
     * Returns country's id.
     *
     * @return Country id.
     */
    public String getCountryId() {
        return countryId;
    }

    /**
     * Sets country's id.
     *
     * @param countryId Country id.
     */
    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }
}
