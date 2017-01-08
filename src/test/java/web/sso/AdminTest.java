package web.sso;

import com.google.inject.Injector;
import controllers.sso.admin.users.UsersController;
import controllers.sso.auth.SignInController;
import org.junit.Before;
import org.junit.Test;
import services.sso.UserService;

import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Administration web-driver tests.
 */
public class AdminTest extends WebDriverTest {

    /**
     * User service.
     */
    UserService userService;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();
        this.userService = injector.getBinding(UserService.class).getProvider().get();
    }

    @Test
    public void testUnauthorizedUserIsRedirectedToSignIn() throws URISyntaxException {
        goTo(getAdminUsersUrl());

        String url = webDriver.getCurrentUrl();
        String adminUsersRelativeUrl = router.getReverseRoute(UsersController.class, "users");
        String signInRelativeUrl = router.getReverseRoute(SignInController.class, "signInGet");
        String continueParameter = extractParameters(url).get("continue");
        assertTrue("Must be redirected to sign in", url.contains(signInRelativeUrl));
        assertNotNull("Sign in URL must contain continue", continueParameter);
        assertTrue("Continue URL must be admin user URL", continueParameter.contains(adminUsersRelativeUrl));
    }
}
