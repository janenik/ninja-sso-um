package web.sso;

import com.google.inject.Injector;
import models.sso.User;
import models.sso.UserConfirmationState;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import org.junit.Before;
import org.junit.Test;
import services.sso.UserService;
import services.sso.token.ExpirableTokenEncryptor;
import web.sso.common.TestEntitiesFactory;
import web.sso.common.WebDriverTest;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link controllers.sso.auth.SignUpVerificationController}.
 */
public class SignUpVerificationTest extends WebDriverTest {

    /**
     * Verification code.
     */
    private static final String VERIFICATION_CODE = "code123";

    /**
     * Verification code time to live.
     */
    private static final long TOKEN_TTL = 100L * 1000L;

    /**
     * Test entities factory.
     */
    private TestEntitiesFactory testEntitiesFactory;

    /**
     * Encryptor.
     */
    private ExpirableTokenEncryptor encryptor;

    /**
     * User service.
     */
    private UserService userService;

    /**
     * User to restore password.
     */
    private User user;

    @Before
    public void setUp() {
        Injector injector = this.getInjector();

        this.userService = injector.getBinding(UserService.class).getProvider().get();
        this.testEntitiesFactory = injector.getBinding(TestEntitiesFactory.class).getProvider().get();
        this.encryptor = injector.getBinding(ExpirableTokenEncryptor.class).getProvider().get();

        // Create test user.
        EntityManager em = entityManagerProvider.get();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        this.user = testEntitiesFactory.createNewUser();
        transaction.commit();
        em.detach(this.user);
    }

    @Test
    public void testUserVerification() throws Exception {
        assertEquals("Created user is expected to be in unverified state.",
                UserConfirmationState.UNCONFIRMED, this.user.getConfirmationState());

        ExpirableToken token = ExpirableToken.newUserToken(
                ExpirableTokenType.SIGNUP_VERIFICATION,
                user.getId(),
                "verificationCode",
                VERIFICATION_CODE,
                TOKEN_TTL);
        goTo(getSignUpVerificationUrl(encryptor.encrypt(token)));

        setFormInputValue("verificationCode", VERIFICATION_CODE);
        click("#verifySignUpSubmit");

        User verifiedUser = userService.getUserByEmailOrUsername(user.getEmail());
        assertEquals("Verified user must be created first.", this.user, verifiedUser);
        assertEquals("User is expected to be verified.",
                UserConfirmationState.CONFIRMED, verifiedUser.getConfirmationState());
    }
}
