package services.sso;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import models.sso.User;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * User service.
 */
@Singleton
public class UserService {

    final Logger logger;

    final Provider<EntityManager> entityManagerProvider;

    @Inject
    public UserService(Provider<EntityManager> entityManagerProvider, Logger logger) {
        this.entityManagerProvider = entityManagerProvider;
        this.logger = logger;
    }

    public User get(Long id) {
        return entityManagerProvider.get().find(User.class, id);
    }

    public User getByEmail(String email) {
        if (Strings.isNullOrEmpty(email)) {
            return null;
        }
        Query q = entityManagerProvider.get().createNamedQuery("User.getByEmail");
        q.setParameter("email", email.toLowerCase().trim());
        q.setMaxResults(1);
        try {
            return (User) q.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public User getByUsername(String username) {
        if (Strings.isNullOrEmpty(username)) {
            return null;
        }
        Query q = entityManagerProvider.get().createNamedQuery("User.getByUsername");
        q.setParameter("username", username.toLowerCase().trim());
        q.setMaxResults(1);
        try {
            return (User) q.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public User getByPhone(String phone) {
        if (Strings.isNullOrEmpty(phone)) {
            return null;
        }
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
     * @param password Password to set.
     * @return Saved user.
     */
    public User save(User user, String password) {
        //user.setNewPassword(password);
        entityManagerProvider.get().persist(user);
        return user;
    }

    /**
     * Updates password for the given user.
     *
     * @param user User.
     * @param newPassword New password.
     */
    public void updatePassword(User user, String newPassword) {
        //user.setNewPassword(newPassword);
        update(user);
    }

    public void update(User user) {
        entityManagerProvider.get().persist(user);
    }


    public User detach(User user) {
        entityManagerProvider.get().detach(user);
        return user;
    }
}
