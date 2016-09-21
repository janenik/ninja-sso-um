package services.sso;

import models.sso.User;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Arrays;

/**
 * User service.
 */
@Singleton
public class UserService implements Paginatable<User> {

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
     * Saves user into the database and returns attached entity.
     *
     * @param user User to save.
     * @return Created user (as as argument, attached instance).
     */
    public User createNew(User user, String password) {
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));
        entityManagerProvider.get().persist(user);
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
     * Updates user password.
     * Since the link to password restoration is sent via email, account becomes verified (confirmed).
     *
     * @param user User.
     * @param password New password.
     * @return Updated user entity.
     */
    public User updatePasswordAndConfirm(User user, String password) {
        user.confirm();
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));
        entityManagerProvider.get().persist(user);
        return user;
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
     * Removes user events.
     *
     * @param user User who's events to remove.
     * @return Number of events removed.
     */
    public int removeUserEvents(User user) {
        return entityManagerProvider.get().createNamedQuery("UserEvent.removeByUser")
                .setParameter("userId", user.getId())
                .executeUpdate();
    }

    @Override
    public String getEntityCountAllQueryName() {
        return "User.countAll";
    }

    @Override
    public String getEntityAllQueryName() {
        return "User.all";
    }

    @Override
    public String getEntityCountSearchQueryName() {
        return "User.countSearch";
    }

    @Override
    public String getEntitySearchQueryName() {
        return "User.search";
    }

    @Override
    public Provider<EntityManager> getEntityManagerProvider() {
        return entityManagerProvider;
    }
}
