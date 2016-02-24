package models.sso.token;

/**
 * Expired token exception.
 */
public class ExpiredTokenException extends ExpirableTokenEncryptorException {

    public ExpiredTokenException() {
        super();
    }

    public ExpiredTokenException(Exception cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "Expired token given.";
    }
}
