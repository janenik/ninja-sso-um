package models.sso.token;

/**
 * Base class for encryptor exceptions.
 */
public class ExpirableTokenEncryptorException extends Exception {

    public ExpirableTokenEncryptorException(String message, Throwable th) {
        super(message, th);
    }

    public ExpirableTokenEncryptorException() {
        super();
    }

    public ExpirableTokenEncryptorException(Throwable th) {
        super(th);
    }
}
