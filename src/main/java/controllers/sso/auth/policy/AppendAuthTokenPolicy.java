package controllers.sso.auth.policy;

/**
 * Defines a way to append an auth token to a client response.
 */
public enum AppendAuthTokenPolicy {

    /**
     * Authentication token is appended as a cookie with the name defined by property
     * "application.sso.device.auth.policy.append.parameter" in {@link ninja.utils.NinjaProperties}.
     * Makes sense for {@link DeviceAuthPolicy#AUTO} and {@link DeviceAuthPolicy#BROWSER}
     */
    COOKIE,

    /**
     * Authentication token is placed into URL parameter with the name defined by property
     * "application.sso.device.auth.policy.append.parameter" in {@link ninja.utils.NinjaProperties}.
     */
    URL_PARAM,

    /**
     * Authentication token is placed into URL hash parameter with the name defined by property
     * "application.sso.device.auth.policy.append.parameter" in {@link ninja.utils.NinjaProperties}.
     */
    URL_HASH;
}
