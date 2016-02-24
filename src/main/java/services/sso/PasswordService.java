package services.sso;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.sso.User;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Password service to generate salts and password hashes. Uses SHA-512 and {@link SecureRandom}.
 */
@Singleton
public class PasswordService {

    static final int HASHING_LENGTH = 64;

    static final Charset UTF8 = Charset.forName("UTF-8");

    final SecureRandom random;

    final HashFunction hashFunction;

    final BaseEncoding baseEncoding;

    @Inject
    public PasswordService() {
        this.random = new SecureRandom();
        this.hashFunction = Hashing.sha512();
        this.baseEncoding = BaseEncoding.base64Url();
    }

    public byte[] newSalt() {
        byte[] bytes = new byte[HASHING_LENGTH];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] passwordHash(String password, byte[] salt) {
        return hashFunction.newHasher(HASHING_LENGTH)
                .putBytes(salt)
                .putString(password, UTF8)
                .hash()
                .asBytes();
    }

    public boolean isValidPassword(String password, byte[] salt, byte[] passwordHash) {
        byte[] hashToCompare = passwordHash(password, salt);
        return Arrays.equals(hashToCompare, passwordHash);
    }

    public boolean isValidPassword(String password, User user) {
        return isValidPassword(password,
                user.getPasswordSalt(),
                user.getPasswordHash());
    }
}
