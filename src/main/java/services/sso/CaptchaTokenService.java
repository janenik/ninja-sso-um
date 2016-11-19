package services.sso;

import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenEncryptorException;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.cache.NinjaCache;
import ninja.utils.NinjaProperties;
import services.sso.token.ExpirableTokenEncryptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Captcha token service. Generates random human readable values to be displayed on captcha and validates user input.
 * Loads dictionary into the memory.
 */
@Singleton
public class CaptchaTokenService {

    /**
     * Random.
     */
    private final Random random = new SecureRandom();

    /**
     * Token encryptor.
     */
    private final ExpirableTokenEncryptor encryptor;

    /**
     * Caches used tokens for some time (to prevent subsequent usage). Use clustered cache for scalability
     * (configured in conf/application.conf).
     */
    private final NinjaCache cache;

    /**
     * Time to live for captcha token, as string, in seconds.
     */
    private final String ttlAsString;

    /**
     * Time to live for captcha token, in millis.
     */
    private final long ttlInMillis;

    /**
     * Captcha alphabet.
     */
    private final String alphabet;

    /**
     * Number of characters in captcha.
     */
    private final long length;

    /**
     * Constructs captcha token service.
     *
     * @param cache Cache.
     * @param properties Properties.
     * @param encryptor Encryptor.
     */
    @Inject
    public CaptchaTokenService(NinjaCache cache, NinjaProperties properties, ExpirableTokenEncryptor encryptor) {
        this.encryptor = encryptor;
        this.cache = cache;
        this.alphabet = properties.getWithDefault(
                "application.sso.captcha.aphabet",
                "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ"); // No 0 and O.
        this.length = properties.getIntegerWithDefault("application.sso.captcha.length", 5);
        int ttlInSeconds = properties.getIntegerWithDefault("application.sso.captcha.ttl", 300);
        this.ttlAsString = ttlInSeconds + "s";
        this.ttlInMillis = ttlInSeconds * 1000L;
    }

    /**
     * Generates new captcha random word.
     *
     * @return New captcha random word.
     */
    private String nextCaptchaRandomWord() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    /**
     * Creates expirable encrypted token with captcha.
     *
     * @param word Word to encrypt in captcha token.
     * @return Expirable encrypted token with captcha.
     * @throws IllegalStateException In case token encryptor is not set up properly.
     */
    public String newCaptchaToken(String word) {
        ExpirableToken token = ExpirableToken.newCaptchaToken("captcha", word, ttlInMillis);
        try {
            return encryptor.encrypt(token);
        } catch (ExpirableTokenEncryptorException ee) {
            throw new IllegalStateException("Unexpected exception while encrypting the captcha.", ee);
        }
    }

    /**
     * Creates expirable encrypted token with captcha.
     *
     * @return Expirable encrypted token with captcha.
     * @throws IllegalStateException In case token encryptor is not set up properly.
     */
    public String newCaptchaToken() {
        return newCaptchaToken(nextCaptchaRandomWord());
    }

    /**
     * Verifies given token and captcha user input.
     *
     * @param token Token to verify.
     * @param userProvidedValue User input.
     * @throws AlreadyUsedTokenException In case if token is valid but has been already used.
     * @throws ExpiredTokenException In case when captcha token is expired.
     * @throws IllegalTokenException In case when invalid token is given.
     * @throws InvalidTokenValueException In case when invalid value is given.
     */
    public void verifyCaptchaToken(String token, String userProvidedValue)
            throws AlreadyUsedTokenException, ExpiredTokenException, IllegalTokenException, InvalidTokenValueException {
        String captchaValue = extractCaptchaText(token);
        if (captchaValue.equalsIgnoreCase(userProvidedValue)) {
            invalidateToken(token);
        } else {
            throw new InvalidTokenValueException();
        }
    }

    /**
     * Parses given token and returns internal code if the token is correct and not expired.
     *
     * @param captchaToken Token.
     * @return Captcha text.
     * @throws CaptchaTokenService.AlreadyUsedTokenException When token is already used.
     * @throws ExpiredTokenException Expired token.
     * @throws IllegalTokenException Wrong/illegal token.
     */
    public String extractCaptchaText(String captchaToken)
            throws AlreadyUsedTokenException, ExpiredTokenException, IllegalTokenException {
        if (captchaToken == null || captchaToken.isEmpty()) {
            throw new IllegalTokenException();
        }
        if (isUsedToken(captchaToken)) {
            // Token was already used.
            throw new AlreadyUsedTokenException();
        }
        ExpirableToken tok = encryptor.decrypt(captchaToken);
        if (ExpirableTokenType.CAPTCHA.equals(tok.getType())) {
            return tok.getAttributeValue("captcha");
        }
        throw new IllegalTokenException();
    }

    /**
     * Checks in cache if the given token is used.
     *
     * @param captchaToken Captcha token to check.
     * @return Whether the given token is used.
     */
    private boolean isUsedToken(String captchaToken) {
        return cache.get(captchaToken, String.class) != null;
    }

    /**
     * Invalidates given token by remembering it in cache for time, equal to lifetime of captcha.
     *
     * @param token Token to invalidate.
     */
    private void invalidateToken(String token) {
        cache.add(token, "", ttlAsString);
    }

    /**
     * Exception for cases when the captcha token is already used.
     */
    public static class AlreadyUsedTokenException extends Exception {
    }

    /**
     * Exception for cases when the value in captcha token doesn't match user input.
     */
    public static class InvalidTokenValueException extends Exception {
    }
}
