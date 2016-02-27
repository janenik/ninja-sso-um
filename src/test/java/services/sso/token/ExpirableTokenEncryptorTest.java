package services.sso.token;

import com.google.common.io.BaseEncoding;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link ExpirableTokenEncryptor}.
 */
public class ExpirableTokenEncryptorTest {

    private static final char[] PASSWORD = "someUnicornPasswordForEncryption1098".toCharArray();

    private ExpirableTokenEncryptor encryptor;

    public ExpirableTokenEncryptorTest() {
        this.encryptor = new ExpirableTokenEncryptor(new AesPasswordBasedEncryptor(PASSWORD));
    }

    @Test
    public void testBasics()
            throws ExpiredTokenException, IllegalTokenException, PasswordBasedEncryptor.EncryptionException {
        ExpirableToken accessToken =
                ExpirableToken.newAccessToken("scope1", "userId", "qwerower1213234", 30L);

        String encrypted = encryptor.encrypt(accessToken);
        ExpirableToken decryptedAccessToken = encryptor.decrypt(encrypted);

        assertEquals("Must be the same tokens.", accessToken, decryptedAccessToken);
    }

    /**
     * Runs tests for rfc7519 specification.
     * @throws UnsupportedEncodingException If UTF-8 is not supported.
     */
    @Test
    public void testBase64UrlFromRFC7519() throws UnsupportedEncodingException {
        String json ="{\"typ\":\"JWT\",\r\n \"alg\":\"HS256\"}";
        byte[] jsonBytes = new byte[] {123, 34, 116, 121, 112, 34, 58, 34, 74, 87, 84, 34, 44, 13, 10, 32,
                34, 97, 108, 103, 34, 58, 34, 72, 83, 50, 53, 54, 34, 125};

        assertArrayEquals(json.getBytes("UTF-8"), jsonBytes);

        String base64Encoded = BaseEncoding.base64Url().encode(jsonBytes);
        assertEquals("eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9", base64Encoded);

        String json2 = "{\"iss\":\"joe\",\r\n" +
                " \"exp\":1300819380,\r\n" +
                " \"http://example.com/is_root\":true}";
        byte[] jsonBytes2 = new byte[] {123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
                32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56,
                48, 44, 13, 10, 32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
                109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
                111, 116, 34, 58, 116, 114, 117, 101, 125};

        assertArrayEquals(json2.getBytes("UTF-8"), jsonBytes2);

        String base64Encoded2 = BaseEncoding.base64Url().encode(jsonBytes2);
        // Replace padding.
        base64Encoded2 = base64Encoded2.replaceAll("=", "");
        String expected2 = "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly" +
                "9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ";
        assertEquals(expected2, base64Encoded2);

        assertEquals(json2,  BaseEncoding.base64Url().decode(expected2));
    }
}
