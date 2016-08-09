package services.sso;

import com.google.common.io.BaseEncoding;
import models.sso.User;
import models.sso.UserEvent;
import models.sso.UserEventType;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for user's events.
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
     * Base encoding.
     */
    final BaseEncoding baseEncoding;

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
        this.baseEncoding = BaseEncoding.base64Url().omitPadding();
    }

    /**
     * Creates user event when the user is created, recording IP address and additional data.
     *
     * @param user Created user.
     * @param ip Remote IP address.
     * @param data Additional data of the event.
     * @return Created user event with {@link UserEventType#SIGN_UP}.
     */
    public UserEvent onUserSignUp(User user, String ip, Map<String, ?> data) {
        UserEvent userEvent = newUserEvent(user, UserEventType.SIGN_UP, ip, data);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Creates user event when the user updates his password. Data may contain old salt and old password hash to
     * create hint for user when he tries to log in with the old credentials.
     *
     * @param user User who updates password.
     * @param oldSalt Old password salt.
     * @param oldHash Old password hash.
     * @param ip Remove IP address.
     * @param headers Additional data for event, headers here.
     * @return Created user event with {@link UserEventType#PASSWORD_CHANGE}.
     */
    public UserEvent onUserPasswordUpdate(User user, byte[] oldSalt, byte[] oldHash, String ip,
                                          Map<String, ?> headers) {
        Map<String, Object> dataToSave = new HashMap<>(headers);
        dataToSave.put("password:oldSalt", baseEncoding.encode(oldSalt));
        dataToSave.put("password:oldHash", baseEncoding.encode(oldHash));
        UserEvent userEvent = newUserEvent(user, UserEventType.PASSWORD_CHANGE, ip, dataToSave);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Remembers user sign in time and remote IP as a new event.
     *
     * @param user User.
     * @param ip Remote IP.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#SIGN_IN}.
     */
    public UserEvent onSignIn(User user, String ip, Map<String, ?> data) {
        UserEvent userEvent = newUserEvent(user, UserEventType.SIGN_IN, ip, data);
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
