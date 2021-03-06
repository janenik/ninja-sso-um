package web.sso.common;

import com.google.inject.Injector;
import com.google.inject.Provider;
import controllers.sso.auth.*;
import controllers.sso.web.Escapers;
import controllers.sso.web.UrlBuilder;
import ninja.NinjaFluentLeniumTest;
import ninja.ReverseRouter;
import ninja.Router;
import ninja.utils.NinjaProperties;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class with common data members and methods for all SSO tests.
 */
public abstract class WebDriverTest extends NinjaFluentLeniumTest {

    /**
     * Update hidden field javascript pattern: fetches form element by name and updates its value. Note: may have
     * problems with escaping.
     */
    private final String JAVASCRIPT_PATTERN = "document.getElementsByName(\"%s\")[0].value=\"%s\"";

    /**
     * Entity manager provider.
     */
    protected Provider<EntityManager> entityManagerProvider;

    /**
     * Application router.
     */
    protected Router router;

    /**
     * Reverse router;
     */
    protected ReverseRouter reverseRouter;

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
        this.reverseRouter = injector.getBinding(ReverseRouter.class).getProvider().get();
        this.properties = injector.getBinding(NinjaProperties.class).getProvider().get();
        this.logger = injector.getBinding(Logger.class).getProvider().get();

        this.webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    /**
     * Returns form element by name.
     *
     * @param name Name of the element.
     * @return Web element on the page.
     */
    protected WebElement getFormInput(String name) {
        return webDriver.findElement(By.name(name));
    }

    /**
     * Returns form element value by name.
     *
     * @param name Name of the element.
     * @return Form element value.
     */
    protected String getFormInputValue(String name) {
        return getFormInput(name).getAttribute("value");
    }

    /**
     * Sets value for form input by its name. Returns form input.
     *
     * @param name  Name of the element.
     * @param value Value.
     * @return Web element on the page.
     */
    protected WebElement setFormInputValue(String name, String value) {
        WebElement element = getFormInput(name);
        if (element == null) {
            throw new IllegalArgumentException("Form element with name " + name + " was not found.");
        }
        if ("hidden".equalsIgnoreCase(element.getAttribute("type"))) {
            if (webDriver instanceof HtmlUnitDriver) {
                String script =
                        String.format(JAVASCRIPT_PATTERN,
                                Escapers.encodePercent(name),
                                Escapers.encodePercent(value));
                HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) webDriver;
                htmlUnitDriver.setJavascriptEnabled(true);
                htmlUnitDriver.executeScript(script);
            } else {
                throw new UnsupportedOperationException(
                        "Setting value for hidden field must be reviewed. Expected class: "
                                + HtmlUnitDriver.class.getCanonicalName() + " but got: "
                                + webDriver.getClass().getCanonicalName());
            }
        } else {
            element.clear();
            element.sendKeys(value);
        }
        return element;
    }

    /**
     * Constructs Sign Up URL with optional continue URL.
     *
     * @param continueUrl Continue URL. Optional.
     * @return Sign Up URL.
     */
    protected String getSignUpUrl(String... continueUrl) {
        StringBuilder sb = new StringBuilder(getBaseUrl())
                .append(reverseRouter.with(SignUpController::signUpGet).build())
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
        return getSignInUrl(null);
    }

    /**
     * Constructs Sign In URL with optional continue URL and optional state.
     *
     * @param continueUrl Continue URL. Optional.
     * @param state       State of the sign in. Optional.
     * @return Continue URL.
     */
    protected String getSignInUrl(String continueUrl, String... state) {
        StringBuilder sb = new StringBuilder(getBaseUrl())
                .append(reverseRouter.with(SignInController::signInGet))
                .append("?");
        if (continueUrl != null) {
            sb.append("&continue=");
            sb.append(Escapers.encodePercent(continueUrl));
        }
        if (state != null && state.length > 0 && state[0] != null) {
            sb.append("&state=");
            sb.append(Escapers.encodePercent(state[0]));
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
        StringBuilder sb = new StringBuilder(getBaseUrl())
                .append(reverseRouter.with(ForgotPasswordController::forgotGet))
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
        return new StringBuilder(getBaseUrl())
                .append(reverseRouter.with(RestorePasswordController::restorePasswordGet))
                .append("?restoreToken=")
                .append(Escapers.encodePercent(restorePasswordToken))
                .toString();
    }

    /**
     * Constructs verification URL with sign up verification token.
     *
     * @param signUpVerificationToken Restore password token.
     * @return Restore password URL.
     */
    protected String getSignUpVerificationUrl(String signUpVerificationToken) {
        return new StringBuilder(getBaseUrl())
                .append(reverseRouter.with(SignUpVerificationController::verifySignUpGet))
                .append("?token=")
                .append(Escapers.encodePercent(signUpVerificationToken))
                .toString();
    }

    /**
     * Clicks given element.
     *
     * @param elementCssSelector Element CSS selector.
     */
    protected void click(String elementCssSelector) {
        this.webDriver.findElement(By.cssSelector(elementCssSelector)).click();
    }

    /**
     * Send ENTER keypress for given element.
     *
     * @param elementCssSelector Element CSS selector.
     */
    protected void pressEnter(String elementCssSelector) {
        this.webDriver.findElement(By.cssSelector(elementCssSelector)).sendKeys(Keys.ENTER);
    }

    /**
     * Submits the first form on the page with javascript.
     *
     * @param index Optional form index.
     */
    protected void submitFormWithJavascript(int... index) {
        if (webDriver instanceof HtmlUnitDriver) {
            int optionalIndex = index != null && index.length > 0 ? index[0] : 0;
            HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) webDriver;
            htmlUnitDriver.setJavascriptEnabled(true);
            htmlUnitDriver.executeScript("document.forms[" + optionalIndex + "].submit();");
        } else {
            throw new UnsupportedOperationException(
                    "Unable to execute Javascript to submit the form. Expected: "
                            + HtmlUnitDriver.class.getCanonicalName() + " but got: "
                            + webDriver.getClass().getCanonicalName());
        }
    }

    /**
     * Extracts parameters from given URL.
     *
     * @param url URL to extract parameters from.
     * @return Map of parameters.
     * @throws URISyntaxException In case of bad URL.
     */
    protected static Map<String, String> extractParameters(String url) throws URISyntaxException {
        return extractParameters(new URI(url));
    }

    /**
     * Returns parameters from given URI.
     *
     * @param uri URI to parse.
     * @return Map of parameters.
     */
    protected static Map<String, String> extractParameters(URI uri) {
        return Collections.unmodifiableMap(UrlBuilder.extractParametersAsMutableMap(uri.getRawQuery()));
    }
}
