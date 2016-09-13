package dto.sso.admin.users;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Edit user role DTO.
 */
public final class EditUserRoleDto implements Serializable {

    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    String role;

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
}
