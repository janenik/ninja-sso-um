package conf.sso;

import com.google.inject.Inject;
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
    }
}
