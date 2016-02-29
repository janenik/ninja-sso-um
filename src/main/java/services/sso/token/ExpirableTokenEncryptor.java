package services.sso.token;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.google.inject.Singleton;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;

import java.io.IOException;

/**
 * Expirable encrypted token. Thread safe.
 */
@Singleton
public final class ExpirableTokenEncryptor {

    /**
     * Encryptor.
     */
    private final PasswordBasedEncryptor encryptor;

    /**
     * Base encoding.
     */
    private final BaseEncoding baseEncoding;

    /**
     * JSON object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructs token encryptor with given password, algorithm and provider.
     *
     * @param encryptor Encryptor/decryptor.
     */
    public ExpirableTokenEncryptor(PasswordBasedEncryptor encryptor) {
        this.encryptor = encryptor;
        this.baseEncoding = BaseEncoding.base64Url();
        this.objectMapper = new ObjectMapper();
        // Set up serialization/deserialization to use fields, not methods.
        this.objectMapper.setVisibilityChecker(this.objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    /**
     * Returns encrypted data with added expiration time. When result is decrypted expiration time is checked and if
     * token is expired ExpiredTokenException is thrown (see #decrypt(String)).
     *
     * @param token Token to encrypt.
     * @return Encrypted expirable token as string.
     * @throws PasswordBasedEncryptor.EncryptionException When the problem with encryption happens.
     * @throws IllegalStateException In case of internal JSON processing problem.
     */
    public String encrypt(ExpirableToken token) throws PasswordBasedEncryptor.EncryptionException {
        if (token == null) {
            throw new IllegalArgumentException("Expecting token to encrypt.");
        }
        if (!token.hasAttributes()) {
            throw new IllegalArgumentException("Token is expected to contain some data.");
        }
        try {
            return baseEncoding.encode(encryptor.encrypt(objectMapper.writeValueAsBytes(token)));
        } catch (JsonProcessingException jpe) {
            throw new IllegalStateException("Unable to build JSON from the given token.", jpe);
        }
    }

    /**
     * Decrypts given token and returns internal data if it is not expired.
     *
     * @param token Token to decrypt.
     * @return Decrypted token (never returns null if there are no exception).
     * @throws ExpiredTokenException When the token is legal but expired.
     * @throws IllegalTokenException When the token is not legal.
     */
    public ExpirableToken decrypt(String token)
            throws ExpiredTokenException, IllegalTokenException {
        if (token == null || token.isEmpty()) {
            throw new IllegalTokenException();
        }
        byte[] decrypted;
        try {
            byte[] encryptedTokenBytes = baseEncoding.decode(token);
            decrypted = encryptor.decrypt(encryptedTokenBytes);
        } catch (Exception e) {
            throw new IllegalTokenException(e);
        }
        if (decrypted == null) {
            throw new IllegalTokenException();
        }
        try {
            return objectMapper.readValue(decrypted, ExpirableToken.class);
        } catch (IOException ioe) {
            throw new IllegalTokenException(ioe);
        }
    }
}
