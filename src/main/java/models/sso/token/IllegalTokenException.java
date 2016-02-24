package models.sso.token;

/**
 * Illegal token exception: parse errors, etc.
 */
public class IllegalTokenException extends ExpirableTokenEncryptorException {

    public IllegalTokenException() {
        super();
    }

    public IllegalTokenException(Exception cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "Illegal token given.";
    }
}
