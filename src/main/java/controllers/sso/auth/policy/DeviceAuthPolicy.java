package controllers.sso.auth.policy;

/**
 * Enumeration of authentication policies supported by application. Policy defines the way SSO web application
 * sends authentication tokens to a client. Works with {@link controllers.sso.filters.DeviceTypeFilter}.
 */
public enum DeviceAuthPolicy {

    /**
     * Policy when an access token is appended to special URL redirect with application defined schema. Standalone
     * application behavior.
     */
    APPLICATION,

    /**
     * Policy for pointer and touchscreen browsers: plain HTTP redirect. Browser only behaviour.
     */
    BROWSER,

    /**
     * Auto policy that uses HTTP redirect for pointer devices and special URL redirect with application defined
     * schema for touchscreen devices. Flexible device type dependent behavior.
     */
    AUTO;
}
