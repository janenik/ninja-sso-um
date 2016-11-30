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
    private final ExpirableTokenType typ;

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
        typ = ExpirableTokenType.CUSTOM;
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
        this.typ = type;
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
        return typ;
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
        return Collections.unmodifiableMap(attr);
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
        return v == null ? defaultValue : v;
    }

    /**
     * Returns attribute value for the given attribute name as long value.
     *
     * @param name Attribute name.
     * @return Attribute value as long.
     */
    public Long getAttributeAsLong(String name) {
        String v = attr.get(name);
        return v == null ? null : Long.valueOf(v);
    }

    /**
     * Returns attribute value for the given attribute name as double value.
     *
     * @param name Attribute name.
     * @return Attribute value as double.
     */
    public Double getAttributeAsDouble(String name) {
        String v = attr.get(name);
        return v == null ? null : Double.valueOf(v);
    }

    /**
     * Returns attribute value for the given attribute name as long value.
     *
     * @param name Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value as long or default if there is no attribute value.
     */
    public long getAttributeAsLong(String name, long defaultValue) {
        Long v = getAttributeAsLong(name);
        return v == null ? defaultValue : v;
    }

    /**
     * Returns attribute value for the given attribute name as double value.
     *
     * @param name Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value as double or default if there is no attribute value.
     */
    public double getAttributeAsDouble(String name, double defaultValue) {
        Double v = getAttributeAsDouble(name);
        return v == null ? defaultValue : v;
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
        return exp <= clock.millis();
    }

    /**
     * Tests whether the token is expired with system UTC clock.
     *
     * @return Whether the token is expired.
     */
    public boolean isExpired() {
        return exp <= Clock.systemUTC().millis();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.exp ^ (this.exp >>> 32));
        hash = 97 * hash + Objects.hashCode(this.typ);
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
        if (this.typ != other.typ) {
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
    public static ExpirableToken newToken(ExpirableTokenType type, String scope, String attrName,
                                          String attrValue, long timeToLive) {
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
     * Static factory for token.
     *
     * @param type Token type.
     * @param data Attributes for the token.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newToken(ExpirableTokenType type, Map<String, String> data, long timeToLive) {
        return new Builder().
                setType(type).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntries(data).
                build();
    }

    /**
     * Static factory for user token of given type.
     *
     * @param type Token type.
     * @param userId User id.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newUserToken(ExpirableTokenType type, long userId, long timeToLive) {
        return new Builder().
                setType(type).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntry("userId", userId).
                build();
    }

    /**
     * Static factory for user token of given type.
     *
     * @param type Token type.
     * @param userId User id.
     * @param attrName Attribute name.
     * @param attrValue Attribute value.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newUserToken(
            ExpirableTokenType type,
            long userId,
            String attrName,
            String attrValue,
            long timeToLive) {
        return new Builder().
                setType(type).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntry(attrName, attrValue).
                addDataEntry("userId", userId).
                build();
    }

    /**
     * Static factory for user token of given type.
     *
     * @param type Token type.
     * @param userId User id.
     * @param data Attributes data.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newUserToken(
            ExpirableTokenType type,
            long userId,
            Map<String, String> data,
            long timeToLive) {
        return new Builder().
                setType(type).
                setExpires(Clock.systemUTC().millis() + timeToLive).
                addDataEntries(data).
                addDataEntry("userId", userId).
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
     * Static factory for scopeless access token.
     *
     * @param attrName Attribute name.
     * @param attrValue Attribute value.
     * @param timeToLive Time to live, in milliseconds.
     * @return Expirable token.
     */
    public static ExpirableToken newAccessToken(String attrName, String attrValue, long timeToLive) {
        return new Builder().
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

    /**
     * Expirable token builder.
     */
    public static final class Builder {

        /**
         * Expiration time in millis, UTC.
         */
        private long expires;

        /**
         * Token type.
         */
        private ExpirableTokenType type;

        /**
         * Token scope.
         */
        private String scope;

        /**
         * Attributes.
         */
        private Map<String, String> attributes;

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
            if (attributes == null) {
                attributes = new LinkedHashMap<>();
            }
            attributes.put(name, value);
            return this;
        }

        /**
         * Builds the token.
         *
         * @return Expirable token.
         */
        public ExpirableToken build() {
            return new ExpirableToken(type, scope, attributes, expires);
        }

        private static final long serialVersionUID = 1L;
    }
}
