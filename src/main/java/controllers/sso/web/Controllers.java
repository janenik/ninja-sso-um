package controllers.sso.web;

import controllers.sso.rest.RestResponse;
import ninja.Context;
import ninja.Cookie;
import ninja.Result;
import ninja.Results;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.Validation;

import java.util.Collections;
import java.util.List;

/**
 * Helper class for all controllers. Contains convenient templates for RESTful responses, validation.
 */
public final class Controllers {

    /**
     * Redirect HTML.
     */
    private static final String REDIRECT_HTML_TEMPLATE = "views/sso/redirect.ftl.html";

    /**
     * No violation validation.
     */
    public static final Validation NO_VIOLATIONS = new NoViolations();

    /**
     * Returns validation object with no violations.
     *
     * @return Validation object with no violations.
     */
    public static Validation noViolations() {
        return NO_VIOLATIONS;
    }

    /**
     * Returns result with 301 location header of the given redirect URL and appropriate HTML with
     * HTML redirects.
     *
     * @param url URL to redirect to.
     * @param cookies Optional cookies.
     * @return Result with location header 301 and HTML redirect.
     */
    public static Result redirect(String url, Cookie... cookies) {
        Result redirect = Results
                .status(Result.SC_301_MOVED_PERMANENTLY)
                .addHeader(Result.LOCATION, url)
                .addHeader("X-Content-Type-Options", "nosniff");
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                redirect.addCookie(cookie);
            }
        }
        return redirect
                .render("url", url)
                .template(REDIRECT_HTML_TEMPLATE);
    }

    /**
     * Bad request result, simplified.
     *
     * @param field Field to report.
     * @param message Message.
     * @param context Context.
     * @return Result.
     */
    public static Result jsonOrJsonPBadRequest(String field, String message, Context context) {
        return jsonOrJsonpResponse(context,
                RestResponse.badRequestWithViolation(field, message, field + ": " + message));
    }

    /**
     * Bad request result, simplified.
     *
     * @param field Field to report.
     * @param ex Exception occurred on field.
     * @param context Context.
     * @return Bad request response.
     */
    public static Result jsonOrJsonPBadRequest(String field, Exception ex, Context context) {
        return jsonOrJsonpResponse(context, RestResponse.badRequestWithViolation(field, ex));
    }

    /**
     * Typed response with object as body.
     *
     * @param object Object to response.
     * @return JSON response.
     */
    public static Result json(Object object) {
        return Results.json().render(RestResponse.newResponse(object));
    }

    /**
     * Typed response with object as body.
     *
     * @param object Object to response.
     * @param message Message to response.
     * @return JSON response.
     */
    public static Result json(Object object, String message) {
        return Results.json().render(RestResponse.newResponse(object, message));
    }

    /**
     * Typed response with object as body.
     *
     * @param object Object to response.
     * @return JSONP response.
     */
    public static Result jsonp(Object object) {
        return Results.jsonp().render(RestResponse.newResponse(object));
    }

    /**
     * Returns JSON or JSONP (if callback is present) result.
     *
     * @param object Object to render.
     * @param context Context.
     * @return JSON or JSONP result.
     */
    public static Result jsonOrJsonp(Object object, Context context) {
        return jsonOrJsonpResponse(context, RestResponse.<Object>newResponse(object));
    }

    /**
     * Returns common response object JSON or JSONP (if callback is present) result.
     *
     * @param context Context.
     * @return Common success response.
     */
    public static Result successJsonOrJsonp(Context context) {
        return jsonOrJsonpResponse(context, RestResponse.<String>newResponse("success"));
    }

    /**
     * Checks if the the "callback" parameter is present then renders JSONP instead of JSON.
     *
     * @param context Context.
     * @param response Response.
     * @return JSON or JSONP.
     */
    private static Result jsonOrJsonpResponse(Context context, RestResponse<?> response) {
        if (context.getParameter("callback") != null) {
            return Results.jsonp().render(response);
        } else {
            return Results.json().render(response);
        }
    }

    private Controllers() {
    }

    /**
     * No violations validation.
     */
    @SuppressWarnings("deprecation")
    public static class NoViolations implements Validation {

        @Override
        public boolean hasViolations() {
            return false;
        }

        @Override
        public boolean hasFieldViolation(String field) {
            return false;
        }

        @Override
        public void addFieldViolation(String field, ConstraintViolation constraintViolation) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void addViolation(ConstraintViolation constraintViolation) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public List<FieldViolation> getFieldViolations() {
            return Collections.emptyList();
        }

        @Override
        public List<FieldViolation> getFieldViolations(String fieldName) {
            return Collections.emptyList();
        }

        @Override
        public List<ConstraintViolation> getGeneralViolations() {
            return Collections.emptyList();
        }

        @Override
        public void addFieldViolation(FieldViolation fieldViolation) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void addBeanViolation(FieldViolation fieldViolation) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean hasBeanViolations() {
            return false;
        }

        @Override
        public List<FieldViolation> getBeanViolations() {
            return Collections.emptyList();
        }

        @Override
        public boolean hasBeanViolation(String name) {
            return false;
        }

        @Override
        public List<FieldViolation> getBeanViolations(String s) {
            return null;
        }
    }
}
