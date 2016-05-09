package services.sso;

import models.sso.User;
import models.sso.UserEvent;
import models.sso.UserEventType;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
     * Saves user into the database and returns user event associated with creation (attached entity).
     *
     * @param user User to save.
     * @param remoteIp Remote IP.
     * @return User event associated with user creation.
     */
    public UserEvent createNew(User user, String password, String remoteIp) {
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));
        entityManagerProvider.get().persist(user);
        // Sign up event.
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setType(UserEventType.SIGN_UP);
        userEvent.setIp(remoteIp);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
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
     * Updates user password, storing old salt and hash in user event for convenience.
     * This information may be used as a hint when user tries to sign in with old password.
     * Since the link to password restoration is sent via email, account becomes verified (confirmed).
     *
     * @param user User.
     * @param password New password.
     * @param remoteIp Remote IP.
     * @return User event that contains information about password update.
     */
    public UserEvent updatePasswordAndConfirm(User user, String password, String remoteIp) {
        byte[] oldPasswordSalt = user.getPasswordSalt();
        byte[] oldPasswordHash = user.getPasswordHash();
        user.confirm();
        user.setPasswordSalt(passwordService.newSalt());
        user.setPasswordHash(passwordService.passwordHash(password, user.getPasswordSalt()));
        entityManagerProvider.get().persist(user);
        // Update password event.
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setType(UserEventType.PASSWORD_CHANGE);
        userEvent.setIp(remoteIp);
        // Event data to store old salt and hash.
        userEvent.setData(getEventDataForPasswordChange(oldPasswordSalt, oldPasswordHash));
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
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
     * Removes user events.
     *
     * @param user User who's events to remove.
     * @return Number of events removed.
     */
    public int removeUserEvents(User user) {
        Query q = entityManagerProvider.get().createNamedQuery("UserEvents.removeByUser");
        q.setParameter("userId", user.getId());
        return q.executeUpdate();
    }

    /**
     * Constructs event data bytes that contain old password salt and hash. The first 4 bytes are salt length,
     * followed by salt bytes, then 4 bytes for hash length, followed by hash bytes. Order of bytes for integers is
     * described in {@link java.io.DataOutput}.
     *
     * @param oldSalt Old password salt bytes.
     * @param oldHash Old password hash bytes.
     * @return Event data bytes.
     */
    private byte[] getEventDataForPasswordChange(byte[] oldSalt, byte[] oldHash) {
        // Event data to store old salt and hash.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8 + oldSalt.length + oldHash.length);
        DataOutputStream dataOutputStream = new DataOutputStream(bos);
        try {
            dataOutputStream.writeInt(oldSalt.length);
            dataOutputStream.write(oldSalt);
            dataOutputStream.writeInt(oldHash.length);
            dataOutputStream.write(oldHash);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception while serializing old password data", e);
        }
        return bos.toByteArray();
    }
}
