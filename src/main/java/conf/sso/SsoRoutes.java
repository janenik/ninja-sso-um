package conf.sso;

import com.google.inject.Inject;
import controllers.sso.auth.SignUpController;
import controllers.sso.auth.SignUpVerificationController;
import controllers.sso.captcha.CaptchaController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import ninja.utils.NinjaProperties;

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
    }
}
