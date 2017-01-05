package web.sso;

import com.google.inject.Injector;
import com.google.inject.Provider;
import controllers.sso.auth.ForgotPasswordController;
import controllers.sso.auth.RestorePasswordController;
import controllers.sso.auth.SignInController;
import controllers.sso.auth.SignUpController;
import controllers.sso.web.Escapers;
import ninja.NinjaFluentLeniumTest;
import ninja.Router;
import ninja.utils.NinjaProperties;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class with common data members and methods for all SSO tests.
 */
abstract class WebDriverTest extends NinjaFluentLeniumTest {

    /**
     * Entity manager provider.
     */
    protected Provider<EntityManager> entityManagerProvider;

    /**
     * Application router.
     */
    protected Router router;

    /**
     * Application properties.
     */
    protected NinjaProperties properties;

    /**
     * Logger.
     */
    protected Logger logger;

    @Before
    public void webDriverTestSetup() {
        Injector injector = this.getInjector();

        this.entityManagerProvider = injector.getProvider(EntityManager.class);
        this.router = injector.getBinding(Router.class).getProvider().get();
        this.properties = injector.getBinding(NinjaProperties.class).getProvider().get();
        this.logger = injector.getBinding(Logger.class).getProvider().get();

        this.webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    /**
     * Returns form element value by name.
     *
     * @param name Name of the element.
     * @return Form element value.
     */
    protected String getFormElementValue(String name) {
        return getFormElement(name).getAttribute("value");
    }

    /**
     * Returns form element by name.
     *
     * @param name Name of the element.
     * @return Web element on the page.
     */
    protected WebElement getFormElement(String name) {
        return webDriver.findElement(By.name(name));
    }

    /**
     * Constructs Sign Up URL with optional continue URL.
     *
     * @param continueUrl Continue URL. Optional.
     * @return Sign Up URL.
     */
    protected String getSignUpUrl(String... continueUrl) {
        StringBuilder sb = new StringBuilder(getServerAddress())
                .append(router.getReverseRoute(SignUpController.class, "signUpGet"))
                .append("?");
        if (continueUrl.length > 0 && continueUrl[0] != null) {
            sb.append("&continue=");
            sb.append(Escapers.encodePercent(continueUrl[0]));
        }
        return sb.toString();
    }

    /**
     * Constructs Sign In URL.
     *
     * @return Sign In URL.
     */
    protected String getSignInUrl() {
        return getSignInUrl(null, null);
    }

    /**
     * Constructs Sign In URL with optional continue URL and optional state.
     *
     * @param continueUrl Continue URL. Optional.
     * @param state State of the sign in. Optional.
     * @return Continue URL.
     */
    protected String getSignInUrl(String continueUrl, String state) {
        StringBuilder sb = new StringBuilder(getServerAddress())
                .append(router.getReverseRoute(SignInController.class, "signInGet"))
                .append("?");
        if (continueUrl != null) {
            sb.append("&continue=");
            sb.append(Escapers.encodePercent(continueUrl));
        }
        if (state != null) {
            sb.append("&state=");
            sb.append(Escapers.encodePercent(state));
        }
        return sb.toString();
    }


    /**
     * Constructs Forgot Password URL with optional continue URL.
     *
     * @param continueUrl Continue URL. Optional.
     * @return Forgot Password URL.
     */
    protected String getForgotPasswordUrl(String... continueUrl) {
        StringBuilder sb = new StringBuilder(getServerAddress())
                .append(router.getReverseRoute(ForgotPasswordController.class, "forgotGet"))
                .append("?");
        if (continueUrl.length > 0 && continueUrl[0] != null) {
            sb.append("&continue=");
            sb.append(Escapers.encodePercent(continueUrl[0]));
        }
        return sb.toString();
    }

    /**
     * Constructs Restore Password URL with restore password token.
     *
     * @param restorePasswordToken Restore password token.
     * @return Restore password URL.
     */
    protected String getRestorePasswordUrl(String restorePasswordToken) {
        return new StringBuilder(getServerAddress())
                .append(router.getReverseRoute(RestorePasswordController.class, "restorePasswordGet"))
                .append("?restoreToken=")
                .append(Escapers.encodePercent(restorePasswordToken))
                .toString();
    }

    /**
     * Returns parameters from given URI.
     *
     * @param uri URI to parse.
     * @return Map of parameters.
     */
    protected static Map<String, String> extractParameters(URI uri) {
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        String[] pairs = query.replaceAll("&amp;", "&").split("&");
        Map<String, String> params = new LinkedHashMap<>(pairs.length);
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1) {
                params.put(Escapers.decodePercent(keyValue[0]), Escapers.decodePercent(keyValue[1]));
            }
        }
        return params;
    }
}
