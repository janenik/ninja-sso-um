/**
 * Copyright (C) 2012 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import conf.sso.SsoModule;
import controllers.annotations.InjectedContext;
import controllers.annotations.SecureHtmlHeaders;
import controllers.annotations.SecureHtmlHeadersForAdmin;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.servlet.NinjaServletContext;
import ninja.utils.NinjaProperties;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class Module extends AbstractModule {

    /**
     * Configures application module.
     */
    protected void configure() {
        install(new SsoModule());
        install(new ServletModule());
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
    @InjectedContext
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
     * use frame-bursting script to prevent nested frames attacks.
     *
     * @param properties Ninja properties.
     * @return HTML result with security headers.
     */
    @RequestScoped
    @Provides
    @SecureHtmlHeaders
    Result provideHtmlWithSecureHeaders(NinjaProperties properties) {
        Result result = Results.html()
                .addHeader("X-Content-Type-Options", "nosniff")
                .addHeader("X-Frame-Options", "DENY")
                .addHeader("X-XSS-Protection", "1; mode=block");
        // Application must be under SSL in production: HSTS header for production only.
        if (properties.isProd()) {
            result.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        return result;
    }

    /**
     * Provides HTML result with security headers for application controllers, accessible with
     * {@link models.sso.UserRole#ADMIN} priveleges. Please do not change X-Frame-Options for administration
     * part: it should not be iframed.
     *
     * @param properties Ninja properties.
     * @return HTML result with security headers for admin controllers.
     */
    @RequestScoped
    @Provides
    @SecureHtmlHeadersForAdmin
    Result provideHtmlWithAdminSecureHeaders(NinjaProperties properties) {
        Result result = Results.html()
                .addHeader("X-Content-Type-Options", "nosniff")
                .addHeader("X-Frame-Options", "DENY")
                .addHeader("X-XSS-Protection", "1; mode=block");
        // Application must be under SSL in production: HSTS header for production only.
        if (properties.isProd()) {
            result.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        return result;
    }
}
