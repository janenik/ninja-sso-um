package controllers.sso.admin.users;

import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import ninja.FilterWith;

import javax.inject.Singleton;

/**
 * View access log controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class ViewAccessLogController {
}
