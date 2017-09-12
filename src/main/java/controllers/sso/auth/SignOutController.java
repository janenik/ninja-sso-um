package controllers.sso.auth;

import controllers.sso.filters.LanguageFilter;
import ninja.FilterWith;
import ninja.Result;
import ninja.metrics.Timed;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Sign out controller that resets cookies for browser based authentication and redirects to continue URL.
 */
@Singleton
@FilterWith({
        LanguageFilter.class
})
public class SignOutController {

    /**
     * Authentication response builder.
     */
    private final Provider<SignInResponseBuilder> responseBuilderProvider;

    /**
     * Controller constructor.
     *
     * @param responseBuilderProvider Authentication response builder provider.
     */
    @Inject
    public SignOutController(Provider<SignInResponseBuilder> responseBuilderProvider) {
        this.responseBuilderProvider = responseBuilderProvider;
    }

    /**
     * Signs out current user by resetting authentication cookie.
     *
     * @return Sign out result.
     */
    @Timed
    public Result signOut() {
        return responseBuilderProvider.get().getSignOutResponse();
    }
}
