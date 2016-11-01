/**
 * Copyright (C) 2013 the original author or authors.
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

package controllers;

import controllers.annotations.SecureHtmlHeaders;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import services.sso.CaptchaTokenService;
import services.sso.UserService;
import services.sso.limits.IPCounterService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class,
        AuthenticationFilter.class
})
public class ApplicationController {

    /**
     * IP counter service.
     */
    @Inject
    IPCounterService ipCounterService;

    /**
     * Captcha token service.
     */
    @Inject
    CaptchaTokenService captchaTokenService;

    /**
     * User service.
     */
    @Inject
    UserService userService;

    /**
     * Provider for HTML result with secure headers. Contains context as well.
     */
    @Inject
    @SecureHtmlHeaders
    Provider<Result> htmlWithSecureHeadersProvider;

    /**
     * Renders index page.
     *
     * @return Index page.
     */
    public Result index(Context context) {
        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        Long userId = (Long) context.getAttribute(AuthenticationFilter.USER_ID);
        User user = null;
        if (userId != null) {
            user = userService.get(userId);
        }
        return htmlWithSecureHeadersProvider.get()
                .render("context", context)
                .render("method", context.getMethod())
                .render("userEntity", user)
                .render("lang", context.getAttribute(LanguageFilter.LANG))
                .render("remoteIp", ip)
                .render("captchaToken", captchaTokenService.newCaptchaToken())
                .render("ipHits", ipCounterService.getIpHits(ip))
                .render("ipHitsExceeded", context.getAttribute(HitsPerIpCheckFilter.HITS_PER_IP_LIMIT_EXCEEDED));
    }
}
