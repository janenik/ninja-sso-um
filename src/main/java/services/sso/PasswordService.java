package services.sso;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import models.sso.User;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Password service to generate salts and password hashes. Uses SHA-512 and {@link SecureRandom}.
 */
@Singleton
public class PasswordService {

    /**
     * Length of hash.
     */
    static final int HASHING_LENGTH = 64;

    /**
     * Secure random instance.
     */
    final SecureRandom random;

    /**
     * Hash function: sha-512.
     */
    final HashFunction hashFunction;

    /**
     * Base 64 encoding.
     */
    final BaseEncoding baseEncoding;

    /**
     * Constructs service.
     */
    public PasswordService() {
        this.random = new SecureRandom();
        this.hashFunction = Hashing.sha512();
        this.baseEncoding = BaseEncoding.base64Url().omitPadding();
    }

    /**
     * Generates new salt of size {@link PasswordService#HASHING_LENGTH}.
     *
     * @return Generated salt.
     */
    public byte[] newSalt() {
        byte[] bytes = new byte[HASHING_LENGTH];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates password hash for given password and salt.
     *
     * @param password Password.
     * @param salt Salt.
     * @return Password hash.
     */
    public byte[] passwordHash(String password, byte[] salt) {
        return hashFunction.newHasher(HASHING_LENGTH)
                .putBytes(salt)
                .putString(password, StandardCharsets.UTF_8)
                .hash()
                .asBytes();
    }

    /**
     * Checks if the given password and salt matches given hash.
     *
     * @param password Password to check.
     * @param salt Salt to use.
     * @param passwordHash Password hash for comparison.
     * @return Whether the given password and salt matches given hash.
     */
    public boolean isValidPassword(String password, byte[] salt, byte[] passwordHash) {
        byte[] hashToCompare = passwordHash(password, salt);
        return Arrays.equals(hashToCompare, passwordHash);
    }

    /**
     * Checks if the given password matches password of the given user.
     *
     * @param password Password.
     * @param user User.
     * @return Whether the given password matches password of the given user.
     */
    public boolean isValidPassword(String password, User user) {
        return isValidPassword(password,
                user.getPasswordSalt(),
                user.getPasswordHash());
    }
}
