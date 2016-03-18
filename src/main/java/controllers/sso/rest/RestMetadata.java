package controllers.sso.rest;

import java.io.Serializable;

/**
 * REST metadata container with error code and message for the REST responses.
 */
public class RestMetadata implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * Response code.
     */
    private final int code;

    /**
     * Status message.
     */
    private final String message;

    /**
     * Constructs metadata with 200/OK response.
     */
    public RestMetadata() {
        this.code = 200;
        this.message = "OK";
    }

    /**
     * Constructs metadata with given error code.
     *
     * @param code Error code.
     */
    public RestMetadata(int code) {
        this.code = code;
        this.message = null;
    }

    /**
     * Constructs metadata with given parameters.
     *
     * @param code Respnse code.
     * @param statusMessage Status message.
     */
    public RestMetadata(int code, String statusMessage) {
        this.code = code;
        this.message = statusMessage;
    }

    /**
     * Returns response code.
     *
     * @return Response code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Status message.
     *
     * @return Status message.
     */
    public String getMessage() {
        return message;
    }
}
