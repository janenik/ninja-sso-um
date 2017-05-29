package conf.sso;

import controllers.sso.UserController;
import controllers.sso.admin.users.EditAccessController;
import controllers.sso.admin.users.EditContactDataController;
import controllers.sso.admin.users.EditPasswordController;
import controllers.sso.admin.users.EditPersonalDataController;
import controllers.sso.admin.users.SendEmailController;
import controllers.sso.admin.users.UsersController;
import controllers.sso.admin.users.ViewAccessLogController;
import controllers.sso.auth.ForgotPasswordController;
import controllers.sso.auth.RestorePasswordController;
import controllers.sso.auth.SignInController;
import controllers.sso.auth.SignOutController;
import controllers.sso.auth.SignUpController;
import controllers.sso.auth.SignUpVerificationController;
import controllers.sso.captcha.CaptchaController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;

/**
 * SSO routes.
 */
public class SsoRoutes implements ApplicationRoutes {

    /**
     * Application properties.
     */
    @Inject
    NinjaProperties properties;

    @Override
    public void init(Router router) {
        String subRoute = properties.getOrDie("application.sso.subRoute");

        // User JSON controller.
        router.GET().route(subRoute + "/user").with(UserController::user);

        // Captcha.
        router.GET().route(subRoute + "/captcha").with(CaptchaController::captcha);

        // Sign up.
        router.GET().route(subRoute +"/signup").with(SignUpController::signUpGet);
        router.POST().route(subRoute +"/signup").with(SignUpController::signUp);

        // Sing up verification.
        router.GET().route(subRoute +"/signup/verify")
                .with(SignUpVerificationController::verifySignUpGet);
        router.POST().route(subRoute +"/signup/verify")
                .with(SignUpVerificationController::verifySignUp);
        router.GET().route(subRoute +"/signup/verify-email")
                .with(SignUpVerificationController::verifyEmail);

        // Sign in.
        router.GET().route(subRoute +"/signin").with(SignInController::signInGet);
        router.POST().route(subRoute +"/signin").with(SignInController::signIn);

        // Sign out.
        router.POST().route(subRoute + "/signout").with(SignOutController::signOut);

        // Forgot password.
        router.GET().route(subRoute +"/forgot")
                .with(ForgotPasswordController::forgotGet);
        router.POST().route(subRoute +"/forgot")
                .with(ForgotPasswordController::forgot);

        // Restore password.
        router.GET().route(subRoute +"/restore")
                .with(RestorePasswordController::restorePasswordGet);
        router.POST().route(subRoute +"/restore")
                .with(RestorePasswordController::restorePassword);

        // Admin routes.
        // Users.
        router.GET().route(subRoute + "/admin/users").with(UsersController::users);
        // Edit personal data.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-personal")
                .with(EditPersonalDataController::get);
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-personal")
                .with(EditPersonalDataController::post);
        // Edit contact data.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-contact")
                .with(EditContactDataController::get);
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-contact")
                .with(EditContactDataController::post);
        // Edit user role.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-access")
                .with(EditAccessController::get);
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-access")
                .with(EditAccessController::post);
        // View access log.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/access-log")
                .with(ViewAccessLogController::get);
        // Change password.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-password")
                .with(EditPasswordController::get);
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-password")
                .with(EditPasswordController::post);
        // Send email.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/send-email")
                .with(SendEmailController::get);
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/send-email")
                .with(SendEmailController::post);

    }
}
