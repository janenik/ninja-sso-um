package services.sso;

import models.sso.User;
import models.sso.UserObject;
import models.sso.UserObject.PK;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.nio.charset.Charset;

/**
 * User settings service. Provides access to data, identified by user and scope (primary key).
 * Each user object is limited to 64K of data (see {@link UserObject} for details).
 */
@Singleton
public class UserObjectService {

    /**
     * Internal charset.
     */
    static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Entity manager provider.
     */
    final Provider<EntityManager> entityManagerProvider;

    /**
     * Constructs user object service.
     *
     * @param entityManagerProvider Entity manager provider.
     * @param logger Logger.
     */
    @Inject
    public UserObjectService(Provider<EntityManager> entityManagerProvider, Logger logger) {
        this.entityManagerProvider = entityManagerProvider;
        this.logger = logger;
    }

    /**
     * Returns user object with user data by given user and scope.
     *
     * @param user User.
     * @param scope Scope of the user object.
     * @return User object or null if was not found.
     */
    public UserObject get(User user, String scope) {
        if (scope == null) {
            return null;
        }
        return get(new PK(user, scope));
    }

    /**
     * Returns user object with user data by given user and scope (as primary key here).
     *
     * @param pk Primary key of the user object.
     * @return User object or null if was not found.
     */
    public UserObject get(PK pk) {
        return entityManagerProvider.get().find(UserObject.class, pk);
    }

    /**
     * Removes user object.
     *
     * @param user User.
     * @param scope User object scope.
     */
    public void remove(User user, String scope) {
        save(user, scope, null);
    }

    /**
     * Creates new or updates existing user object.
     *
     * @param user User.
     * @param scope User object scope.
     * @param data User data as string. UTF-8 charset is used to extract bytes from string.
     * @return Saved user object.
     */
    public UserObject save(User user, String scope, String data) {
        if (scope == null) {
            throw new NullPointerException("scope is expected");
        }
        PK pk = new PK(user, scope);
        UserObject old = get(pk);
        if (old == null) {
            old = new UserObject();
            old.setPk(pk);
            old.setData(data.getBytes(UTF8));
            return save(old);
        } else if (data != null) {
            old.setData(data.getBytes(UTF8));
            return save(old);
        } else {
            entityManagerProvider.get().remove(old);
            return null;
        }
    }

    /**
     * Creates new or updates existing user object.
     *
     * @param UserObject User object to save.
     * @return Saved/updated user object.
     */
    public UserObject save(UserObject UserObject) {
        entityManagerProvider.get().persist(UserObject);
        return UserObject;
    }
}
