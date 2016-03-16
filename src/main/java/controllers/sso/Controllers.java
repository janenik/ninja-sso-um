package controllers.sso;

import controllers.sso.rest.TypedRestResponse;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.Validation;

import java.util.Collections;
import java.util.List;

/**
 * Helps class for all controllers. Contains templates for RESTful responses.
 */
public final class Controllers {

    /**
     * No violation validation.
     */
    public static final Validation NO_VIOLATIONS = new NoViolations();

    /**
     * Bad request result, simplified.
     *
     * @param field Field to report.
     * @param message Message.
     * @param context Context.
     * @return Result.
     */
    public static Result badRequest(String field, String message, Context context) {
        return jsonOrJsonpResponse(context,
                TypedRestResponse.badRequestWithViolation(field, message, field + ": " + message));
    }

    /**
     * Bad request result, simplified.
     *
     * @param field Field to report.
     * @param ex Exception occurred on field.
     * @param context Context.
     * @return Bad request response.
     */
    public static Result badRequest(String field, Exception ex, Context context) {
        return jsonOrJsonpResponse(context, TypedRestResponse.badRequestWithViolation(field, ex));
    }

    /**
     * Typed response with object as body.
     *
     * @param object Object to response.
     * @return JSON response.
     */
    public static Result json(Object object) {
        return Results.json().render(TypedRestResponse.newResponse(object));
    }

    /**
     * Typed response with object as body.
     *
     * @param object Object to response.
     * @param message Message to response.
     * @return JSON response.
     */
    public static Result json(Object object, String message) {
        return Results.json().render(TypedRestResponse.newResponse(object, message));
    }

    /**
     * Typed response with object as body.
     *
     * @param object Object to response.
     * @return JSONP response.
     */
    public static Result jsonp(Object object) {
        return Results.jsonp().render(TypedRestResponse.newResponse(object));
    }

    /**
     * Returns JSON or JSONP (if callback is present) result.
     *
     * @param object Object to render.
     * @param context Context.
     * @return JSON or JSONP result.
     */
    public static Result jsonOnJsonp(Object object, Context context) {
        return jsonOrJsonpResponse(context, TypedRestResponse.<Object>newResponse(object));
    }

    /**
     * Returns common response object JSON or JSONP (if callback is present) result.
     *
     * @param context Context.
     * @return Common success response.
     */
    public static Result successJsonOrJsonp(Context context) {
        return jsonOrJsonpResponse(context, TypedRestResponse.<String>newResponse("success"));
    }

    private static Result jsonOrJsonpResponse(Context context, TypedRestResponse<?> response) {
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
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addViolation(ConstraintViolation constraintViolation) {
            throw new UnsupportedOperationException("Not supported yet.");
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
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addBeanViolation(FieldViolation fieldViolation) {
            throw new UnsupportedOperationException("Not supported yet.");
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
    }
}
