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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import controllers.sso.filters.HitsPerIpCheckFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import services.sso.CaptchaTokenService;
import services.sso.limits.IPCounterService;

@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        HitsPerIpCheckFilter.class
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
     * Method to put initial data in the db.
     *
     * @return Result.
     */
    public Result setup() {
        return Results.ok();
    }

    /**
     * Renders index page.
     *
     * @return Index page.
     */
    public Result index(Context context) {
        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        return Results.html()
                .render("lang", context.getAttribute(LanguageFilter.LANG))
                .render("remoteIp", ip)
                .render("captchaToken", captchaTokenService.newCaptchaToken())
                .render("ipHits", ipCounterService.getIpHits(ip))
                .render("ipHitsExceeded", context.getAttribute(HitsPerIpCheckFilter.HITS_PER_IP_LIMIT_EXCEEDED));
    }
}
