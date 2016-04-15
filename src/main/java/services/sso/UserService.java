package services.sso;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import models.sso.User;
import models.sso.UserEvent;
import models.sso.UserEventType;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * User service.
 */
@Singleton
public class UserService {

    /**
     * Entity manager provider.
     */
    final Provider<EntityManager> entityManagerProvider;

    /**
     * Password service.
     */
    final PasswordService passwordService;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Constructs user service.
     *
     * @param entityManagerProvider Entity manager provider.
     * @param logger Logger.
     */
    @Inject
    public UserService(Provider<EntityManager> entityManagerProvider, PasswordService passwordService, Logger logger) {
        this.entityManagerProvider = entityManagerProvider;
        this.passwordService = passwordService;
        this.logger = logger;
    }

    /**
     * Returns user by given id or null.
     *
     * @param id Id of the user.
     * @return User or null when the user was not found.
     */
    public User get(Long id) {
        return entityManagerProvider.get().find(User.class, id);
    }

    /**
     * Returns user by string that contains username or email.
     *
     * @param emailOrUsername String with email or username.
     * @return User or null if there is no such user in database.
     */
    public User getUserByEmailOrUsername(String emailOrUsername) {
        emailOrUsername = emailOrUsername.toLowerCase().trim();
        if (emailOrUsername.indexOf('@') >= 0) {
            return getByEmail(emailOrUsername);
        }
        return getByUsername(emailOrUsername);
    }

    /**
     * Returns user with given email or null if there is no such user.
     *
     * @param email Email.
     * @return User with given email or null if there is no such user.
     */
    public User getByEmail(String email) {
        Query q = entityManagerProvider.get().createNamedQuery("User.getByEmail");
        q.setParameter("email", email.toLowerCase().trim());
        q.setMaxResults(1);
        try {
            return (User) q.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Returns user with given username or null if there is no such user.
     *
     * @param username Username.
     * @return User with given username or null if there is no such user.
     */
    public User getByUsername(String username) {
        Query q = entityManagerProvider.get().createNamedQuery("User.getByUsername");
        q.setParameter("username", username.toLowerCase().trim());
        q.setMaxResults(1);
        try {
            return (User) q.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Returns user with given phone or null if there is no such user.
     *
     * @param phone Phone.
     * @return User with given phone or null if there is no such user.
     */
    public User getByPhone(String phone) {
        Query q = entityManagerProvider.get().createNamedQuery("User.getByPhone");
        q.setParameter("phone", phone.toLowerCase().trim());
        q.setMaxResults(1);
        try {
            return (User) q.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Saves user into the database.
     *
     * @param user User to save.
     * @return Saved user.
     */
    public User save(User user) {
        entityManagerProvider.get().persist(user);
        return user;
    }

    /**
     * Saves user into the database and returns user parameter (attached entity).
     *
     * @param user User to save.
     * @param remoteIp Remote IP.
     * @return Saved user.
     */
    public User createNew(User user, String password, String remoteIp) {
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));
        entityManagerProvider.get().persist(user);
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setTime(ZonedDateTime.now(ZoneId.of("UTC")));
        userEvent.setType(UserEventType.SIGN_UP);
        userEvent.setIp(remoteIp);
        entityManagerProvider.get().persist(userEvent);
        return user;
    }

    /**
     * Checks if the given user password is valid.
     *
     * @param user User to check.
     * @param password Password to check.
     * @return Whether the given password is a valid user password.
     */
    public boolean isValidPassword(User user, String password) {
        byte[] passwordHash = passwordService.passwordHash(password, user.getPasswordSalt());
        return Arrays.equals(passwordHash, user.getPasswordHash());
    }

    /**
     * Updates sign in time for user in case of successful sign in.
     *
     * @param user User.
     * @param remoteIp Remote IP.
     * @return User event with information about sign in.
     */
    public UserEvent updateSignInTime(User user, String remoteIp) {
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setTime(ZonedDateTime.now(ZoneId.of("UTC")));
        userEvent.setType(UserEventType.SIGN_IN);
        userEvent.setIp(remoteIp);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Updates existing user.
     *
     * @param user User to update.
     */
    public void update(User user) {
        entityManagerProvider.get().persist(user);
    }


    /**
     * Detaches given user from current session.
     *
     * @param user User to detach.
     * @return Detached user (same as argument).
     */
    public User detach(User user) {
        entityManagerProvider.get().detach(user);
        return user;
    }
}
