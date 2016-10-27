package conf.sso;

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
        router.GET().route(subRoute + "/captcha").with(CaptchaController.class, "captcha");

        // Sign up.
        router.GET().route(subRoute +"/signup").with(SignUpController.class, "signUpGet");
        router.POST().route(subRoute +"/signup").with(SignUpController.class, "signUp");

        // Sing up verification.
        router.GET().route(subRoute +"/signup/verify")
                .with(SignUpVerificationController.class, "verifySignUpGet");
        router.POST().route(subRoute +"/signup/verify")
                .with(SignUpVerificationController.class, "verifySignUp");
        router.GET().route(subRoute +"/signup/verify-email")
                .with(SignUpVerificationController.class, "verifyEmail");

        // Sign in.
        router.GET().route(subRoute +"/signin").with(SignInController.class, "signInGet");
        router.POST().route(subRoute +"/signin").with(SignInController.class, "signIn");

        // Sign out.
        router.POST().route(subRoute + "/signout").with(SignOutController.class, "signOut");

        // Forgot password.
        router.GET().route(subRoute +"/forgot")
                .with(ForgotPasswordController.class, "forgotGet");
        router.POST().route(subRoute +"/forgot")
                .with(ForgotPasswordController.class, "forgot");

        // Restore password.
        router.GET().route(subRoute +"/restore")
                .with(RestorePasswordController.class, "restorePasswordGet");
        router.POST().route(subRoute +"/restore")
                .with(RestorePasswordController.class, "restorePassword");

        // Admin routes.
        // Users.
        router.GET().route(subRoute + "/admin/users").with(UsersController.class, "users");
        // Edit personal data.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-personal")
                .with(EditPersonalDataController.class, "get");
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-personal")
                .with(EditPersonalDataController.class, "post");
        // Edit contact data.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-contact")
                .with(EditContactDataController.class, "get");
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-contact")
                .with(EditContactDataController.class, "post");
        // Edit user role.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-access")
                .with(EditAccessController.class, "get");
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-access")
                .with(EditAccessController.class, "post");
        // View access log.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/access-log")
                .with(ViewAccessLogController.class, "get");
        // Change password.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-password")
                .with(EditPasswordController.class, "get");
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/edit-password")
                .with(EditPasswordController.class, "post");
        // Send email.
        router.GET().route(subRoute + "/admin/user/{userId: [0-9]+}/send-email")
                .with(SendEmailController.class, "get");
        router.POST().route(subRoute + "/admin/user/{userId: [0-9]+}/send-email")
                .with(SendEmailController.class, "post");

    }
}
