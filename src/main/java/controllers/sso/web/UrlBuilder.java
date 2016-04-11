package controllers.sso.web;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import controllers.sso.auth.SignUpVerificationController;
import controllers.sso.captcha.CaptchaController;
import controllers.sso.filters.LanguageFilter;
import ninja.Context;
import ninja.Router;
import ninja.utils.NinjaProperties;

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
            @Named("ssoContext") Context context,
            @Named("allowedContinueUrls") List<String> allowedContinueUrls) {
        this.properties = properties;
        this.router = router;
        this.context = context;
        this.allowedContinueUrls = allowedContinueUrls;
        this.baseUrl = properties.getOrDie("application.baseUrl");
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
     * Extracts the redirect URL to the final destination from given context. Also, checks
     * provided URL to match only allowed URL prefixes which saves application from open
     * redirect problem.
     *
     * @return Continue URL to the final project or base URL of application.
     */
    public String getContinueUrlParameter() {
        String url = Strings.nullToEmpty(context.getParameter("continue")).trim();
        if (url.isEmpty()) {
            return baseUrl;
        }
        for (String urlPrefix : allowedContinueUrls) {
            if (url.startsWith(urlPrefix)) {
                return url;
            }
        }
        // Nothing matched. Returning default which is a base URL.
        return baseUrl;
    }

    /**
     * Constructs confirmation URL by given parameters.
     *
     * @param emailConfirmationToken Confirmation code.
     * @return Confirmation URL.
     */
    public String getEmailConfirmationUrl(String emailConfirmationToken) {
        String reverseRoute = router.getReverseRoute(SignUpVerificationController.class, "confirmEmail");
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(context, reverseRoute);
        return urlBuilder
                .append("&token=")
                .append(Escapers.encodePercent(emailConfirmationToken))
                .append("&continue=")
                .append(Escapers.encodePercent(getContinueUrlParameter()))
                .toString();
    }

    /**
     * Returns URL to Sign Up welcome page after successful user sign up and confirmation email sent.
     *
     * @param signUpVerificationToken Sign up verification token.
     * @return URL to sign up verification page.
     */
    public String getSignUpVerificationPage(String signUpVerificationToken) {
        String reverseRoute = router.getReverseRoute(SignUpVerificationController.class, "verifySignUp");
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(context, reverseRoute);
        return urlBuilder
                .append("&token=")
                .append(Escapers.encodePercent(signUpVerificationToken))
                .append("&continue=")
                .append(Escapers.encodePercent(getContinueUrlParameter()))
                .toString();
    }

    /**
     * Returns redirect URL to Sign Up welcome page after successful user sign up and confirmation email sent.
     *
     * @return Sign in URL.
     */
    public String getSignInUrl(String... status) {
        String reverseRoute = "TODO-INSERT-SIGNIN-CONTROLLER-PATH";
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(context, reverseRoute);
        return urlBuilder
                .append("&continue=")
                .append(Escapers.encodePercent(getContinueUrlParameter()))
                .toString();
    }

    /**
     * Returns URL string builder for constructing URLs.
     *
     * @param route Application route to controller without context path.
     * @param context Context.
     * @return URL StringBuilder.
     */
    private StringBuilder newAbsoluteUrlBuilder(Context context, String route) {
        return newAbsoluteUrlBuilder(baseUrl, context, route);
    }

    /**
     * Returns URL string builder for constructing URLs.
     *
     * @param baseUrl Base URL.
     * @param context Context.
     * @param route Application path (without context path).
     * @return URL StringBuilder.
     */
    private StringBuilder newAbsoluteUrlBuilder(String baseUrl, Context context, String route) {
        String cp = Strings.nullToEmpty(context.getContextPath());
        if ("/".equals(cp)) {
            cp = "";
        }
        String lang = (String) context.getAttribute(LanguageFilter.LANG);
        return new StringBuilder(baseUrl)
                .append(cp)
                .append(route)
                .append("?")
                .append(LanguageFilter.LANG)
                .append("=")
                .append(lang);
    }

    /**
     * Returns relative URL builder with optional context path and language parameter.
     *
     * @param path Path.
     * @param context Context.
     * @return Relative URL.
     */
    private StringBuilder newRelativeUrlBuilder(String path, Context context) {
        return newAbsoluteUrlBuilder("", context, path);
    }
}
