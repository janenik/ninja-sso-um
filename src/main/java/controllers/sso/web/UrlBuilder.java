package controllers.sso.web;

import com.google.common.base.Strings;
import com.google.inject.servlet.RequestScoped;
import controllers.ApplicationController;
import controllers.annotations.AllowedContinueUrls;
import controllers.annotations.InjectedContext;
import controllers.sso.admin.users.EditAccessController;
import controllers.sso.admin.users.EditContactDataController;
import controllers.sso.admin.users.EditPasswordController;
import controllers.sso.admin.users.EditPersonalDataController;
import controllers.sso.admin.users.SendEmailController;
import controllers.sso.admin.users.UsersController;
import controllers.sso.auth.RestorePasswordController;
import controllers.sso.auth.SignInController;
import controllers.sso.auth.SignUpVerificationController;
import controllers.sso.captcha.CaptchaController;
import controllers.sso.filters.LanguageFilter;
import ninja.Context;
import ninja.ReverseRouter;
import ninja.Router;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * Reverse router.
     */
    final ReverseRouter reverseRouter;

    /**
     * Current request context.
     */
    final Context context;

    /**
     * HTTP request.
     */
    final HttpServletRequest servletRequest;

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
     * @param properties          Application properties.
     * @param router              Router.
     * @param context             Context.
     * @param allowedContinueUrls List of allowed continue URLs.
     */
    @Inject
    public UrlBuilder(
            NinjaProperties properties,
            Router router,
            ReverseRouter reverseRouter,
            HttpServletRequest servletRequest,
            @InjectedContext Context context,
            @AllowedContinueUrls List<String> allowedContinueUrls) {
        this.properties = properties;
        this.router = router;
        this.reverseRouter = reverseRouter;
        this.context = context;
        this.servletRequest = servletRequest;
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
        String contextPath = context.getContextPath();
        String reversedRoute = reverseRouter
                .with(CaptchaController::captcha)
                .queryParam(CaptchaController.CAPTCHA_PARAMETER, captchaToken)
                .build();
        if (!reversedRoute.startsWith(contextPath)) {
            return new StringBuilder(contextPath)
                    .append(reversedRoute)
                    .toString();
        }
        return reversedRoute;
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
        // Check whether the continue URL is allowed / whitelisted.
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
        String reverseRoute = reverseRouter.with(SignUpVerificationController::verifyEmail).build();
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(reverseRoute);
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
        String reverseRoute = reverseRouter.with(SignUpVerificationController::verifySignUp).build();
        StringBuilder urlBuilder = newRelativeUrlBuilder(reverseRoute);
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
        String reverseRoute = reverseRouter.with(SignInController::signInGet).build();
        StringBuilder urlBuilder = newRelativeUrlBuilder(reverseRoute);
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
     * Returns URL to Sign In page.
     * URL is absolute.
     *
     * @return Absolute Sign In URL.
     */
    public String getAbsoluteSignInUrl() {
        String reverseRoute = reverseRouter.with(SignInController::signInGet).build();
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(reverseRoute);
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
        String reverseRoute = reverseRouter.with(SignInController::signInGet).build();
        StringBuilder urlBuilder = newRelativeUrlBuilder(reverseRoute);
        String currentAbsoluteUrl = newAbsoluteUrlBuilder(context.getContextPath() + context.getRequestPath()).toString();
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
        String reverseRoute = reverseRouter.with(RestorePasswordController::restorePasswordGet).build();
        StringBuilder urlBuilder = newAbsoluteUrlBuilder(reverseRoute);
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
        String reverseRoute = reverseRouter.with(ApplicationController::index).build();
        return newRelativeUrlBuilder(reverseRoute.replaceAll("\\.\\*", "")).toString();
    }

    /**
     * Constructs application index URL.
     * URL is absolute.
     *
     * @return Absolute URL to application index.
     */
    public String getAbsoluteIndexUrl() {
        String reverseRoute = reverseRouter.with(ApplicationController::index).build();
        return newAbsoluteUrlBuilder(reverseRoute.replaceAll("\\.\\*", "")).toString();
    }

    /**
     * Returns current URL.
     *
     * @param urlParameterKeyValuePairs Additional URL parameters key value pairs.
     * @return Current URL of the application.
     */
    public String getCurrentUrl(String... urlParameterKeyValuePairs) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(servletRequest.getRequestURL());
        urlBuilder.append('?');
        if (urlParameterKeyValuePairs != null && urlParameterKeyValuePairs.length > 0) {
            Map<String, String> existingParameters = extractParametersAsMutableMap(servletRequest.getQueryString());
            String key = null;
            for (int i = 0; i < urlParameterKeyValuePairs.length; i++) {
                if (i % 2 == 0) {
                    key = urlParameterKeyValuePairs[i];
                    continue;
                }
                existingParameters.put(key, urlParameterKeyValuePairs[i]);
            }
            toQueryString(existingParameters, urlBuilder);
        } else {
            urlBuilder.append(servletRequest.getQueryString());
        }
        return urlBuilder.toString();
    }

    /**
     * Constructs admin URL to users section.
     * URL is relative.
     *
     * @param query Optional query parameter. Item at index 0 is a query, item at index 1 is a page.
     * @return Relative URL to admin users section.
     */
    public String getAdminUsersUrl(Object... query) {
        return getAdminEditUserDataUrl(UsersController.class, "users", null, query);
    }

    /**
     * Constructs admin URL to edit personal info.
     * URL is relative.
     *
     * @param query Optional query parameter. Item at index 0 is a query, item at index 1 is a page.
     * @return Relative URL to admin personal data section.
     */
    public String getAdminEditPersonalDataUrl(long userId, Object... query) {
        return getAdminEditUserDataUrl(EditPersonalDataController.class, "get", userId, query);
    }

    /**
     * Constructs admin URL to edit contact info.
     * URL is relative.
     *
     * @param query Optional query parameter. Item at index 0 is a query, item at index 1 is a page.
     * @return Relative URL to admin edit contact data section.
     */
    public String getAdminEditContactDataUrl(long userId, Object... query) {
        return getAdminEditUserDataUrl(EditContactDataController.class, "get", userId, query);
    }

    /**
     * Constructs admin URL to edit user role data.
     * URL is relative.
     *
     * @param query Optional query parameter. Item at index 0 is a query, item at index 1 is a page.
     * @return Relative URL to admin edit contact data section.
     */
    public String getAdminEditUserRoleUrl(long userId, Object... query) {
        return getAdminEditUserDataUrl(EditAccessController.class, "get", userId, query);
    }

    /**
     * Constructs admin URL to edit user password form.
     * URL is relative.
     *
     * @param query Optional query parameter. Item at index 0 is a query, item at index 1 is a page.
     * @return Relative URL to admin edit contact data section.
     */
    public String getAdminEditPasswordUrl(long userId, Object... query) {
        return getAdminEditUserDataUrl(EditPasswordController.class, "get", userId, query);
    }

    /**
     * Constructs admin URL to send email form.
     * URL is relative.
     *
     * @param query Optional query parameter. Item at index 0 is a query, item at index 1 is a page.
     * @return Relative URL to admin edit contact data section.
     */
    public String getAdminSendEmailUrl(long userId, Object... query) {
        return getAdminEditUserDataUrl(SendEmailController.class, "get", userId, query);
    }

    /**
     * Constructs admin URL to edit controllers, passing "userId" parameter to reverse route.
     * URL is relative.
     *
     * @param controllerClass Edit user data controller class.
     * @param methodName      Method name.
     * @param query           Optional query parameter. Item at index 0 is a query, item at index 1 is a page.
     * @return Relative URL to one of the edit user controllers.
     */
    private String getAdminEditUserDataUrl(Class<?> controllerClass, String methodName, Long userId, Object... query) {
        String reverseRoute = userId != null ?
                reverseRouter.with(controllerClass, methodName).pathParam("userId", userId).build() :
                reverseRouter.with(controllerClass, methodName).build();
        StringBuilder builder = newRelativeUrlBuilder(reverseRoute);
        if (query != null && query.length > 0 && query[0] != null && !query[0].toString().isEmpty()) {
            builder.append("&query=");
            builder.append(Escapers.encodePercent(query[0].toString()));
        }
        if (query != null && query.length > 1 && query[1] != null) {
            builder.append("&page=");
            builder.append(Escapers.encodePercent(query[1].toString()));
        }
        return builder.toString();
    }

    /**
     * Returns absolute URL string builder for constructing URLs.
     *
     * @return URL StringBuilder.
     */
    private StringBuilder newAbsoluteUrlBuilder(String route) {
        return newAbsoluteUrlBuilder(baseUrl, route);
    }

    /**
     * Returns absolute URL string builder for constructing URLs.
     *
     * @param baseUrl Base URL.
     * @param route   Application path (without context path).
     * @return URL StringBuilder.
     */
    private StringBuilder newAbsoluteUrlBuilder(String baseUrl, String route) {
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
     * @param route Controller route.
     * @return Relative URL.
     */
    private StringBuilder newRelativeUrlBuilder(String route) {
        return newAbsoluteUrlBuilder("", route);
    }

    /**
     * Returns parameters from given query string.
     *
     * @param query Query part of the URL.
     * @return Mutable map of parameters.
     */
    public static Map<String, String> extractParametersAsMutableMap(String query) {
        if (query == null || query.isEmpty()) {
            return new LinkedHashMap<>();
        }
        String[] pairs = query.replaceAll("&amp;", "&").split("&");
        Map<String, String> parameters = new LinkedHashMap<>(pairs.length);
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1) {
                parameters.put(Escapers.decodePercent(keyValue[0]), Escapers.decodePercent(keyValue[1]));
            }
        }
        return parameters;
    }

    /**
     * Appends given URL parameters as in URL query and appends everything to given string builder.
     *
     * @param parameters   Parameters to concatenate.
     * @param queryBuilder String builder to append to.
     * @return Given query builder for chaining.
     */
    public static StringBuilder toQueryString(Map<String, String> parameters, StringBuilder queryBuilder) {
        int i = parameters.size() - 1;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            queryBuilder.append(Escapers.encodePercent(entry.getKey()));
            queryBuilder.append('=');
            queryBuilder.append(Escapers.encodePercent(entry.getValue()));
            if (i != 0) {
                queryBuilder.append('&');
            }
            i--;
        }
        return queryBuilder;
    }
}
