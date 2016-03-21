package services.sso;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.cache.NinjaCache;
import ninja.utils.NinjaProperties;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
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
    private static final Random RANDOM = new SecureRandom();

    /**
     * Token encryptor.
     */
    private final ExpirableTokenEncryptor encryptor;

    /**
     * Dictionary.
     */
    private final List<String> dictionary;

    /**
     * Whether the dictionary was read.
     */
    private final boolean realDictionary;

    /**
     * Caches used tokens for some time (to prevent subsequent usage). Use clustered cache for scalability
     * (configured in conf/application.conf).
     */
    private final NinjaCache cache;

    /**
     * Time to live for captcha token, as string, in seconds.
     */
    private final String captchaTTLAsString;

    /**
     * Time to live for captcha token, in millis.
     */
    private final long captchaTTLInMillis;

    /**
     * Constructs captcha token service.
     *
     * @param cache Cache.
     * @param properties Properties.
     * @param encryptor Encryptor.
     * @throws IOException In case of error during reading the dictionary.
     */
    @Inject
    public CaptchaTokenService(NinjaCache cache, NinjaProperties properties, ExpirableTokenEncryptor encryptor)
            throws IOException {
        List<String> result = Lists.newArrayListWithCapacity(10000);
        InputStream is = getClass().getClassLoader().
                getResourceAsStream(properties.getOrDie("application.sso.captcha.dictionary"));
        if (is != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line.toUpperCase().trim());
                }
            }
            realDictionary = true;
        } else {
            realDictionary = false;
        }
        this.dictionary = result;
        this.encryptor = encryptor;
        this.cache = cache;

        int captchaTTLInSeconds = properties.getIntegerWithDefault("application.sso.captcha.ttl", 300);
        this.captchaTTLAsString = captchaTTLInSeconds + "s";
        this.captchaTTLInMillis = captchaTTLInSeconds * 1000L;
    }

    /**
     * Generates new captcha random word.
     *
     * @return New captcha random word.
     */
    private String nextCaptchaRandomWord() {
        if (realDictionary) {
            // Zero not needed.
            String i = Integer.toString(111 + RANDOM.nextInt(889)).replace('0', '5');
            return dictionary.get(RANDOM.nextInt(this.dictionary.size())) + i;
        }
        return Integer.toHexString(1000000 + RANDOM.nextInt(89999999)).toUpperCase();
    }

    /**
     * Creates expirable encrypted token with captcha.
     *
     * @return Expirable encrypted token with captcha.
     * @throws IllegalStateException In case token encryptor is not set up properly.
     */
    public String newCaptchaToken() {
        ExpirableToken token = ExpirableToken.newCaptchaToken("captcha", nextCaptchaRandomWord(), captchaTTLInMillis);
        try {
            return encryptor.encrypt(token);
        } catch (PasswordBasedEncryptor.EncryptionException ee) {
            throw new IllegalStateException("Unexpected exception while encrypting the captcha.", ee);
        }
    }

    /**
     * Verifies given token and captcha user input.
     *
     * @param token Token to verify.
     * @param userProvidedValue User input.
     * @throws AlreadyUsedTokenException In case if token is valid but has been already used.
     * @throws ExpiredTokenException In case when captcha token is expired.
     * @throws IllegalTokenException In case when illegal token is given.
     */
    public void verifyCaptchaToken(String token, String userProvidedValue)
            throws AlreadyUsedTokenException, ExpiredTokenException, IllegalTokenException {
        String captchaValue = extractCaptchaText(token);
        if (captchaValue.equalsIgnoreCase(userProvidedValue)) {
            invalidateToken(token);
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
        cache.safeAdd(token, "", captchaTTLAsString);
    }

    /**
     * In case when the captcha token is already used.
     */
    public static class AlreadyUsedTokenException extends Exception {
    }
}
