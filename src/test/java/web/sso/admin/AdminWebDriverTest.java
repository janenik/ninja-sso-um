package web.sso.admin;

import controllers.sso.admin.users.*;
import web.sso.common.WebDriverTest;

/**
 * Administrator web driver test.
 */
public abstract class AdminWebDriverTest extends WebDriverTest {

    /**
     * Returns administrator users page URL.
     *
     * @return Admin users' page URL.
     */
    protected String getAdminUsersPageUrl() {
        return new StringBuilder(getServerAddress())
                .append(reverseRouter.with(UsersController::users))
                .toString();
    }

    /**
     * Returns edit personal data administrator page URL.
     *
     * @return Edit personal data administrator page URL.
     */
    protected String getAdminPersonalDataPageUrl(long userId) {
        return new StringBuilder(getServerAddress())
                .append(reverseRouter.with(EditPersonalDataController::get)
                        .pathParam("userId", userId))
                .toString();
    }

    /**
     * Returns edit contact data administrator page URL.
     *
     * @return Edit contact data administrator page URL.
     */
    protected String getAdminContactDataPageUrl(long userId) {
        return new StringBuilder(getServerAddress())
                .append(reverseRouter.with(EditContactDataController::get)
                        .pathParam("userId", userId))
                .toString();
    }

    /**
     * Returns edit access administrator page URL.
     *
     * @return Edit access administrator page URL.
     */
    protected String getAdminAccessPageUrl(long userId) {
        return new StringBuilder(getServerAddress())
                .append(reverseRouter.with(EditAccessController::get)
                        .pathParam("userId", userId))
                .toString();
    }

    /**
     * Returns reset password access administrator page URL.
     *
     * @return Edit access administrator page URL.
     */
    protected String getAdminResetPasswordPageUrl(long userId) {
        return new StringBuilder(getServerAddress())
                .append(reverseRouter.with(EditPasswordController::get)
                        .pathParam("userId", userId))
                .toString();
    }
}
