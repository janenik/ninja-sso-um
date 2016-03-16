package controllers.sso.rest;

import java.io.Serializable;

public class RestMetadata implements Serializable {

    private static final long serialVersionUID = 1;
    private final int code;
    private final String message;

    public RestMetadata() {
        code = 200;
        message = null;
    }

    public RestMetadata(int code) {
        this.code = code;
        message = null;
    }

    public RestMetadata(int code, String errorMessage) {
        this.code = code;
        this.message = errorMessage;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
