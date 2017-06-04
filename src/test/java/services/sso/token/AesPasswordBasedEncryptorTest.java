package services.sso.token;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests and benchmarks {@link AesPasswordBasedEncryptor}.
 */
public class AesPasswordBasedEncryptorTest {

    private static final Logger logger = LoggerFactory.getLogger(AesPasswordBasedEncryptorTest.class);

    private byte[] data;
    private char[] passwordCharacters;

    @Before
    public void setUp() {
        this.data = "(?) 1982 Some data to encrypt and decrypt. 1982 Some data to encrypt and decrypt.".
                getBytes(StandardCharsets.UTF_8);
        this.passwordCharacters = "password_1234567890_1234567890_!@#$%^&*()_+<>?{}|".toCharArray();
    }

    /**
     * AES-128 bit encryption is supposed to be supported without JCE Unlimited Strength Jurisdiction Policy.
     *
     * @throws NoSuchAlgorithmException In case if algorithm is not supported.
     */
    @Test
    public void testAes128() throws NoSuchAlgorithmException {
        invokeAesTest((short) 128, true);
    }

    /**
     * AES-192 bit encryption is supposed to be supported with JCE Unlimited Strength Jurisdiction Policy only.
     *
     * @throws NoSuchAlgorithmException In case if algorithm is not supported.
     */
    @Test
    public void testAes192() throws NoSuchAlgorithmException {
        invokeAesTest((short) 192, true);
    }

    /**
     * AES-256 bit encryption is supposed to be supported with JCE Unlimited Strength Jurisdiction Policy only.
     *
     * @throws NoSuchAlgorithmException In case if algorithm is not supported.
     */
    @Test
    public void testAes256() throws NoSuchAlgorithmException {
        invokeAesTest((short) 256, true);
    }

    /**
     * Provides some information about speed of token encryption.
     *
     * @throws NoSuchAlgorithmException In case if algorithm is not supported.
     */
    @Test
    public void testAes128Speed() throws NoSuchAlgorithmException {
        int iterations = 100;
        long start;
        double duration,
                totalTime = 0.0D,
                maxTime = Double.MIN_VALUE,
                minTime = Double.MAX_VALUE;
        invokeAesTest((short) 128, false);
        for (int i = 0; i < iterations; i++) {
            start = System.nanoTime();
            invokeAesTest((short) 128, false);
            duration = (System.nanoTime() - start) / 1e9;
            totalTime += duration;
            minTime = Math.min(minTime, duration);
            maxTime = Math.max(maxTime, duration);
            if (i > 0 && i % 1000 == 0) {
                logger.info("Testing {}...", i);
            }
        }
        logger.info("Executed {} encryptions/decryptions.\n\tTotal: {} sec, avg: {} sec, min: {} sec, max: {} sec",
                iterations, totalTime, totalTime / iterations, minTime, maxTime);
    }

    /**
     * Starts AES encryption checkLimit with given key size.
     *
     * @param keySize Key size, bits.
     * @param logSuccess Whether to log success or not.
     * @throws NoSuchAlgorithmException If there is no AES algorithm.
     */
    private void invokeAesTest(short keySize, boolean logSuccess) throws NoSuchAlgorithmException {
        if (keySize > 128 && !isUnlimitedStrengthCryptographySupported()) {
            logger.warn("Unlimited policy is not installed: {} bit encryption is not expected to work.", keySize);
        }

        try {
            AesPasswordBasedEncryptor encryptor = new AesPasswordBasedEncryptor(passwordCharacters, keySize);

            byte[] encrypted = encryptor.encrypt(data);
            byte[] decrypted = encryptor.decrypt(encrypted);

            assertArrayEquals("Decrypted data must match the original.", data, decrypted);
            if (logSuccess) {
                logger.info("{} bits: success. Data: {}, encrypted: {} (bytes).", keySize,
                        data.length, decrypted.length);
            }
        } catch (PasswordBasedEncryptor.EncryptionException | PasswordBasedEncryptor.DecryptionException ee) {
            logger.warn("{} bit encryption does not work: {} / {}",
                    keySize,
                    ee.getMessage(),
                    ee.getCause().getMessage());
            if (keySize <= 128) {
                // Propagate exception in case if basic 128bit encryption is not supported..
                throw new RuntimeException(ee);
            }
        }
    }

    /**
     * Returns true if unlimited policy is enabled.
     *
     * @return whether unlimited policy is enabled.
     * @throws NoSuchAlgorithmException Should not happen.
     */
    private static boolean isUnlimitedStrengthCryptographySupported() throws NoSuchAlgorithmException {
        return Cipher.getMaxAllowedKeyLength("AES") > 128;
    }
}