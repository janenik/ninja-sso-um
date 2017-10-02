package controllers.sso.rest;

import ninja.Result;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for generic REST response.
 *
 * @param <T> Response type.
 */
public class RestResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Metadata for the response.
     */
    private final RestMetadata meta;

    /**
     * Data.
     */
    private final T data;

    /**
     * Constructs response with given data and 200/OK metadata.
     *
     * @param data Response data.
     */
    private RestResponse(T data) {
        this.data = data;
        this.meta = new RestMetadata();
    }

    /**
     * Constructs response with given data and metadata parameters.
     *
     * @param data    Data.
     * @param status  Status code for metadata.
     * @param message Status message for metadata.
     */
    private RestResponse(T data, int status, String message) {
        this.data = data;
        this.meta = new RestMetadata(status, message);
    }

    /**
     * Returns data from REST response.
     *
     * @return Data.
     */
    public T getData() {
        return data;
    }

    /**
     * Returns metadata.
     *
     * @return Metadata.
     */
    public RestMetadata getMeta() {
        return meta;
    }

    /**
     * Static factory for 200/OK response.
     *
     * @param data Data for response.
     * @param <T>  Data type.
     * @return REST response with data and 200/OK metadata.
     */
    public static <T> RestResponse<T> newResponse(T data) {
        return new RestResponse<>(data);
    }

    /**
     * Static factory for 200 response and given message.
     *
     * @param data    Data for response.
     * @param message Status message.
     * @param <T>     Data type.
     * @return REST response with data and 200 code in metadata.
     */
    public static <T> RestResponse<T> newResponse(T data, String message) {
        return new RestResponse<>(data, Result.SC_200_OK, message);
    }

    /**
     * Static factory for 400 (bad) response and given message.
     *
     * @param message Status message.
     * @param <T>     Data type.
     * @return REST response with data and 400 code in metadata.
     */
    public static <T> RestResponse<T> badRequest(String message) {
        return new RestResponse<>(null, Result.SC_400_BAD_REQUEST, message);
    }

    /**
     * Static factory for 400 (bad) response and given data and message..
     *
     * @param message Status message.
     * @param data    REST data.
     * @param <T>     Data type.
     * @return REST response with data and 400 code in metadata.
     */
    public static <T> RestResponse<T> badRequest(String message, T data) {
        return new RestResponse<>(data, Result.SC_400_BAD_REQUEST, message);
    }

    /**
     * Static factory for 400 (bad) response by given violation field name (validation errors) and exception.
     *
     * @param fieldName Field name.
     * @param e         Exception.
     * @return REST response with 400 status code and violation field.
     */
    public static RestResponse<Map<String, String>> badRequestWithViolation(String fieldName, Exception e) {
        String violationMessage =
                e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
        FieldViolation violation = new FieldViolation(fieldName,
                new ConstraintViolation(fieldName, fieldName, violationMessage));
        return badRequestWithViolations(Collections.singletonList(violation));
    }

    /**
     * Static factory for 400 (bad) response by given violation field name (validation error) and constraint violation
     * kes as string.
     *
     * @param fieldName Field name.
     * @param key       Constraint violation key as string.
     * @return REST response with 400 status code and violation field.
     */
    public static RestResponse<Map<String, String>> badRequestWithViolation(String fieldName, String key) {
        FieldViolation violation = new FieldViolation(fieldName,
                new ConstraintViolation(fieldName, fieldName, fieldName));
        return badRequestWithViolations(Collections.singletonList(violation), key);
    }

    /**
     * Static factory for 400 (bad) response by given violation field name (validation error) and constraint violation
     * kes as string.
     *
     * @param fieldName Field name.
     * @param key       Constraint violation key as string.
     * @param message   Message for violation (validation error).
     * @return REST response with 400 status code and violation field.
     */
    public static RestResponse<Map<String, String>> badRequestWithViolation(
            String fieldName, String key, String message) {
        FieldViolation violation = new FieldViolation(fieldName, new ConstraintViolation(key, fieldName, message));
        return badRequestWithViolations(Collections.singletonList(violation), message);
    }

    /**
     * Static factory for 400 (bad) response by given violation (validation error) and message.
     *
     * @param violations Single violation (validation error).
     * @return REST response with data and 400 code in metadata.
     */
    public static RestResponse<Map<String, String>> badRequestWithViolation(FieldViolation violations) {
        return badRequestWithViolations(Collections.singletonList(violations));
    }

    /**
     * Static factory for 400 (bad) response and given list of violations (validation errors).
     *
     * @param violations List of violations (validation errors).
     * @return REST response with data and 400 code in metadata.
     */
    public static RestResponse<Map<String, String>> badRequestWithViolations(List<FieldViolation> violations) {
        return badRequestWithViolations(violations, "validation violations");
    }

    /**
     * Static factory for 400 (bad) response and given list of violations (validation errors) and message.
     *
     * @param violations List of violations (validation errors).
     * @param message    Status message.
     * @return REST response with data and 400 code in metadata.
     */
    public static RestResponse<Map<String, String>> badRequestWithViolations(
            List<FieldViolation> violations, String message) {
        Map<String, String> mapped = new HashMap<>(violations.size() + 1);
        mapped.put("invalidValues", "true");
        for (FieldViolation fv : violations) {
            mapped.put(fv.field, fv.constraintViolation.getMessageKey());
        }
        return new RestResponse<>(mapped, Result.SC_400_BAD_REQUEST, message);
    }

    /**
     * Static factory for 404 (not found) response and given message.
     *
     * @param message Status message.
     * @param <T>     Data type.
     * @return REST response with 401 code in metadata.
     */
    public static <T> RestResponse<T> notAuthorized(String message) {
        return new RestResponse<>(null, Result.SC_401_UNAUTHORIZED, message);
    }

    /**
     * Static factory for 404 (forbidden) response and given message.
     *
     * @param message Status message.
     * @param <T>     Data type.
     * @return REST response with 403 code in metadata.
     */
    public static <T> RestResponse<T> forbidden(String message) {
        return new RestResponse<>(null, Result.SC_403_FORBIDDEN, message);
    }

    /**
     * Static factory for 404 (not found) response and given message.
     *
     * @param message Status message.
     * @param <T>     Data type.
     * @return REST response with 404 code in metadata.
     */
    public static <T> RestResponse<T> notFound(String message) {
        return new RestResponse<>(null, Result.SC_404_NOT_FOUND, message);
    }

    /**
     * Static factory for 500 (internal server) response and given data and message..
     *
     * @param e Server throwable.
     * @param <T>     Data type.
     * @return REST response with data and 400 code in metadata.
     */
    public static <T> RestResponse<T> serverError(Throwable e) {
        String errorMessage =
                e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
        return new RestResponse<>(null, Result.SC_500_INTERNAL_SERVER_ERROR, errorMessage);
    }
}
