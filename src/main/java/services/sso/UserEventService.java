package services.sso;

import models.sso.User;
import models.sso.UserEvent;
import models.sso.UserEventType;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Service for user events.
 */
@Singleton
public class UserEventService {

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
     * Constructs user event service.
     *
     * @param entityManagerProvider Entity manager provider.
     * @param logger Logger.
     */
    @Inject
    public UserEventService(Provider<EntityManager> entityManagerProvider, Logger logger) {
        this.entityManagerProvider = entityManagerProvider;
        this.logger = logger;
    }

    /**
     * Creates user event when the user is created or updated, recording IP address and additional data.
     *
     * @param user Create or updated user.
     * @param ip Remote IP address.
     * @param data Additional data of the event.
     * @return Created user event.
     */
    public UserEvent onUserSave(User user, String ip, Map<String, ?> data) {
        UserEvent userEvent = newUserEvent(user, UserEventType.SIGN_UP, ip, data);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Creates user event when the user updates his password. Data may contain old salt and old password hash to
     * create hint for user when he tries to log in with the old credentials.
     *
     * @param user User who updates password.
     * @param ip Remove IP address.
     * @param data Additional data for event.
     * @return Created user event.
     */
    public UserEvent onUserPasswordUpdate(User user, String ip, Map<String, ?> data) {
        UserEvent userEvent = newUserEvent(user, UserEventType.PASSWORD_CHANGE, ip, data);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Creates  new user event based on provided data.
     *
     * @param user User.
     * @param eventType Event type.
     * @param ip Remote IP
     * @param data Additional data to save.
     * @return User event to save.
     */
    static UserEvent newUserEvent(User user, UserEventType eventType, String ip, Map<String, ?> data) {
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
