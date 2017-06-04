package dto.sso.admin.users;

import dto.sso.common.Constants;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User edit contact data DTO.
 */
public final class EditContactDataDto implements Serializable {

    /**
     * Email.
     */
    @Pattern(regexp = Constants.EMAIL_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.EMAIL_MIN_LENGTH, max = Constants.EMAIL_MAX_LENGTH)
    private String email;

    /**
     * Phone.
     */
    @Pattern(regexp = Constants.PHONE_PATTERN)
    @NotBlank
    @NotNull
    @Size(min = Constants.PHONE_MIN_LENGTH, max = Constants.PHONE_MAX_LENGTH)
    private String phone;

    /**
     * Country id (ISO code).
     */
    @NotBlank
    @NotNull
    @Size(min = Constants.COUNTRY_ISO_MIN_LENGTH, max = Constants.COUNTRY_ISO_MAX_LENGTH)
    private String countryId;

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
