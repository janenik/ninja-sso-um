package services.sso.token;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

/**
 * AES password based encryptor/decryptor.
 */
public class AesPasswordBasedEncryptor implements PasswordBasedEncryptor {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String ALGORITHM_FOR_KEY_SPEC = "AES";

    private static final String KEY_GENERATOR = "PBKDF2WithHmacSHA1";

    /**
     * Using thread local {@link SecureRandom} for efficiency and uniform distribution.
     */
    private static final ThreadLocal<SecureRandom> secureRandom = new ThreadLocal<SecureRandom>() {
        @Override
        protected SecureRandom initialValue() {
            return new SecureRandom();
        }
    };

    private final char[] password;
    private final short keySize;
    private final short saltSize;
    private final int passwordIterations;
    private final int readBufferSize;

    /**
     * Constructs 128-bit AES PBE with 1024 iterations, salt of 16 bytes and 512 bytes for read buffer.
     *
     * @param password Password
     */
    public AesPasswordBasedEncryptor(char[] password) {
        this(password, 1024, (short) 128, (short) 16, 512);
    }

    /**
     * Constructs arbitrary AES PBE with 1024 iterations, salt of 16 bytes and 512 bytes for read buffer.
     *
     * @param password Password.
     * @param keySize Key size
     */
    public AesPasswordBasedEncryptor(char[] password, short keySize) {
        this(password, 1024, keySize, (short) 16, 512);
    }

    /**
     * Constructs arbitrary AES password based encryptor (PBE).
     *
     * @param password Password.
     * @param passwordIterations Iterations to construct a hash from password.
     * @param keySize Key size in bits.
     * @param saltSize Salt size.
     * @param readBufferSize Read buffer size for encryption/decryption.
     */
    public AesPasswordBasedEncryptor(char[] password, int passwordIterations, short keySize, short saltSize,
                                     int readBufferSize) {
        this.password = password;
        this.keySize = keySize;
        this.saltSize = saltSize;
        this.passwordIterations = passwordIterations;
        this.readBufferSize = readBufferSize;
    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws IOException, EncryptionException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            KeySpecAndSalt keySpecAndSalt = new KeySpecAndSalt(password, passwordIterations, keySize, saltSize);

            cipher.init(Cipher.ENCRYPT_MODE, keySpecAndSalt.encryptionKeySpec);
            byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

            DataOutputStream dos = new DataOutputStream(outputStream);

            dos.write(keySize >>> 3);
            dos.writeShort(keySpecAndSalt.salt.length);
            dos.writeShort(iv.length);
            dos.write(keySpecAndSalt.salt);
            dos.write(iv);

            byte[] readBuffer = new byte[readBufferSize];
            int readBytes;
            byte[] encrypted;
            while ((readBytes = inputStream.read(readBuffer)) > 0) {
                encrypted = cipher.update(readBuffer, 0, readBytes);
                if (encrypted != null) {
                    outputStream.write(encrypted);
                }
            }
            encrypted = cipher.doFinal();
            if (encrypted != null) {
                outputStream.write(encrypted);
            }

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidParameterSpecException |
                BadPaddingException | IllegalBlockSizeException e) {
            throw new EncryptionException(e.getMessage(), e);
        } catch (InvalidKeyException ike) {
            throw new EncryptionException("Unable to use strong encryption.", ike);
        }
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws IOException, DecryptionException {
        DataInputStream dis = new DataInputStream(inputStream);
        short encryptedKeySize = (short) (dis.read() << 3);
        short encryptedSaltLength = dis.readShort();
        short ivLength = dis.readShort();

        byte[] salt = new byte[encryptedSaltLength];
        dis.read(salt);

        byte[] iv = new byte[ivLength];
        dis.read(iv);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            KeySpecAndSalt keySpecAndSalt = new KeySpecAndSalt(password, passwordIterations, encryptedKeySize, salt);

            cipher.init(Cipher.DECRYPT_MODE, keySpecAndSalt.encryptionKeySpec, new IvParameterSpec(iv));

            byte[] readBuffer = new byte[readBufferSize];

            int readBytes;
            byte[] decrypted;
            while ((readBytes = dis.read(readBuffer)) > 0) {
                decrypted = cipher.update(readBuffer, 0, readBytes);
                if (decrypted != null) {
                    outputStream.write(decrypted);
                }
            }
            decrypted = cipher.doFinal();
            if (decrypted != null) {
                outputStream.write(decrypted);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException |
                InvalidAlgorithmParameterException e) {
            throw new DecryptionException(e.getMessage(), e);
        } catch (InvalidKeyException ike) {
            throw new DecryptionException("Unable to use strong encryption.", ike);
        }
    }

    /**
     * Encryption/decryption key plus salt.
     */
    private static class KeySpecAndSalt {

        private final SecretKey encryptionKeySpec;
        private final byte[] salt;

        /**
         * Constructs key spec and with generated salt of given size.
         *
         * @param password Password for key.
         * @param passwordIterations Number of iterations to use for building safe hash.
         * @param keyLength Key length (128, 192 or 256).
         * @param saltSize Salt size.
         * @throws EncryptionException Exception in case of key building.
         */
        public KeySpecAndSalt(char[] password, int passwordIterations, short keyLength, short saltSize)
                throws EncryptionException {
            byte[] newSalt = new byte[saltSize];
            secureRandom.get().nextBytes(newSalt);
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_GENERATOR);
                KeySpec keySpec = new PBEKeySpec(password, newSalt, passwordIterations, keyLength);
                SecretKey secretKey = factory.generateSecret(keySpec);
                this.encryptionKeySpec = new SecretKeySpec(secretKey.getEncoded(), ALGORITHM_FOR_KEY_SPEC);
                this.salt = newSalt;
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new EncryptionException("Initialization error.", e);
            }
        }

        /**
         * Constructs key spec with given salt.
         *
         * @param password Password for key.
         * @param passwordIterations Number of iterations to use for building safe hash.
         * @param keyLength Key length (128, 192 or 256).
         * @param salt Salt.
         * @throws EncryptionException Exception in case of key building.
         */
        public KeySpecAndSalt(char[] password, int passwordIterations, short keyLength, byte[] salt)
                throws DecryptionException {
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_GENERATOR);
                KeySpec keySpec = new PBEKeySpec(password, salt, passwordIterations, keyLength);
                SecretKey secretKey = factory.generateSecret(keySpec);
                this.encryptionKeySpec = new SecretKeySpec(secretKey.getEncoded(), ALGORITHM_FOR_KEY_SPEC);
                this.salt = salt;
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new DecryptionException("Initialization error.", e);
            }
        }
    }
}
