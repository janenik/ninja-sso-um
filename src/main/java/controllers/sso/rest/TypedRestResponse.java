package controllers.sso.rest;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ninja.Result;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;

/**
 * Typed rest response.
 *
 * @param <T> Response type.
 */
public class TypedRestResponse<T> {

    private static final long serialVersionUID = 1L;
    /**
     * Fields.
     */
    private final RestMetadata meta;
    private final T data;

    public TypedRestResponse() {
        this.data = null;
        this.meta = new RestMetadata();
    }

    public TypedRestResponse(T data) {
        this.data = data;
        this.meta = new RestMetadata();
    }

    public TypedRestResponse(T data, int status, String message) {
        this.data = data;
        this.meta = new RestMetadata(status, message);
    }

    public TypedRestResponse(int status, String message) {
        this.data = null;
        this.meta = new RestMetadata(status, message);
    }

    public T getData() {
        return data;
    }

    public RestMetadata getMeta() {
        return meta;
    }

    public static <T> TypedRestResponse<T> newResponse(T data) {
        return new TypedRestResponse<>(data);
    }
    
    public static <T> TypedRestResponse<T> newResponse(T data, String message) {
        return new TypedRestResponse<>(data, Result.SC_200_OK, message);
    }

    public static <T> TypedRestResponse<T> badRequest(String message) {
        return new TypedRestResponse<>(null, Result.SC_400_BAD_REQUEST, message);
    }

    public static <T> TypedRestResponse<T> badRequest(String message, T data) {
        return new TypedRestResponse<>(data, Result.SC_400_BAD_REQUEST, message);
    }

    public static TypedRestResponse<Map<String, String>> badRequestWithViolation(String field, Exception e) {
        return badRequestWithViolations(
                Collections.singletonList(new FieldViolation(field, ConstraintViolation.create(
                e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "")))));
    }

    public static TypedRestResponse<Map<String, String>> badRequestWithViolation(String field, String key) {
        return badRequestWithViolations(
                Collections.singletonList(new FieldViolation(field, ConstraintViolation.create(key))), key);
    }

    public static TypedRestResponse<Map<String, String>> badRequestWithViolation(String field, String key, String message) {
        return badRequestWithViolations(
                Collections.singletonList(new FieldViolation(field, ConstraintViolation.create(key))), message);
    }

    public static TypedRestResponse<Map<String, String>> badRequestWithViolation(FieldViolation violations) {
        return badRequestWithViolations(Collections.singletonList(violations));
    }

    public static TypedRestResponse<Map<String, String>> badRequestWithViolations(List<FieldViolation> violations) {
        return badRequestWithViolations(violations, "validation violations");
    }

    public static TypedRestResponse<Map<String, String>> badRequestWithViolations(List<FieldViolation> violations,
            String message) {
        Map<String, String> mapped = Maps.newHashMapWithExpectedSize(violations.size() + 1);
        mapped.put("invalidValues", "true");
        for (FieldViolation fv : violations) {
            mapped.put(fv.field, fv.constraintViolation.getMessageKey());
        }
        return new TypedRestResponse<>(mapped, Result.SC_400_BAD_REQUEST, message);
    }

    public static <T> TypedRestResponse<T> notAuthorized(String message) {
        return new TypedRestResponse<>(null, 401, message);
    }

    public static <T> TypedRestResponse<T> forbidden(String message) {
        return new TypedRestResponse<>(null, Result.SC_403_FORBIDDEN, message);
    }

    public static <T> TypedRestResponse<T> notFound(String message) {
        return new TypedRestResponse<>(null, Result.SC_404_NOT_FOUND, message);
    }
}
