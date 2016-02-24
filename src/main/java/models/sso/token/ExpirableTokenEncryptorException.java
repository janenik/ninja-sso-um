package models.sso.token;

/**
 * Base class for token exceptions.
 */
public class ExpirableTokenEncryptorException extends Exception {

    public ExpirableTokenEncryptorException() {
        super();
    }

    public ExpirableTokenEncryptorException(Throwable th) {
        super(th);
    }
}
