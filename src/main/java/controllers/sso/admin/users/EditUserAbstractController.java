package controllers.sso.admin.users;

import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.web.UrlBuilder;
import models.sso.User;
import ninja.Context;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Provider;

/**
 * Edit user data abstract controller with common method and services.
 */
public abstract class EditUserAbstractController {

    /**
     * User service.
     */
    protected final UserService userService;

    /**
     * User event service.
     */
    protected final UserEventService userEventService;

    /**
     * URL builder provider for controller. Instance per request.
     */
    protected final Provider<UrlBuilder> urlBuilderProvider;

    /**
     * Constructs abstract controller.
     *
     * @param userService User service.
     * @param userEventService User event service.
     * @param urlBuilderProvider URL builder provider.
     */
    public EditUserAbstractController(UserService userService, UserEventService userEventService,
                                      Provider<UrlBuilder> urlBuilderProvider) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.urlBuilderProvider = urlBuilderProvider;
    }

    /**
     * Log access event.
     *
     * @param user User who's information is accessed.
     * @param context Context.
     */
    protected void logAccess(User user, Context context) {
        User loggedInUser = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        String remoteIp = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        String currentUrl = urlBuilderProvider.get().getCurrentUrl();
        userEventService.onUserDataAccess(loggedInUser, user, currentUrl, remoteIp, context.getHeaders());
    }

    /**
     * Log update event.
     *
     * @param user User who's information is updated.
     * @param context Context.
     */
    protected void logUpdate(User user, Context context) {
        User loggedInUser = userService.get((Long) context.getAttribute(AuthenticationFilter.USER_ID));
        String remoteIp = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        String currentUrl = urlBuilderProvider.get().getCurrentUrl();
        userEventService.onUserDataUpdate(loggedInUser, user, currentUrl, remoteIp, context.getHeaders());
    }
}
