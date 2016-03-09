package services.sso.token;

import com.google.common.base.Throwables;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests {@link AesPasswordBasedEncryptor} and encryption test.
 */
public class AesPasswordBasedEncryptorTest {

    static final Charset UTF8 = Charset.forName("UTF-8");
    static final Logger logger = LoggerFactory.getLogger(AesPasswordBasedEncryptorTest.class);

    byte[] data;
    char[] passwordCharacters;

    @Before
    public void setUp() {
        this.data = "(?) 1982 Some data to encrypt and decrypt. 1982 Some data to encrypt and decrypt.".getBytes(UTF8);
        this.passwordCharacters = "password_1234567890_1234567890_!@#$%^&*()_+<>?{}|".toCharArray();
    }

    /**
     * AES-128 bit encryption is supposed to be supported without JCE Unlimited Strength Jurisdiction Policy.
     *
     * @throws NoSuchAlgorithmException In case of exception.
     */
    @Test
    public void testAes128() throws NoSuchAlgorithmException {
        invokeAesTest((short) 128, true);
    }

    @Test
    public void testAes192() throws NoSuchAlgorithmException {
        invokeAesTest((short) 192, true);
    }

    @Test
    public void testAes256() throws NoSuchAlgorithmException {
        invokeAesTest((short) 256, true);
    }

    @Test
    public void testAes128Speed() throws NoSuchAlgorithmException {
        int iterations = 100;
        long start;
        double duration, total = 0.0D, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        invokeAesTest((short) 128, false);
        for (int i = 0; i < iterations; i++) {
            start = System.nanoTime();
            invokeAesTest((short) 128, false);
            duration = (System.nanoTime() - start) / 1e9;
            total += duration;
            min = Math.min(min, duration);
            max = Math.max(max, duration);
            if (i > 0 && i % 1000 == 0) {
                logger.info("Testing {}...", i);
            }
        }
        logger.info("Executed {} encryptions/decryptions.\n\tTotal: {} sec, avg: {} sec, min: {} sec, max: {} sec",
                iterations, total, total / iterations, min, max);
    }

    /**
     * Starts AES encryption test with given key size.
     *
     * @param keySize Key size, bits.
     * @param logSuccess Whether to log success or not.
     * @throws NoSuchAlgorithmException If there is no AES algorithm.
     */
    private void invokeAesTest(short keySize, boolean logSuccess) throws NoSuchAlgorithmException {
        if (keySize > 128 && !isUnlimitedStrengthCrypto()) {
            logger.warn("Unlimited policy is not installed: {} bit encryption is not expected to work.", keySize);
        }

        try {
            AesPasswordBasedEncryptor encryptor = new AesPasswordBasedEncryptor(passwordCharacters, keySize);

            byte[] encrypted = encryptor.encrypt(data);
            byte[] decrypted = encryptor.decrypt(encrypted);

            assertArrayEquals("Decrypted data must match the original.", data, decrypted);
            if (logSuccess) {
                logger.info("Encryption test of {} bits succeeded.", keySize);
            }
        } catch (PasswordBasedEncryptor.EncryptionException | PasswordBasedEncryptor.DecryptionException ee) {
            logger.warn("{} bit encryption does not work: {} / {}",
                    keySize,
                    ee.getMessage(),
                    ee.getCause().getMessage());
            if (keySize <= 128) {
                // Propagate exception except base case.
                Throwables.propagate(ee);
            }
        }
    }

    /**
     * Returns true if unlimited policy is enabled.
     *
     * @return whether unlimited policy is enabled.
     * @throws NoSuchAlgorithmException Should not happen.
     */
    static boolean isUnlimitedStrengthCrypto() throws NoSuchAlgorithmException {
        return Cipher.getMaxAllowedKeyLength("AES") > 128;
    }
}