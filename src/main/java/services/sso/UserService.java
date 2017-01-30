package services.sso;

import services.sso.annotations.ExclusionDictionary;
import models.sso.User;
import org.slf4j.Logger;
import services.sso.annotations.ExclusionSubstrings;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.Set;

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
     * Exclusion dictionary for username check.
     */
    final Set<String> usernameExclusionDictionary;

    /**
     * Exclusion substrings for username check.
     */
    final Set<String> usernameExclusionSubstrings;

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
     * @param usernameExclusionDictionary Username exclusion dictionary.
     * @param passwordService Password service.
     * @param logger Logger.
     */
    @Inject
    public UserService(
            Provider<EntityManager> entityManagerProvider,
            @ExclusionDictionary Set<String> usernameExclusionDictionary,
            @ExclusionSubstrings Set<String> usernameExclusionSubstrings,
            PasswordService passwordService,
            Logger logger) {
        this.entityManagerProvider = entityManagerProvider;
        this.usernameExclusionDictionary = usernameExclusionDictionary;
        this.usernameExclusionSubstrings = usernameExclusionSubstrings;
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
        EntityManager em = entityManagerProvider.get();
        logger.warn("USER_LOAD[{}]: EM: {} / US: {}", id,
                System.identityHashCode(em), System.identityHashCode(this));
        return em.find(User.class, id);
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
     * Creates new user in a database and returns attached entity (create).
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
     * Tests if the given username is acceptable.
     *
     * @param username Username to test for availability.
     * @return Whether the given username is available.
     */
    public boolean isUsernameAcceptable(String username) {
        if (username == null) {
            return false;
        }
        username = username.trim().toLowerCase();
        if (username.isEmpty()) {
            return false;
        }
        if (usernameExclusionDictionary.contains(username)) {
            return false;
        }
        for (String exclusionSubstring : usernameExclusionSubstrings) {
            if (username.contains(exclusionSubstring)) {
                return false;
            }
        }
        return true;
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
     *
     * @param user User.
     * @param password New password.
     * @return Updated user entity.
     */
    public User updatePassword(User user, String password) {
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));
        entityManagerProvider.get().persist(user);
        return user;
    }

    /**
     * Updates user password and changes user's status to confirmed.
     * Since the link to password restoration is sent via email, account becomes verified (confirmed).
     *
     * @param user User.
     * @param password New password.
     * @return Updated user entity.
     */
    public User updatePasswordAndConfirm(User user, String password) {
        user.confirm();
        return updatePassword(user, password);
    }

    /**
     * Updates existing user.
     *
     * @param user User to update.
     */
    public void update(User user) {
        EntityManager em = entityManagerProvider.get();
        logger.warn("USER_SAVE[{}]: EM: {} / US: {}", user.getId(),
                System.identityHashCode(em), System.identityHashCode(this));
        em.persist(user);
    }

    /**
     * Updates existing user with last used locale. Uses simple update query to avoid whole user update.
     *
     * @param user User to update.
     * @param lastUsedLocale Last used locale.
     */
    public void updateLastUsedLocale(User user, String lastUsedLocale) {
        entityManagerProvider.get().createNamedQuery("User.updateLastUsedLocale")
                .setParameter("userId", user.getId())
                .setParameter("lastUsedLocale", lastUsedLocale)
                .executeUpdate();
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
