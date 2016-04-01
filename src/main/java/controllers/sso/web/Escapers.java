package controllers.sso.web;

import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;
import com.google.common.net.UrlEscapers;

import java.net.URLDecoder;

/**
 * Encoding utils.
 */
public final class Escapers {

    /**
     * More efficient encoding (acts like encodeURIComponent).
     */
    private static final Escaper percentEscaper = new PercentEscaper("-_.*", false);

    /**
     * Encodes given string characters using a UTF-8 based percent
     * encoding scheme. See {@link com.google.common.net.PercentEscaper} documentation.
     * Acts like {@link UrlEscapers#urlFormParameterEscaper()} but space is escaped with '%20'.
     *
     * @param str String to escape.
     * @return URL encoded string.
     */
    public static String encodePercent(String str) {
        if (str == null) {
            return "";
        }
        return percentEscaper.escape(str);
    }

    /**
     * Decodes given string that contains some percent encoded values.
     *
     * @param str String to URL decode.
     * @return URL decode string.
     */
    public static String decodePercent(String str) {
        if (str == null) {
            return "";
        }
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (java.io.UnsupportedEncodingException wow) {
            throw new RuntimeException(wow.getMessage(), wow);
        }
    }

    private Escapers() {
    }
}
