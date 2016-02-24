package models.sso.token;

import java.io.Serializable;
import java.time.Clock;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Expirable token than holds the token type, scope id, data fields, creation and expiration time.
 */
public final class ExpirableToken implements Serializable {

    /**
     * Creation time of the token. In seconds, since January, 1, 1970, GMT.
     */
    private final long created;
    /**
     * Time to live, in seconds.
     */
    private final long timeToLive;
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
    private final Map<String, String> attributes;

    /**
     * Constructs expirable token.
     *
     * @param type       Token type.
     * @param scope      Scope (like scope name, domain, etc).
     * @param attributes Attributes for the token.
     * @param created    Creation time, in seconds since .
     * @param timeToLive Time
     */
    public ExpirableToken(ExpirableTokenType type, String scope, Map<String, String> attributes, long created, long
            timeToLive) {
        this.created = created;
        this.timeToLive = timeToLive;
        this.type = type;
        this.scope = scope;
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    /**
     * Returns creation time of the token. In seconds, since January, 1, 1970, GMT.
     *
     * @return Creation time.
     */
    public long getCreated() {
        return created;
    }

    /**
     * Return time to live for the token.
     *
     * @return Time to live, in seconds.
     */
    public long getTimeToLive() {
        return timeToLive;
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
        return attributes;
    }

    /**
     * Returns attribute value for given name.
     *
     * @param name Attribute name.
     * @return attribute value for given name or null if there is no such attribute.
     */
    public String getAttributeValue(String name) {
        return attributes.get(name);
    }

    /**
     * Returns attribute value for the given attribute name.
     *
     * @param name         Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value or default value if there is no attribute in token.`
     */
    public String getAttributeValue(String name, String defaultValue) {
        String v = attributes.get(name);
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
        String v = attributes.get(name);
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
        String v = attributes.get(name);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return Double.valueOf(v);
    }

    /**
     * Returns attribute value for the given attribute name as long value.
     *
     * @param name         Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value as long or default if there is no attribute value.
     */
    public long getAttributeAsLong(String name, long defaultValue) {
        String v = attributes.get(name);
        if (v == null || v.isEmpty()) {
            return defaultValue;
        }
        return Long.valueOf(v);
    }

    /**
     * Returns attribute value for the given attribute name as double value.
     *
     * @param name         Attribute name.
     * @param defaultValue Default value.
     * @return Attribute value as double or default if there is no attribute value.
     */
    public double getAttributeAsDouble(String name, double defaultValue) {
        String v = attributes.get(name);
        if (v == null || v.isEmpty()) {
            return defaultValue;
        }
        return Double.valueOf(v);
    }

    /**
     * Returns whether the token has attributes.
     *
     * @return whether the token has attributes.
     */
    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    /**
     * Tests whether the token is expired.
     *
     * @param clock Clock to verify expiration.
     * @return Whether the token is expired.
     */
    public boolean isExpired(Clock clock) {
        return created + timeToLive >= clock.millis() / 1000L;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.created ^ (this.created >>> 32));
        hash = 97 * hash + (int) (this.timeToLive ^ (this.timeToLive >>> 32));
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.scope);
        hash = 97 * hash + Objects.hashCode(this.attributes);
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
        if (this.created != other.created) {
            return false;
        }
        if (this.timeToLive != other.timeToLive) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.scope, other.scope)) {
            return false;
        }
        return Objects.equals(this.attributes, other.attributes);
    }

    /**
     * Static factory for the token.
     *
     * @param type       Token type.
     * @param scope      Token scope.
     * @param attrName   Attribute name.
     * @param attrValue  Attribute value.
     * @param timeToLive Time to live.
     * @return Expiratble token.
     */
    public static ExpirableToken newToken(ExpirableTokenType type, String scope, String attrName, String attrValue,
                                          long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(type).
                setTimeToLive(timeToLive).
                addDataEntry(attrName, attrValue).
                build();
    }

    /**
     * Static factory for the token.
     *
     * @param type       Token type.
     * @param scope      Token scope.
     * @param data       Attributes for the token.
     * @param timeToLive Time to live.
     * @return Expiratble token.
     */
    public static ExpirableToken newToken(ExpirableTokenType type, String scope, Map<String, String> data, long
            timeToLive) {
        return new Builder().
                setScope(scope).
                setType(type).
                setTimeToLive(timeToLive).
                addDataEntries(data).
                build();
    }

    /**
     * Static factory for the access token.
     *
     * @param scope      Token scope.
     * @param attrName   Attribute name.
     * @param attrValue  Attribute value.
     * @param timeToLive Time to live.
     * @return Expiratble token.
     */
    public static ExpirableToken newAccessToken(String scope, String attrName, String attrValue, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.AUTH).
                setTimeToLive(timeToLive).
                addDataEntry(attrName, attrValue).
                build();
    }

    /**
     * Static factory for access token.
     *
     * @param scope      Token scope.
     * @param data       Attributes for the token.
     * @param timeToLive Time to live.
     * @return Expiratble token.
     */
    public static ExpirableToken newAccessToken(String scope, Map<String, String> data, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.AUTH).
                setTimeToLive(timeToLive).
                addDataEntries(data).
                build();
    }

    /**
     * Static factory for refresh token.
     *
     * @param scope      Token scope.
     * @param data       Attributes for the token.
     * @param timeToLive Time to live.
     * @return Expiratble token.
     */
    public static ExpirableToken newRefreshToken(String scope, Map<String, String> data, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.REFRESH).
                setTimeToLive(timeToLive).
                addDataEntries(data).
                build();
    }

    /**
     * Static factory for refresh token.
     *
     * @param scope      Token scope.
     * @param attrName   Attribute name.
     * @param attrValue  Attribute value.
     * @param timeToLive Time to live.
     * @return Expiratble token.
     */
    public static ExpirableToken newRefreshToken(String scope, String attrName, String attrValue, long timeToLive) {
        return new Builder().
                setScope(scope).
                setType(ExpirableTokenType.REFRESH).
                setTimeToLive(timeToLive).
                addDataEntry(attrName, attrValue).
                build();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Expirable token builder.
     */
    public static final class Builder {

        private long created;
        private long timeToLive;
        private ExpirableTokenType type;
        private String scope;
        private Map<String, String> values;

        /**
         * Constructs token builder.
         */
        public Builder() {
            this(Clock.systemUTC());
        }

        /**
         * Constructs token builder.
         *
         * @param clock Clock to use.
         */
        public Builder(Clock clock) {
            created = clock.millis() / 1000L;
        }

        /**
         * Sets creation time in seconds since Unix epoch for the token.
         *
         * @param createdInSeconds Creation time in seconds.
         * @return Current builder.
         */
        public Builder setCreated(long createdInSeconds) {
            this.created = createdInSeconds;
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
         * Sets time to live for the token.
         *
         * @param timeToLive Token duration, in seconds.
         * @return Current token builder.
         */
        public Builder setTimeToLive(long timeToLive) {
            this.timeToLive = timeToLive;
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
         * @param name  Token attribute name.
         * @param value Token attribute value.
         * @return Current token builder.
         */
        public Builder addDataEntry(String name, long value) {
            return addDataEntry(name, Long.toString(value));
        }

        /**
         * Adds an attribute to the token.
         *
         * @param name  Token attribute name.
         * @param value Token attribute value.
         * @return Current token builder.
         */
        public Builder addDataEntry(String name, double value) {
            return addDataEntry(name, Double.toString(value));
        }

        /**
         * Adds an attribute to the token.
         *
         * @param name  Token attribute name.
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
            return new ExpirableToken(type, scope, values, created, timeToLive);
        }
    }
}
