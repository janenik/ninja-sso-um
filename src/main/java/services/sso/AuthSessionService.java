package services.sso;

import models.sso.AuthSession;
import models.sso.User;
import models.sso.token.ExpirableToken;
import models.sso.token.ExpirableTokenType;
import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.scheduler.Schedule;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Login token service.
 */
@Singleton
public class AuthSessionService {

    public interface  ExpirableTokenEncryptor {
        String encrypt(ExpirableToken expirableToken);
        ExpirableToken decrypt(String token) throws ExpiredTokenException, IllegalTokenException;
    }

    public static final String ACCESS_TOKEN_TTL_PARAM = "application.auth.token.access.ttl";
    public static final String REFRESH_TOKEN_TTL_PARAM = "application.auth.token.refresh.ttl";
    public static final String REFRESH_UNCONFIRMED_TOKEN_TTL_PARAM = "application.auth.token.refresh.unconfirmed.ttl";
    public static final String CHECK_CONFIRMED_EMAIL_PARAM = "application.auth.token.checkConfirmedEmail";
    public static final String USER_ID_TOKEN_PARAMETER = "userId";

    // Fields.
    final UserService userService;
    final ExpirableTokenEncryptor sessionTokenEncryptor;
    final Provider<EntityManager> entityManager;
    final Clock clock;
    final NinjaProperties properties;
    final Logger logger;
    final int accessTokenTtl;
    final int refreshTokenTtl;
    final int refreshTokenUnconfirmedTtl;
    final boolean checkForConfirmedEmail;

    @Inject
    public AuthSessionService(
            UserService userService,
            @Named("sessionTokenEncryptor") ExpirableTokenEncryptor sessionTokenEncryptor,
            Provider<EntityManager> entityManager,
            Clock clock,
            NinjaProperties properties,
            Logger logger) {
        this.userService = userService;
        this.sessionTokenEncryptor = sessionTokenEncryptor;
        this.entityManager = entityManager;
        this.clock = clock;
        this.properties = properties;
        this.logger = logger;
        this.accessTokenTtl = properties.getIntegerWithDefault(ACCESS_TOKEN_TTL_PARAM, 3600); // 1 hour
        this.refreshTokenTtl = properties.getIntegerWithDefault(REFRESH_TOKEN_TTL_PARAM, 30 * 24 * 3600); // 1 month
        this.refreshTokenUnconfirmedTtl
                = properties.getIntegerWithDefault(REFRESH_UNCONFIRMED_TOKEN_TTL_PARAM, 3 * 24 * 3600); // 3 days
        this.checkForConfirmedEmail = properties.getBooleanOrDie(CHECK_CONFIRMED_EMAIL_PARAM);
    }

    /**
     * Creates new session by refresh token.
     *
     * @param refreshTokenAsString Refresh token as string.
     * @return Login session.
     * @throws ExpiredTokenException When refresh token is expired or user is missing.
     * @throws IllegalTokenException When illegal token is given.
     */
    public AuthSession newSession(String refreshTokenAsString) throws ExpiredTokenException, IllegalTokenException {
        ExpirableToken refreshToken = sessionTokenEncryptor.decrypt(refreshTokenAsString);
        Long userId = refreshToken.getAttributeAsLong(USER_ID_TOKEN_PARAMETER);
        User user = userService.get(userId);
        if (user == null) {
            throw new ExpiredTokenException();
        }
        // Access token.
        ExpirableToken accessToken
                = ExpirableToken.newAccessToken("project", USER_ID_TOKEN_PARAMETER, userId.toString(),
                this.accessTokenTtl);
        String accessTokenAsString = sessionTokenEncryptor.encrypt(accessToken);
        // Entity.
        AuthSession session = new AuthSession();
        session.setAccessToken(accessTokenAsString);
        session.setUser(user);
        session.setRefreshToken(refreshTokenAsString);
        entityManager.get().persist(session);
        return session;
    }

    /**
     * Creates new login session by refresh token (creates new access token for that).
     *
     * @param refreshToken Refresh token
     * @return New login session with given refresh token and new access token.
     * @throws ExpiredTokenException                                  When refresh token is expired.
     * @throws IllegalTokenException                                  When illegal token is given.
     * @throws ProbationPeriodWihthoutEmailConfirmationEndedException When probation period is over and user is forced
     *                                                                to confirm his email.
     */
    public AuthSession newAuthSessionByRefreshToken(String refreshToken)
            throws ExpiredTokenException, IllegalTokenException,
            ProbationPeriodWihthoutEmailConfirmationEndedException {
        ExpirableToken token = sessionTokenEncryptor.decrypt(refreshToken);
        if (!ExpirableTokenType.REFRESH.equals(token.getType())) {
            throw new IllegalTokenException();
        }
        String userId = token.getAttributeValue(USER_ID_TOKEN_PARAMETER);
        return newSession("project", refreshToken, userService.get(Long.valueOf(userId)));
    }

    /**
     * Creates login session with access/refresh token with project and user. For sign up only.
     *
     * @param projectAsString Project.
     * @param user            User.
     * @return Login session.
     */
    public AuthSession newSession(String projectAsString, User user) {
        try {
            return newSession(projectAsString, null, user);
        } catch (AuthSessionService.ProbationPeriodWihthoutEmailConfirmationEndedException ex) {
            String emailConfirmationMessage = "Email confirmation is not expected at this step.";
            logger.error(emailConfirmationMessage, ex);
            throw new RuntimeException(emailConfirmationMessage, ex);
        }
    }

    /**
     * Creates login session with access/refresh token with project, refresh token and user.
     *
     * @param projectAsString      Project.
     * @param refreshTokenAsString Refresh token as string. If null is given the new refresh token is created.
     * @param user                 User to authenticate.
     * @return Login session.
     * @throws ProbationPeriodWihthoutEmailConfirmationEndedException When probation period is over and user is forced
     *                                                                to confirm his email.
     */
    public AuthSession newSession(String projectAsString, String refreshTokenAsString, User user)
            throws ProbationPeriodWihthoutEmailConfirmationEndedException {
        if (isProbationPeriodEnded(user)) {
            throw new ProbationPeriodWihthoutEmailConfirmationEndedException();
        }
        long userId = user.getId();
        // Access token.
        ExpirableToken accessToken = ExpirableToken.newAccessToken("project", USER_ID_TOKEN_PARAMETER,
                Long.toString(userId), this.accessTokenTtl);
        String accessTokenAsString = sessionTokenEncryptor.encrypt(accessToken);
        // Refresh token.
        if (refreshTokenAsString == null) {
            ExpirableToken refreshToken = ExpirableToken.newRefreshToken("project", USER_ID_TOKEN_PARAMETER,
                    Long.toString(userId), user.isConfirmed() ? this.refreshTokenTtl : this.refreshTokenUnconfirmedTtl);
            refreshTokenAsString = sessionTokenEncryptor.encrypt(refreshToken);
        }
        // Entity.
        AuthSession session = new AuthSession();
        session.setAccessToken(accessTokenAsString);
        session.setUser(user);
        session.setRefreshToken(refreshTokenAsString);
        entityManager.get().persist(session);
        return session;
    }

    /**
     * Checks if the user hasn't confirmed his email in 3 days (unconfirmed refresh token ttl).
     *
     * @param user User to check.
     * @return Whether user hasn't confirmed email yet and probation is period ended.
     */
    public boolean isProbationPeriodEnded(User user) {
        return !user.isConfirmed()
                && getTimeInSeconds(user.getCreated()) + this.refreshTokenUnconfirmedTtl < getCurrentTimeInSeconds();
    }

    /**
     * Returns login session by access token.
     *
     * @param accessToken Access token.
     * @return Access token.
     */
    public AuthSession get(String accessToken) {
        return entityManager.get().find(AuthSession.class, accessToken);
    }

    /**
     * Returns user by checking given access token for type and project. In case of correct project and type (token must
     * be access token) returns the user associated with this token.
     *
     * @param accessToken Access token.
     * @return User associated with the token.
     * @throws ExpiredTokenException When the token is expired.
     * @throws IllegalTokenException In case if incorrect token type, project or user data.
     */
    public User getUser(String accessToken) throws ExpiredTokenException, IllegalTokenException {
        try {
            ExpirableToken tok = sessionTokenEncryptor.decrypt(accessToken);
            if (ExpirableTokenType.AUTH.equals(tok.getType())) {
                return userService.get(tok.getAttributeAsLong(USER_ID_TOKEN_PARAMETER));
            } else {
                throw new IllegalTokenException();
            }
        } catch (NumberFormatException ex) {
            logger.warn("Unable to extract user from token: " + accessToken, ex);
            throw new IllegalTokenException();
        }
    }

    /**
     * Checks if the session is expired.
     *
     * @param session Session to check.
     * @return Whether the session is expired.
     */
    public boolean isSessionExpired(AuthSession session) {
        return getCurrentTimeInSeconds() - this.accessTokenTtl > getTimeInSeconds(session.getCreated());
    }

    /**
     * Deletes expired sessions from database. Scheduled by framework.
     */
    @Schedule(delay = 600, initialDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void deleteExpiredSessions() {
        logger.warn("Deleting expired sessions...");
        EntityManager em = entityManager.get();
        EntityTransaction t = em.getTransaction();
        try {
            t.begin();
            Query q = em.createNamedQuery("AuthSession.deleteExpired");
            q.setParameter("created", getTimeFromSeconds(getCurrentTimeInSeconds() - this.accessTokenTtl));
            q.executeUpdate();
            t.commit();
        } catch (Exception e) {
            t.rollback();
            logger.error("Error while deleting.", e);
        }
        logger.warn("Done deleting expired sessions.");
    }

    long getCurrentTimeInSeconds() {
        return clock.millis() / 1000L;
    }

    static long getTimeInSeconds(ZonedDateTime time) {
        return time.toInstant().getEpochSecond();
    }

    static ZonedDateTime getTimeFromSeconds(long seconds) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("UTC"));
    }

    /**
     * Exception is thrown when probation period is finished and user is forced to confirm his email.
     */
    public static class ProbationPeriodWihthoutEmailConfirmationEndedException extends Exception {
    }
}
