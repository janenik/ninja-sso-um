package services.sso;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import models.sso.User;
import models.sso.UserEvent;
import models.sso.UserEventType;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Service for user events.
 */
@Singleton
public class UserEventService {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    Logger logger;

    Provider<EntityManager> entityManagerProvider;

    @Inject
    public UserEventService(Provider<EntityManager> entityManagerProvider, Logger logger) {
        this.entityManagerProvider = entityManagerProvider;
        this.logger = logger;
    }

    public UserEvent onUserSave(User user, String ip, Map<String, ?> data) {
        UserEvent userEvent = newUserEvent(user, UserEventType.SIGN_UP, ip, data);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    public UserEvent onUserPasswordUpdate(User user, String ip, Map<String, ?> data) {
        UserEvent userEvent = newUserEvent(user, UserEventType.PASSWORD_CHANGE, ip, data);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    private static UserEvent newUserEvent(User user, UserEventType eventType, String ip, Map<String, ?> data) {
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setType(eventType);
        userEvent.setIp(ip);
        if (data != null) {
            userEvent.setData(data.toString().getBytes(UTF8));
        }
        return userEvent;
    }
}
