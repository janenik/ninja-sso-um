package services.sso.token;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encrypts and descrypts messages with password.
 */
public interface PasswordBasedEncryptor {

    /**
     * Encrypts given input stream and writes encrypted data to output stream.
     *
     * @param inputStream Input stream.
     * @param outputStream Output stream.
     * @throws IOException Input/output exception in case of problems with read/write operations.
     * @throws EncryptionException Encryption exception in case of problems with encryption. Use cause to identify the
     * reason.
     */
    void encrypt(InputStream inputStream, OutputStream outputStream) throws IOException, EncryptionException;

    /**
     * Descrypts given input stream and writes decrypted data to output stream.
     *
     * @param inputStream Input stream.
     * @param outputStream Output stream.
     * @throws IOException Input/output exception in case of problems with read/write operations.
     * @throws DecryptionException Decryption exception in case of problems with decryption. Use cause to identify the
     * reason.
     */
    void decrypt(InputStream inputStream, OutputStream outputStream) throws IOException, DecryptionException;

    /**
     * Encrypts given data and returns encrypted data.
     *
     * @param data Data.
     * @return Encrypted data.
     * @throws EncryptionException Encryption exception in case of problems with encryption. Use cause to identify the
     * reason.
     */
    default byte[] encrypt(byte[] data) throws EncryptionException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(3 * data.length / 2);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try {
            encrypt(bis, bos);
        } catch (IOException ioe) {
            throw new EncryptionException("Unexpected error with byte arrays.", ioe);
        }
        return bos.toByteArray();
    }

    /**
     * Decrypts given bytes and returns decrypted data.
     *
     * @param encrypted Data.
     * @return Decrypted data.
     * @throws DecryptionException Decryption exception in case of problems with encryption. Use cause to identify the
     * reason.
     */
    default byte[] decrypt(byte[] encrypted) throws DecryptionException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(encrypted.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(encrypted);
        try {
            decrypt(bis, bos);
        } catch (IOException ioe) {
            throw new DecryptionException("Unexpected error with byte arrays.", ioe);
        }
        return bos.toByteArray();
    }

    /**
     * Encryption exception. See cause for details.
     */
    class EncryptionException extends Exception {

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Decryption exception. See cause for details.
     */
    class DecryptionException extends Exception {

        public DecryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
