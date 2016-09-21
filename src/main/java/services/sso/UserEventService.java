package services.sso;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import models.sso.PaginationResult;
import models.sso.User;
import models.sso.UserEvent;
import models.sso.UserEventType;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for user's events.
 */
@Singleton
public class UserEventService implements Paginatable<UserEvent> {

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
        UserEvent userEvent = newEvent(user, UserEventType.SIGN_UP, ip, data);
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
     * @param data Additional data for event, headers here.
     * @return Created user event with {@link UserEventType#PASSWORD_CHANGE}.
     */
    public UserEvent onUserPasswordUpdate(User user, byte[] oldSalt, byte[] oldHash, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>(data);
        dataToSave.put("password.old.salt", baseEncoding.encode(oldSalt));
        dataToSave.put("password.old.hash", baseEncoding.encode(oldHash));
        UserEvent userEvent = newEvent(user, UserEventType.PASSWORD_CHANGE, ip, dataToSave);
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
        UserEvent userEvent = newEvent(user, UserEventType.SIGN_IN, ip, data);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Saves common data access by source user event, like listing users.
     *
     * @param source Source user who performs the action.
     * @param ip IP address of the source user.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#ACCESS}.
     */
    @SuppressWarnings("unchecked")
    public UserEvent onDataAccess(User source, String query, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave;
        if (!Strings.isNullOrEmpty(query)) {
            dataToSave = new HashMap<>(data);
            dataToSave.put("users.search.query", query);
        } else {
            dataToSave = (Map<String, Object>) data;
        }
        UserEvent event = newEvent(source, UserEventType.ACCESS, ip, dataToSave);
        entityManagerProvider.get().persist(event);
        return event;
    }

    /**
     * Saves user data access event by source user event.
     *
     * @param source Source user who performs the action.
     * @param target Target user who's data is accessed.
     * @param appUrl Current application URL.
     * @param ip IP address of the source user.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#ACCESS}.
     */
    public UserEvent onUserDataAccess(User source, User target, String appUrl, String ip, Map<String, ?> data) {
        UserEvent event = newEvent(source, UserEventType.ACCESS, ip, data);
        event.setTargetUser(target);
        event.setUrl(appUrl);
        entityManagerProvider.get().persist(event);
        return event;
    }

    /**
     * Saves user data update event by source user event.
     *
     * @param source Source user who performs the action.
     * @param target Target user who's data is accessed.
     * @param appUrl Current application URL.
     * @param ip IP address of the source user.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#UPDATE}.
     */
    public UserEvent onUserDataUpdate(User source, User target, String appUrl, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>(data);
        dataToSave.put("user.old.username", target.getUsername());

        dataToSave.put("user.old.email", target.getEmail());
        dataToSave.put("user.old.phone", target.getPhone());
        dataToSave.put("user.old.countryId", target.getCountry() != null ? target.getCountry().getIso() : "");

        dataToSave.put("user.old.firstName", target.getFirstName());
        dataToSave.put("user.old.lastName", target.getLastName());
        dataToSave.put("user.old.middleName", String.valueOf(target.getMiddleName()));
        dataToSave.put("user.old.birthDay", target.getDateOfBirth().toString());

        dataToSave.put("user.old.version", target.getVersion());
        dataToSave.put("user.old.role", target.getRole().toString());
        dataToSave.put("user.old.confirmationState", target.getConfirmationState().toString());

        UserEvent event = newEvent(source, UserEventType.UPDATE, ip, dataToSave);
        event.setTargetUser(target);
        event.setUrl(appUrl);
        entityManagerProvider.get().persist(event);
        return event;
    }

    /**
     * Searches for users by given query in email, username, first name or last name.
     *
     * @param owner Owner of the event (a user who produced it).
     * @param query Search query.
     * @param currentPage Current page.
     * @param objectsPerPage Objects per page.
     * @return Pagination result with events for user current page.
     */
    public PaginationResult<UserEvent> searchByUser(User owner, String query, int currentPage, int objectsPerPage) {
        return search(query, Collections.singletonMap("userId", owner.getId()), currentPage, objectsPerPage);
    }

    @Override
    public PaginationResult<UserEvent> search(String query, int currentPage, int entitiesPerPage) {
        throw new UnsupportedOperationException("Search for events without user scope is not implemented.");
    }

    @Override
    public PaginationResult<UserEvent> search(String query, Map<String, Object> additionalParameters,
                                              int currentPage, int entitiesPerPage) {
        if (!additionalParameters.containsKey("userId")) {
            throw new UnsupportedOperationException("Search for events without user scope is not implemented.");
        }
        return Paginatable.super.search(query, additionalParameters, currentPage, entitiesPerPage);
    }

    @Override
    public String getEntityCountAllQueryName() {
        return "UserEvent.countAllByUser";
    }

    @Override
    public String getEntityAllQueryName() {
        return "UserEvent.allByUser";
    }

    @Override
    public String getEntityCountSearchQueryName() {
        return "UserEvent.searchByUser";
    }

    @Override
    public String getEntitySearchQueryName() {
        return "UserEvent.countSearchByUser";
    }

    @Override
    public Provider<EntityManager> getEntityManagerProvider() {
        return entityManagerProvider;
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
    private static UserEvent newEvent(User user, UserEventType eventType, String ip, Map<String, ?> data) {
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
