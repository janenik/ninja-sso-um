package conf.sso;

import controllers.sso.admin.UsersList;
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
        router.GET().route(subRoute + "/signout").with(SignOutController.class, "signOut");

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
        router.GET().route(subRoute + "/admin/users").with(UsersList.class, "users");
    }
}
