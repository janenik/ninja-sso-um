package web.sso;

import com.google.inject.Injector;
import com.google.inject.Provider;
import controllers.sso.auth.ForgotPasswordController;
import controllers.sso.web.Escapers;
import ninja.NinjaFluentLeniumTest;
import ninja.Router;
import ninja.utils.NinjaProperties;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;

import javax.persistence.EntityManager;
import java.util.concurrent.TimeUnit;

/**
 * Forgot password test.
 */
public class ForgotPasswordTest extends NinjaFluentLeniumTest {

    /**
     * Application router.
     */
    Router router;

    /**
     * User service.
     */
    UserService userService;

    /**
     * Entity manager.
     */
    Provider<EntityManager> entityManagerProvider;

    /**
     * Encryptor.
     */
    ExpirableTokenEncryptor encryptor;

    /**
     * Captcha token service.
     */
    CaptchaTokenService captchaTokenService;

    /**
     * Application properties.
     */
    NinjaProperties properties;

    /**
     * Logger.
     */
    Logger logger;


    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.router = injector.getBinding(Router.class).getProvider().get();
        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.entityManagerProvider = injector.getProvider(EntityManager.class);
        this.encryptor = injector.getBinding(ExpirableTokenEncryptor.class).getProvider().get();
        this.captchaTokenService = injector.getBinding(CaptchaTokenService.class).getProvider().get();
        this.properties = injector.getBinding(NinjaProperties.class).getProvider().get();
        this.logger = injector.getBinding(Logger.class).getProvider().get();

        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    @Test
    public void testForgotPassword() throws Exception {
        goTo(getForgotPasswordUrl(getServerAddress() + "?forgot_password=true"));
    }

    /**
     * Constructs sign up URL with optional continue URL.
     *
     * @param continueUrl Continue URL. Optional.
     * @return Continue URL.
     */
    private String getForgotPasswordUrl(String... continueUrl) {
        StringBuilder sb = new StringBuilder(getServerAddress())
                .append(router.getReverseRoute(ForgotPasswordController.class, "signUpGet"))
                .append("?");
        if (continueUrl.length > 0 && continueUrl[0] != null) {
            sb.append("&continue=");
            sb.append(Escapers.encodePercent(continueUrl[0]));
        }
        return sb.toString();
    }
}
