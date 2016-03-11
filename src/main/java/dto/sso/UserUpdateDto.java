package dto.sso;

import com.google.common.base.Strings;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * User update DTO.
 */
public class UserUpdateDto implements Serializable {

    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.FIRST_NAME_MAX_LENGTH)
    String firstName;
    @NotBlank
    @NotNull
    @Size(min = 1, max = Constants.LAST_NAME_MAX_LENGTH)
    String lastName;
    @Size(max = Constants.MIDDLE_NAME_MAX_LENGTH)
    String middleName;
    @Pattern(regexp = Constants.PHONE_PATTERN)
    @NotBlank
    @NotNull
    @Size(max = Constants.MIDDLE_NAME_MAX_LENGTH)
    String phone;

    public String getFirstName() {
        return Strings.nullToEmpty(firstName).trim();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return Strings.nullToEmpty(lastName).trim();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = Strings.nullToEmpty(phone).trim();
    }

    private static final long serialVersionUID = 1L;
}
