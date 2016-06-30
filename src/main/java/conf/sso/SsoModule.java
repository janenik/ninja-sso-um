package conf.sso;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import controllers.sso.auth.policy.AppendAuthTokenPolicy;
import controllers.sso.auth.policy.DeviceAuthPolicy;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.servlet.NinjaServletContext;
import ninja.utils.NinjaProperties;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import services.sso.token.AesPasswordBasedEncryptor;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * SSO module.
 */
public class SsoModule extends AbstractModule {

    @Override
    protected void configure() {
        // Configure password and expirable token encryptors.
        bind(ExpirableTokenEncryptor.class);

        // Configure Dozer.
        bind(Mapper.class).toInstance(new DozerBeanMapper());

        // Configure start up actions.
        bind(SsoStartupActions.class);
    }

    /**
     * Provides password based encryptor.
     *
     * @param properties Properties.
     * @return Password based encryptor.
     */
    @Provides
    PasswordBasedEncryptor providesPasswordBasedEncryptor(NinjaProperties properties) {
        char[] key = properties.getOrDie("application.sso.tokens.encryption.aes.key").toCharArray();
        short strength = Short.valueOf(
                properties.getWithDefault("application.sso.tokens.encryption.aes.strength", "128"));
        return new AesPasswordBasedEncryptor(key, strength);
    }

    /**
     * Provides a list with allowed continue URL prefixes. Includes ${application.baseUrl}.
     *
     * @param properties Application properties.
     * @return List of allowed continue URL prefixes, including self.
     */
    @Provides
    @Named("allowedContinueUrls")
    @Singleton
    List<String> provideAllowedContinueUrls(NinjaProperties properties) {
        String baseUrl = properties.get("application.baseUrl");
        String[] urls = properties.getStringArray("application.sso.allowedContinueUrls");
        if (urls == null) {
            urls = new String[0];
        }
        List<String> allowedRedirects = Lists.newArrayListWithCapacity(urls.length + 1);
        allowedRedirects.add(baseUrl);
        for (String url : urls) {
            url = Strings.nullToEmpty(url).trim();
            if (!url.isEmpty()) {
                allowedRedirects.add(url);
            }
        }
        return Collections.unmodifiableList(allowedRedirects);
    }

    /**
     * Provides Ninja context.
     *
     * @param context Context.
     * @param servletContext Servlet context.
     * @param servletRequest Servlet request.
     * @param servletResponse Servlet response.
     * @return Ninja context.
     */
    @RequestScoped
    @Provides
    @Named("ssoContext")
    Context provideContext(NinjaServletContext context,
                           ServletContext servletContext,
                           HttpServletRequest servletRequest,
                           HttpServletResponse servletResponse) {
        context.init(servletContext, servletRequest, servletResponse);
        return context;
    }

    /**
     * Provides HTML result with security headers for application controllers, accessible with
     * {@link models.sso.UserRole#USER} and
     * {@link models.sso.UserRole#MODERATOR} priveleges. May be simplified to SAMEORIGIN if needed but make sure to
     * use framebursting script to prevent nested frames attacks.
     *
     * @param properties Ninja properties.
     * @return HTML result with security headers.
     */
    @RequestScoped
    @Provides
    @Named("htmlSecureHeaders")
    Result provideHtmlWithSecureHeaders(NinjaProperties properties) {
        Result result = Results.html()
                .addHeader("X-Content-Type-Options", "nosniff")
                .addHeader("X-Frame-Options", "DENY")
                .addHeader("X-XSS-Protection", "1; mode=block");
        // Application must be under SSL, so sending HSTS header for production.
        if (properties.isProd()) {
            result.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        return result;
    }

    /**
     * Provides HTML result with security headers for application controllers, accessible with
     * {@link models.sso.UserRole#ADMIN} priveleges.
     *
     * @param properties Ninja properties.
     * @return HTML result with security headers for admin controllers.
     */
    @RequestScoped
    @Provides
    @Named("htmlAdminSecureHeaders")
    Result provideHtmlWithAdminSecureHeaders(NinjaProperties properties) {
        Result result = Results.html()
                .addHeader("X-Content-Type-Options", "nosniff")
                .addHeader("X-Frame-Options", "DENY")
                .addHeader("X-XSS-Protection", "1; mode=block");
        // Application must be under SSL, so sending HSTS header for production.
        if (properties.isProd()) {
            result.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        return result;
    }

    /**
     * Provides device authorization policy (how to pass authentication tokens to browser and mobile devices).
     *
     * @param properties Properties.
     * @param logger Logger.
     * @return Device authentication policy.
     */
    @Provides
    @Singleton
    DeviceAuthPolicy provideDeviceAuthPolicy(NinjaProperties properties, Logger logger) {
        String property = "application.sso.device.auth.policy";
        String policy = properties.getWithDefault(property, DeviceAuthPolicy.AUTO.toString());
        try {
            return DeviceAuthPolicy.valueOf(policy);
        } catch (Exception e) {
            logger.error("Error while parsing " + property + ": " + policy, e);
            return DeviceAuthPolicy.AUTO;
        }
    }

    /**
     * Provides browser append token policy.
     *
     * @param properties Properties.
     * @param logger Logger.
     * @return Desktop append token policy.
     */
    @Provides
    @Named("browser")
    @Singleton
    AppendAuthTokenPolicy provideDesktopAppendAuthTokenPolicy(NinjaProperties properties, Logger logger) {
        String property = "application.sso.device.auth.policy.append.browser";
        String policy = properties.getWithDefault(property, AppendAuthTokenPolicy.COOKIE.toString());
        try {
            return AppendAuthTokenPolicy.valueOf(policy);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("Error while parsing " + property + ": " + policy, e);
            return AppendAuthTokenPolicy.COOKIE;
        }
    }


    /**
     * Provides standalone application append token policy.
     *
     * @param properties Properties.
     * @param logger Logger.
     * @return Application append token policy.
     */
    @Provides
    @Named("application")
    @Singleton
    AppendAuthTokenPolicy provideMobileAppendAuthTokenPolicy(NinjaProperties properties, Logger logger) {
        String property = "application.sso.device.auth.policy.append.application";
        String policy = properties.getWithDefault(property, AppendAuthTokenPolicy.URL_PARAM.toString());
        try {
            return AppendAuthTokenPolicy.valueOf(policy);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("Error while parsing " + property + ": " + policy, e);
            return AppendAuthTokenPolicy.URL_PARAM;
        }
    }
}