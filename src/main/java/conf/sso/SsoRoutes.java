package conf.sso;

import controllers.sso.captcha.CaptchaController;
import ninja.Router;
import ninja.application.ApplicationRoutes;

/**
 * SSO routes.
 */
public class SsoRoutes implements ApplicationRoutes {

    @Override
    public void init(Router router) {
        router.GET().route("/auth/captcha").with(CaptchaController.class, "captcha");
    }
}
