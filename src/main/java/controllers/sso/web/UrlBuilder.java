package controllers.sso.web;

import com.google.common.base.Strings;
import com.google.inject.servlet.RequestScoped;
import controllers.ApplicationController;
import controllers.annotations.InjectedContext;
import controllers.sso.auth.RestorePasswordController;
import controllers.sso.auth.SignInController;
import controllers.sso.auth.SignUpVerificationController;
import controllers.sso.captcha.CaptchaController;
import controllers.sso.filters.LanguageFilter;
import ninja.Context;
import ninja.Router;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * URL builder for application. Constructs URLs for captcha, emails, continue URLs, etc.
 */
@RequestScoped
public class UrlBuilder {

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Router.
     */
    final Router router;

    /**
     * Current request context.
     */
    final Context context;

    /**
     * Allowed redirect URL prefixes.
     */
    final List<String> allowedContinueUrls;

    /**
     * Base application URL.
     */
    final String baseUrl;

    /**
     * Base application URL with context.
     */
    final String baseUrlWithContext;

    /**
     * Constructs URL builder.
     *
     * @param properties Application properties.
     * @param router Router.
     * @param context Context.
     * @param allowedContinueUrls List of allowed continue URLs.
     */
    @Inject
    public UrlBuilder(
            NinjaProperties properties,
            Router router,
            @InjectedContext Context context,
            @Named("allowedContinueUrls") List<String> allowedContinueUrls) {
        this.properties = properties;
        this.router = router;
        this.context = context;
        this.allowedContinueUrls = allowedContinueUrls;
        this.baseUrl = properties.getOrDie("application.baseUrl");
        this.baseUrlWithContext = this.baseUrl + context.getContextPath();
    }

    /**
     * Constructs URL to captcha image.
     *
     * @param captchaToken Captcha token.
     * @return URL to captcha image with given token.
     */
    public String getCaptchaUrl(String captchaToken) {
        StringBuilder sb = new StringBuilder();
        String contextPath = context.getContextPath();
        String reversedRoute = router.getReverseRoute(CaptchaController.class, "captcha");
        if (!reversedRoute.startsWith(contextPath)) {
            sb.append(contextPath);
        }
        sb.append(reversedRoute);
        sb.append("?");
        sb.append(CaptchaController.CAPTCHA_PARAMETER);
        sb.append("=");
        sb.append(Escapers.encodePercent(captchaToken));
        return sb.toString();
    }

    /**
     * Extracts the continue URL to the final destination from given context. Also, checks
     * provided URL to match only allowed URL prefixes which saves application from open
     * redirect problem.
     * URL is absolute.
     *
     * @return Absolute continue URL to the final project or base URL of application.
     */
    public String getContinueUrlParameter() {
        String url = Strings.nullToEmpty(context.getParameter("continue")).trim();
        if (url.isEmpty()) {
            return baseUrlWithContext;
        }
        // Allow redirect to anywhere in test mode.
        if (properties.isTest()) {
            return url;
        }
        for (String urlPrefix : allowedContinueUrls) {
            if (url.startsWith(urlPrefix)) {
                return url;
            }
        }
        // Nothing matched. Returning default which is a base URL.
        return baseUrlWithContext;
    }

    /**
     * Constructs confirmation URL by given parameters.
     * URL is absolute.
     *
     * @param emailConfirmationToken Confirmation code.
     * @return Confirmation URL.
     */
    public String getEmailConfirmationUrl(String emailConfirmationToken) {
        String reverseRoute = router.getReverseRoute(SignUpVerificationController.class, "verifyEmail");
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(context, reverseRoute);
        return urlBuilder
                .append("&token=")
                .append(Escapers.encodePercent(emailConfirmationToken))
                .append("&continue=")
                .append(Escapers.encodePercent(getContinueUrlParameter()))
                .toString();
    }

    /**
     * Returns relative URL to Sign Up verification page after successful user sign up and confirmation email sent.
     * URL is relative.
     *
     * @param signUpVerificationToken Sign up verification token.
     * @return URL to sign up verification page.
     */
    public String getSignUpVerificationPage(String signUpVerificationToken) {
        String reverseRoute = router.getReverseRoute(SignUpVerificationController.class, "verifySignUp");
        StringBuilder urlBuilder = newRelativeUrlBuilder(context, reverseRoute);
        return urlBuilder
                .append("&token=")
                .append(Escapers.encodePercent(signUpVerificationToken))
                .append("&continue=")
                .append(Escapers.encodePercent(getContinueUrlParameter()))
                .toString();
    }

    /**
     * Returns URL to Sign In page that contains a state message.
     * URL is relative.
     *
     * @param state Optional state.
     * @return Relative Sign In URL.
     */
    public String getSignInUrl(Object... state) {
        String reverseRoute = router.getReverseRoute(SignInController.class, "signInGet");
        StringBuilder urlBuilder = newRelativeUrlBuilder(context, reverseRoute);
        if (state != null && state.length > 0 && state[0] != null) {
            urlBuilder
                    .append("&state=")
                    .append(Escapers.encodePercent(state[0].toString().toLowerCase()));
        }
        return urlBuilder
                .append("&continue=")
                .append(Escapers.encodePercent(getContinueUrlParameter()))
                .toString();
    }

    /**
     * Returns URL to Sign In page with current URL as continue URL.
     * URL is relative.
     *
     * @return Relative Sign In URL.
     */
    public String getSignInUrlForCurrentUrl() {
        String reverseRoute = router.getReverseRoute(SignInController.class, "signInGet");
        StringBuilder urlBuilder = newRelativeUrlBuilder(context, reverseRoute);
        String currentAbsoluteUrl = newAbsoluteUrlBuilder(context,
                context.getContextPath() + context.getRequestPath()).toString();
        return urlBuilder
                .append("&continue=")
                .append(Escapers.encodePercent(currentAbsoluteUrl))
                .toString();
    }

    /**
     * Constructs restore password URL by given parameters. URL may be sent in email.
     * URL is absolute.
     *
     * @param token Restore password token.
     * @return Restore password URL.
     */
    public String getRestorePasswordUrl(String token) {
        String reverseRoute = router.getReverseRoute(RestorePasswordController.class, "restorePasswordGet");
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(context, reverseRoute);
        return urlBuilder
                .append("&restoreToken=").append(Escapers.encodePercent(token))
                .append("&continue=")
                .append(Escapers.encodePercent(getContinueUrlParameter()))
                .toString();
    }

    /**
     * Constructs application index URL.
     * URL is relative.
     *
     * @return Relative URL to application index.
     */
    public String getIndexUrl() {
        String reverseRoute = router.getReverseRoute(ApplicationController.class, "index");
        return newRelativeUrlBuilder(context, reverseRoute.replaceAll("\\.\\*", "")).toString();
    }

    /**
     * Returns absolute URL string builder for constructing URLs.
     *
     * @param route Application route to controller without context path.
     * @param context Context.
     * @return URL StringBuilder.
     */
    private StringBuilder newAbsoluteUrlBuilder(Context context, String route) {
        return newAbsoluteUrlBuilder(context, baseUrl, route);
    }

    /**
     * Returns absolute URL string builder for constructing URLs.
     *
     * @param context Context.
     * @param baseUrl Base URL.
     * @param route Application path (without context path).
     * @return URL StringBuilder.
     */
    private StringBuilder newAbsoluteUrlBuilder(Context context, String baseUrl, String route) {
        String lang = (String) context.getAttribute(LanguageFilter.LANG);
        return new StringBuilder(baseUrl)
                .append(route)
                .append("?")
                .append(LanguageFilter.LANG)
                .append("=")
                .append(lang);
    }

    /**
     * Returns relative URL builder with optional context path and language parameter.
     *
     * @param context Context.
     * @param baseUrl Path.
     * @return Relative URL.
     */
    private StringBuilder newRelativeUrlBuilder(Context context, String baseUrl) {
        return newAbsoluteUrlBuilder(context, "", baseUrl);
    }
}
