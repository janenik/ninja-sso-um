package models.sso.token;

import java.io.Serializable;
import java.time.Clock;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Expirable token than holds the token type, scope id, data fields and exp time.
 */
public final class ExpirableToken implements Serializable {

    /**
     * Expiration time. In milliseconds, since January, 1, 1970, GMT.
     */
    private final long exp;

    /**
     * Token type.
     */
    private final ExpirableTokenType type;

    /**
     * Scope of the token (like project name, domain, etc).
     */
    private final String scope;

    /**
     * Attributes map.
     */
    private final Map<String, String> attr;

    /**
     * Default constructor.
     */
    public ExpirableToken() {
        exp = 0L;
        type = ExpirableTokenType.CUSTOM;
        scope = null;
        attr = Collections.emptyMap();
    }

    /**
     * Constructs expirable token.
     *
     * @param type Token type.
     * @param scope Scope (like scope name, domain, etc).
     * @param attributes Attributes for the token.
     * @param exp Expiration time, in milliseconds since Jan 1, 1970 UTC.
     */
    public ExpirableToken(ExpirableTokenType type, String scope, Map<String, String> attributes, long exp) {
        this.exp = exp;
        this.type = type;
        this.scope = scope;
        this.attr = Collections.unmodifiableMap(attributes);
    }

    /**
     * Returns creation time of the token. In seconds, since January, 1, 1970, GMT.
     *
     * @return Creation time.
     */
    public long getExpires() {
        return exp;
    }

    /**
     * Returns token type.
     *
     * @return Token type.
     */
    public ExpirableTokenType getType() {
        return type;
    }

    /**
     * Returns scope name.
     *
     * @return Project name.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Returns attributes for the expirable token. Unmodifiable map.
     *
     * @return Attributes for the expirable token.
     */
    public Map<String, String> getAttributes() {
        return attr;
    }

    /**
     * Returns attribute value for given name.
     *
     * @param name Attribute name.
     * @return attribute value for given name or null if there is no such attribute.
     */
    public String getAttributeValue(String name) {
        return attr.get(name);
    }

    /**
     * Returns attribute value for the given attribute name.
     *
     * @param name Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value or default value if there is no attribute in token.`
     */
    public String getAttributeValue(String name, String defaultValue) {
        String v = attr.get(name);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    /**
     * Returns attribute value for the given attribute name as long value.
     *
     * @param name Attribute name.
     * @return Attribute value as long.
     */
    public Long getAttributeAsLong(String name) {
        String v = attr.get(name);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return Long.valueOf(v);
    }

    /**
     * Returns attribute value for the given attribute name as double value.
     *
     * @param name Attribute name.
     * @return Attribute value as double.
     */
    public Double getAttributeAsDouble(String name) {
        String v = attr.get(name);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return Double.valueOf(v);
    }

    /**
     * Returns attribute value for the given attribute name as long value.
     *
     * @param name Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value as long or default if there is no attribute value.
     */
    public long getAttributeAsLong(String name, long defaultValue) {
        String v = attr.get(name);
        if (v == null || v.isEmpty()) {
            return defaultValue;
        }
        return Long.valueOf(v);
    }

    /**
     * Returns attribute value for the given attribute name as double value.
     *
     * @param name Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value as double or default if there is no attribute value.
     */
    public double getAttributeAsDouble(String name, double defaultValue) {
        String v = attr.get(name);
        if (v == null || v.isEmpty()) {
            return defaultValue;
        }
        return Double.valueOf(v);
    }

    /**
     * Returns whether the token has attr.
     *
     * @return whether the token has attr.
     */
    public boolean hasAttributes() {
        return !attr.isEmpty();
    }

    /**
     * Tests whether the token is expired.
     *
     * @param clock Clock to verify expiration.
     * @return Whether the token is expired.
     */
    public boolean isExpired(Clock clock) {
        return exp >= clock.millis();
    }

    /**
     * Tests whether the token is expired with system UTC clock.
     *
     * @return Whether the token is expired.
     */
    public boolean isExpired() {
        return exp >= Clock.systemUTC().millis();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.exp ^ (this.exp >>> 32));
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.scope);
        hash = 97 * hash + Objects.hashCode(this.attr);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExpirableToken other = (ExpirableToken) obj;
        if (this.exp != other.exp) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.scope, other.scope)) {
            return false;
        }
        return Objects.equals(this.attr, other.attr);
    }

    /**
     * Static factory for token.
     *
     * @param type Token type.
     * @param scope Token scope.
     * @param attrName Attribute name.
     * @param attrValue Attribute value.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newToken(ExpirableTokenType type, String scope, String attrName, String attrValue,
                                          long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(type).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntry(attrName, attrValue).
                build();
    }

    /**
     * Static factory for token.
     *
     * @param type Token type.
     * @param scope Token scope.
     * @param data Attributes for the token.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newToken(ExpirableTokenType type, String scope, Map<String, String> data,
                                          long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(type).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntries(data).
                build();
    }

    /**
     * Static factory for access token.
     *
     * @param scope Token scope.
     * @param attrName Attribute name.
     * @param attrValue Attribute value.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newAccessToken(String scope, String attrName, String attrValue, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.ACCESS).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntry(attrName, attrValue).
                build();
    }

    /**
     * Static factory for access token.
     *
     * @param scope Token scope.
     * @param data Attributes for the token.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expiratble token.
     */
    public static ExpirableToken newAccessToken(String scope, Map<String, String> data, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.ACCESS).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntries(data).
                build();
    }

    /**
     * Static factory for captcha token.
     *
     * @param attrName Attribute name.
     * @param attrValue Attribute value.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable captcha token.
     */
    public static ExpirableToken newCaptchaToken(String attrName, String attrValue, long timeToLive) {
        return new Builder().
                setScope(null).
                setType(ExpirableTokenType.CAPTCHA).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntry(attrName, attrValue).
                build();
    }

    /**
     * Static factory for refresh token.
     *
     * @param scope Token scope.
     * @param data Attributes for the token.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expiratble token.
     */
    public static ExpirableToken newRefreshToken(String scope, Map<String, String> data, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.REFRESH).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntries(data).
                build();
    }

    /**
     * Static factory for refresh token.
     *
     * @param scope Token scope.
     * @param attrName Attribute name.
     * @param attrValue Attribute value.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newRefreshToken(String scope, String attrName, String attrValue, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.REFRESH).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntry(attrName, attrValue).
                build();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Expirable token builder.
     */
    public static final class Builder {

        private long expires;
        private ExpirableTokenType type;
        private String scope;
        private Map<String, String> values;

        /**
         * Sets expires time in milliseconds since Unix epoch for the token.
         *
         * @param expires Expiration time.
         * @return Current builder.
         */
        public Builder setExpires(long expires) {
            this.expires = expires;
            return this;
        }

        /**
         * Sets scope for the token.
         *
         * @param scope Token scope.
         * @return Current builder.
         */
        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Sets type for the token.
         *
         * @param type Token scope.
         * @return Current builder.
         */
        public Builder setType(ExpirableTokenType type) {
            this.type = type;
            return this;
        }

        /**
         * Adds all data entries (attributes) for the token.
         *
         * @param entries Entries.
         * @return Current token builder.
         */
        public Builder addDataEntries(Map<String, String> entries) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                addDataEntry(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Adds an attribute to the token.
         *
         * @param name Token attribute name.
         * @param value Token attribute value.
         * @return Current token builder.
         */
        public Builder addDataEntry(String name, long value) {
            return addDataEntry(name, Long.toString(value));
        }

        /**
         * Adds an attribute to the token.
         *
         * @param name Token attribute name.
         * @param value Token attribute value.
         * @return Current token builder.
         */
        public Builder addDataEntry(String name, double value) {
            return addDataEntry(name, Double.toString(value));
        }

        /**
         * Adds an attribute to the token.
         *
         * @param name Token attribute name.
         * @param value Token attribute value.
         * @return Current token builder.
         */
        public Builder addDataEntry(String name, String value) {
            if (value == null || name == null) {
                throw new IllegalArgumentException("Both name and value must not be null.");
            }
            if (values == null) {
                values = new LinkedHashMap<>();
            }
            values.put(name, value);
            return this;
        }

        /**
         * Builds the token.
         *
         * @return Expirable token.
         */
        public ExpirableToken build() {
            return new ExpirableToken(type, scope, values, expires);
        }
    }
}
