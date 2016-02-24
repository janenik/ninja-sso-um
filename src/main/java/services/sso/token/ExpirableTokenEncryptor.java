package services.sso.token;

import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.security.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Expirable encrypted token. Thread safe.
 */
public final class ExpirableTokenEncryptor {

    private final static int EXPECTED_TOKEN_LENGTH = 64;
    private final static String DEFAULT_ENCRYPTION_ALGORITHM = "PBEWITHSHA256AND128BITAES-CBC-BC";
    private final static char TOKEN_SEPARATOR = '/';
    private final static String TOKEN_SEPARATOR_ESCAPED = "%2F";
    private final static char PERCENT = '%';
    private final static String PERCENT_ESCAPED = "%25";

    //  private final StandardPBEStringEncryptor encryptor;

    /**
     * Constructs token encryptor with given password, algorithm and provider.
     *
     * @param password Password to use.
     * @param algorithmName Algorithm name.
     * @param provider Provider. Can be null for default provider.
     */
    public ExpirableTokenEncryptor(String password, String algorithmName, Provider provider) {
        //    this.encryptor = new StandardPBEStringEncryptor();
        //  if (provider != null) {
        //     this.encryptor.setProvider(provider);
        //}
        //this.encryptor.setAlgorithm(algorithmName);
        //this.encryptor.setPassword(password);
    }

    /**
     * Constructs token encryptor with given password and PBEWITHSHA256AND128BITAES-CBC-BC algorithm encryption from
     * Bouncy Castle.
     *
     * @param password password to use.
     */
    public ExpirableTokenEncryptor(String password) {
        //   this(password, DEFAULT_ENCRYPTION_ALGORITHM, new BouncyCastleProvider());
    }

    /**
     * Constructs expirable encrypted token with given encryptor.
     *
     * @param encryptor Encryptor to use.
     */
    // public ExpirableTokenEncryptor(StandardPBEStringEncryptor encryptor) {
    // this.encryptor = encryptor;
    //}

    /**
     * Returns encrypted data with added expiration time. When result is decrypted expiration time is checked and if
     * token is expired ExpiredTokenException is thrown (see #decrypt(String)).
     *
     * @param token Token to encrypt.
     * @return Encrypted expirable token as string.
     */
    public String encrypt(ExpirableToken token) {
        if (token == null) {
            throw new IllegalArgumentException("Expecting token to encrypt.");
        }
        if (!token.hasAttributes()) {
            throw new IllegalArgumentException("Token is expected to contain some data.");
        }
        StringBuilder tokenBuilders = new StringBuilder(EXPECTED_TOKEN_LENGTH);
        // Append creation time directly as there is no '/' character in representation.
        tokenBuilders.append(Long.toString(token.getCreated(), Character.MAX_RADIX));
        tokenBuilders.append(TOKEN_SEPARATOR);
        // Append time to live directly as there is no '/' character in representation.
        tokenBuilders.append(Long.toString(token.getTimeToLive(), Character.MAX_RADIX));
        tokenBuilders.append(TOKEN_SEPARATOR);
        appendStringEncodedValue(tokenBuilders, token.getType().toString());
        tokenBuilders.append(TOKEN_SEPARATOR);
        appendStringEncodedValue(tokenBuilders, token.getScope());
        tokenBuilders.append(TOKEN_SEPARATOR);
        for (Map.Entry<String, String> entry : token.getAttributes().entrySet()) {
            appendStringEncodedValue(tokenBuilders, entry.getKey());
            tokenBuilders.append(TOKEN_SEPARATOR);
            appendStringEncodedValue(tokenBuilders, entry.getValue());
            tokenBuilders.append(TOKEN_SEPARATOR);
        }
        if (TOKEN_SEPARATOR == tokenBuilders.charAt(tokenBuilders.length() - 1)) {
            tokenBuilders.setLength(tokenBuilders.length() - 1);
        }
        throw new IllegalStateException("Complete this code");
        //      return encryptor.encrypt(tokenBuilders.toString());
    }

    /**
     * Decrypts given token and returns internal data if it is not expired.
     *
     * @param token Token to decrypt.
     * @return Decrypted token (never returns null if there are no exception).
     * @throws ExpiredTokenException When the token is legal but expired.
     * @throws IllegalTokenException When the token is not legal.
     */
    public ExpirableToken decrypt(String token) throws ExpiredTokenException, IllegalTokenException {
        if (token == null || token.isEmpty()) {
            throw new IllegalTokenException();
        }
        String decrypted = null;
        try {
            if (true) {
                throw new IllegalStateException("Complete this code");
            }
            //decrypted = encryptor.decrypt(token);
        } catch (Exception e) {
            throw new IllegalTokenException(e);
        }
        if (decrypted == null) {
            throw new IllegalTokenException();
        }

        int firstSeparator = decrypted.indexOf(TOKEN_SEPARATOR);
        if (firstSeparator <= 0) {
            throw new IllegalTokenException();
        }
        int secondSeparator = decrypted.indexOf(TOKEN_SEPARATOR, firstSeparator + 1);
        if (secondSeparator <= 0) {
            throw new IllegalTokenException();
        }
        long createdTime;
        long duration;
        try {
            // Extract creation and duration.
            createdTime = Long.parseLong(decrypted.substring(0, firstSeparator), Character.MAX_RADIX);
            duration = Long.parseLong(decrypted.substring(firstSeparator + 1, secondSeparator), Character.MAX_RADIX);
            // Fail early for expired tokens.
            if (createdTime + duration <= getSecondsSinceUnixEpoch()) {
                throw new ExpiredTokenException();
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalTokenException(nfe);
        }

        int tokenIndex = 0;
        ExpirableTokenType type = null;
        String scope = null;
        String key = null;
        String value = null;
        Map<String, String> values = new LinkedHashMap<>();
        StringBuilder currentItem = new StringBuilder();
        for (int i = secondSeparator + 1, l = decrypted.length(); i < l; i++) {
            char c = decrypted.charAt(i);
            if (c == TOKEN_SEPARATOR) {
                if (tokenIndex == 0) {
                    type = ExpirableTokenType.valueOf(decodeValue(currentItem));
                } else if (tokenIndex == 1) {
                    scope = decodeValue(currentItem);
                } else if (tokenIndex % 2 == 0) {
                    key = decodeValue(currentItem);
                } else if (tokenIndex % 2 == 1) {
                    value = decodeValue(currentItem);
                    values.put(key, value);
                    key = null;
                    value = null;
                }
                currentItem.setLength(0);
                tokenIndex++;
            } else {
                currentItem.append(c);
            }
        }
        if (key != null && currentItem.length() > 0) {
            // Key is decoded, current item is not.
            values.put(key, decodeValue(currentItem));
        }
        if (type == null || scope == null) {
            throw new IllegalTokenException();
        }
        return new ExpirableToken(type, scope, values, createdTime, duration);
    }

    /**
     * Returns number of seconds since January 1, 1970 00:00:00 UTC.
     *
     * @return number of seconds since January 1, 1970 00:00:00 UTC.
     */
    public static long getSecondsSinceUnixEpoch() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Appends and escapes the value to given output stream. Internal encoding of '/' and '%' characters is done
     * according to <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>.
     *
     * @param tokenBuilder Output to write the escaped value.
     * @param value Value to escape.
     */
    static void appendStringEncodedValue(StringBuilder tokenBuilder, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (TOKEN_SEPARATOR == c) {
                tokenBuilder.append(TOKEN_SEPARATOR_ESCAPED);
            } else if (PERCENT == c) {
                tokenBuilder.append(PERCENT_ESCAPED);
            } else {
                tokenBuilder.append(c);
            }
        }
    }

    /**
     * Decodes escaped value. Internal decoding of '/' and '%' characters is done according to
     * <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>.
     *
     * @param value Value to decode.
     * @return Decoded value.
     */
    static String decodeValue(CharSequence value) {
        if (value.length() < TOKEN_SEPARATOR_ESCAPED.length()) {
            return value.toString();
        }
        char pprev = 0;
        char prev = 0;
        char unescapedChar = PERCENT;
        StringBuilder output = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char curr = value.charAt(i);
            if (PERCENT == pprev) {
                if (prev == TOKEN_SEPARATOR_ESCAPED.charAt(1) && curr == TOKEN_SEPARATOR_ESCAPED.charAt(2)) {
                    unescapedChar = TOKEN_SEPARATOR;
                } else if (prev == PERCENT_ESCAPED.charAt(1) && curr == PERCENT_ESCAPED.charAt(2)) {
                    unescapedChar = PERCENT;
                }
                // Backtrack and decode.
                output.setCharAt(output.length() - 2, unescapedChar);
                output.setLength(output.length() - 1);
            } else {
                output.append(curr);
            }
            pprev = prev;
            prev = curr;
        }
        return output.toString();
    }
}
