package services.sso.token;

import com.google.common.io.BaseEncoding;
import controllers.sso.web.Escapers;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenEncryptorException;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test for {@link ExpirableTokenEncryptor}.
 */
public class ExpirableTokenEncryptorTest {

    static final char[] PASSWORD = "someUnicornPasswordForEncryption1098123129380980980980293909==+=<>09".toCharArray();
    static final Logger logger = LoggerFactory.getLogger(AesPasswordBasedEncryptorTest.class);

    /**
     * Encryptor.
     */
    ExpirableTokenEncryptor encryptor;

    /**
     * Contructs test.
     */
    public ExpirableTokenEncryptorTest() {
        this.encryptor = new ExpirableTokenEncryptor(new AesPasswordBasedEncryptor(PASSWORD));
    }

    @Test
    public void testBasic()
            throws ExpiredTokenException, IllegalTokenException, ExpirableTokenEncryptorException {
        ExpirableToken accessToken = ExpirableToken.newAccessToken("scope1", "userId", "12345678901234567890", 30_000L);

        String encrypted = encryptor.encrypt(accessToken);
        ExpirableToken decryptedAccessToken = encryptor.decrypt(encrypted);

        assertEquals("Must be the same tokens.", accessToken, decryptedAccessToken);
        assertFalse("Ends with/contains padding character.", encrypted.contains("="));
        assertEquals("There is no need in URL escaping", encrypted, Escapers.encodePercent(encrypted));
        logTestInformation(encrypted, decryptedAccessToken);
    }

    @Test
    public void testBasic_nullScope()
            throws ExpiredTokenException, IllegalTokenException, ExpirableTokenEncryptorException {
        ExpirableToken accessToken = ExpirableToken.newAccessToken(null, "userId", "12345678901234567890", 30_000L);

        String encrypted = encryptor.encrypt(accessToken);
        ExpirableToken decryptedAccessToken = encryptor.decrypt(encrypted);

        assertEquals("Must be the same tokens.", accessToken, decryptedAccessToken);
        assertFalse("Ends with/contains padding character.", encrypted.contains("="));
        assertEquals("There is no need in URL escaping", encrypted, Escapers.encodePercent(encrypted));
        logTestInformation(encrypted, decryptedAccessToken);
    }

    @Test
    public void testCaptcha()
            throws ExpiredTokenException, IllegalTokenException, ExpirableTokenEncryptorException {
        ExpirableToken accessToken = ExpirableToken.newCaptchaToken("captcha", "WORLD768", 30_000L);

        String encrypted = encryptor.encrypt(accessToken);
        ExpirableToken decryptedAccessToken = encryptor.decrypt(encrypted);

        assertEquals("Must be the same tokens.", accessToken, decryptedAccessToken);
        assertFalse("Ends with/contains padding character.", encrypted.contains("="));
        assertEquals("There is no need in URL escaping", encrypted, Escapers.encodePercent(encrypted));
        logTestInformation(encrypted, decryptedAccessToken);
    }

    @Test
    public void testBiggerToken()
            throws ExpiredTokenException, IllegalTokenException, ExpirableTokenEncryptorException {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("origin", "http://example.org/");
        attributes.put("sub", "12345678901234567890");
        attributes.put("name", "Christine Luisetta Castelli-Johns");
        attributes.put("role", "admin");
        attributes.put("location", "-22.3123123123,-23.124234234234");
        attributes.put("addressLine1", "707 Continental Circle");
        attributes.put("addressLine2", "841");
        attributes.put("city", "Mountain View");
        attributes.put("state", "California");
        attributes.put("country", "United States");
        attributes.put("flag1", "value1");
        attributes.put("flag2", "very_very_long_value_21234567890");

        ExpirableToken accessToken = ExpirableToken.newAccessToken("scope365", attributes, 30_000L);

        String encrypted = encryptor.encrypt(accessToken);
        ExpirableToken decryptedAccessToken = encryptor.decrypt(encrypted);

        assertEquals("Must be the same tokens.", accessToken, decryptedAccessToken);
        assertFalse("Ends with/contains padding character.", encrypted.contains("="));
        assertEquals("There is no need in URL escaping", encrypted, Escapers.encodePercent(encrypted));
        logTestInformation(encrypted, decryptedAccessToken);
    }

    @Test
    public void benchmarkEncryptedTokenSize()
            throws ExpiredTokenException, IllegalTokenException, ExpirableTokenEncryptorException {
        String userId = "";
        for (int i = 1; i < 100; i++) {
            userId += i;
            ExpirableToken accessToken = ExpirableToken.newAccessToken("u", userId, 30_000L);

            String encrypted = encryptor.encrypt(accessToken);
            ExpirableToken decryptedAccessToken = encryptor.decrypt(encrypted);

            assertEquals("Must be the same tokens.", accessToken, decryptedAccessToken);
            assertFalse("Ends with/contains padding character.", encrypted.contains("="));
            assertEquals("There is no need in URL escaping", encrypted, Escapers.encodePercent(encrypted));
        }
    }

    /**
     * Runs tests for rfc7519 specification.
     *
     * @throws UnsupportedEncodingException when UTF-8 is not supported.
     */
    @Test
    public void testBase64UrlFromRFC7519() throws UnsupportedEncodingException {
        String json = "{\"typ\":\"JWT\",\r\n \"alg\":\"HS256\"}";
        byte[] jsonBytes = new byte[]{123, 34, 116, 121, 112, 34, 58, 34, 74, 87, 84, 34, 44, 13, 10, 32,
                34, 97, 108, 103, 34, 58, 34, 72, 83, 50, 53, 54, 34, 125};

        assertArrayEquals(json.getBytes("UTF-8"), jsonBytes);

        String base64Encoded = BaseEncoding.base64Url().encode(jsonBytes);
        assertEquals("eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9", base64Encoded);

        String json2 = "{\"iss\":\"joe\",\r\n" +
                " \"exp\":1300819380,\r\n" +
                " \"http://example.com/is_root\":true}";
        byte[] jsonBytes2 = new byte[]{123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
                32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56,
                48, 44, 13, 10, 32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
                109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
                111, 116, 34, 58, 116, 114, 117, 101, 125};

        assertArrayEquals(json2.getBytes("UTF-8"), jsonBytes2);

        String base64Encoded2 = BaseEncoding.base64Url().omitPadding().encode(jsonBytes2);
        // Replace padding.
        base64Encoded2 = base64Encoded2.replaceAll("=", "");
        String expected2 = "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly" +
                "9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ";
        assertEquals(expected2, base64Encoded2);

        assertArrayEquals(jsonBytes2, BaseEncoding.base64Url().decode(expected2));
    }

    /**
     * Logs test information about token JSON size and encrypted size.
     *
     * @param encrypted Encrypted token.
     * @param expirableToken Expirable token.
     */
    private void logTestInformation(String encrypted, ExpirableToken expirableToken) {
        logger.info("Encrypted: {}, JSON: ~{} (bytes) ({}/{})",
                encrypted.getBytes(StandardCharsets.UTF_8).length,
                estimateTokenJsonSize(expirableToken), expirableToken.getScope(), expirableToken.getType());
    }

    /**
     * Estimates JSON size of the token.
     *
     * @param expirableToken Expirable token.
     * @return Estimated JSON size.
     */
    private static int estimateTokenJsonSize(ExpirableToken expirableToken) {
        Map<String, String> attr = expirableToken.getAttributes();
        int unencryptedTokenJsonLengthEstimation =
                attr.toString().getBytes(StandardCharsets.UTF_8).length + (attr.size() + 4) * 6
                        + expirableToken.getType().toString().length() + 8;
        if (expirableToken.getScope() != null) {
            unencryptedTokenJsonLengthEstimation += expirableToken.getScope().length() + 8;
        }
        return unencryptedTokenJsonLengthEstimation;
    }

}
