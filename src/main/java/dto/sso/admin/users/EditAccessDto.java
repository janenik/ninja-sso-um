package dto.sso.admin.users;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Edit user role DTO.
 */
public final class EditAccessDto implements Serializable {

    /**
     * User role as string.
     */
    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String role;

    /**
     * User sign-in state as string.
     */
    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String signInState;

    /**
     * User confirmation state as string.
     */
    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String confirmationState;

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
     * Returns sign-in state for user.
     *
     * @return Sign-in state.
     */
    public String getSignInState() {
        return signInState;
    }

    /**
     * Sets sign-in state.
     *
     * @param signInState Sign-in state.
     */
    public void setSignInState(String signInState) {
        this.signInState = signInState;
    }

    /**
     * Returns confirmation state.
     *
     * @return Confirmation state.
     */
    public String getConfirmationState() {
        return confirmationState;
    }

    /**
     * Sets confirmation state.
     *
     * @param confirmationState Confirmation state.
     */
    public void setConfirmationState(String confirmationState) {
        this.confirmationState = confirmationState;
    }
}
