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
 * User settings service.
 */
@Singleton
public class UserObjectService {

    final Logger logger;
    final Provider<EntityManager> entitiyManager;

    @Inject
    public UserObjectService(Provider<EntityManager> entitiyManager, Logger logger) {
        this.entitiyManager = entitiyManager;
        this.logger = logger;
    }

    public UserObject get(User user, String scope) {
        if (scope == null) {
            return null;
        }
        return get(new PK(user, scope));
    }

    public UserObject get(PK pk) {
        return entitiyManager.get().find(UserObject.class, pk);
    }

    public void remove(User user, String scope) {
        save(user, scope, null);
    }

    public UserObject save(User user, String scope, String data) {
        if (scope == null) {
            throw new NullPointerException("scope is expected");
        }
        PK pk = new PK(user, scope);
        UserObject old = get(pk);
        if (old == null) {
            old = new UserObject();
            old.setPk(pk);
            old.setData(data.getBytes(Charset.forName("UTF-8")));
            return save(old);
        } else if (data != null) {
            old.setData(data.getBytes(Charset.forName("UTF-8")));
            return save(old);
        } else {
            entitiyManager.get().remove(old);
            return null;
        }
    }

    public UserObject save(UserObject UserObject) {
        entitiyManager.get().persist(UserObject);
        return UserObject;
    }
}
