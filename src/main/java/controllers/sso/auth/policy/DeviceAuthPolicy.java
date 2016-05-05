package controllers.sso.auth.policy;

/**
 * Enumeration of authentication policies supported by application. Policy defines the way application
 * sends authentication tokens to a client. Works with {@link controllers.sso.filters.DeviceTypeFilter}.
 */
public enum DeviceAuthPolicy {

    /**
     * Policy when an access token is appended to special URL redirect with application defined schema for mobile
     * devices.
     */
    MOBILE,

    /**
     * Policy for desktop and mobile browsers: plain HTTP redirect.
     */
    BROWSER,

    /**
     * Auto policy that uses HTTP redirect for browsers and special URL redirect with application defined
     * schema for mobile devices.
     */
    AUTO;
}
