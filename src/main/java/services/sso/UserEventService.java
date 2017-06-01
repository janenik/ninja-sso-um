package services.sso;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import models.sso.PaginationResult;
import models.sso.User;
import models.sso.UserEvent;
import models.sso.UserEventType;
import models.sso.UserRole;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for user's events.
 */
@Singleton
public class UserEventService implements Paginatable<UserEvent> {

    /**
     * Namespace for event data payload.
     */
    private static String EVENT_DATA_NAMESPACE = "event.data";

    /**
     * Entity manager provider.
     */
    final Provider<EntityManager> entityManagerProvider;

    /**
     * Password service.
     */
    final PasswordService passwordService;

    /**
     * Base encoding.
     */
    final BaseEncoding baseEncoding;

    /**
     * Json serializer.
     */
    final ObjectMapper objectMapper;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Constructs user event service.
     *
     * @param entityManagerProvider Entity manager provider.
     * @param logger Logger.
     */
    @Inject
    public UserEventService(
            Provider<EntityManager> entityManagerProvider,
            PasswordService passwordService,
            ObjectMapper objectMapper,
            Logger logger) {
        this.entityManagerProvider = entityManagerProvider;
        this.passwordService = passwordService;
        this.objectMapper = objectMapper;
        this.baseEncoding = BaseEncoding.base64Url().omitPadding();
        this.logger = logger;
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
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        UserEvent userEvent = newEvent(user, UserEventType.SIGN_UP, ip, dataToSave);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Returns a hint duration of the last password change if the given password matches previous
     * password.
     *
     * @param user User.
     * @param password Old password to search for.
     * @return Optional of the duration of the last password change.
     */
    @SuppressWarnings("unchecked")
    public Optional<Date> getLastPasswordChangeDate(User user, String password) {
        Query query = entityManagerProvider.get().createNamedQuery("UserEvent.ownByUserAndType");
        query.setParameter("userId", user.getId());
        query.setParameter("type", UserEventType.PASSWORD_CHANGE);
        query.setMaxResults(1);
        List<UserEvent> events = (List<UserEvent>) query.getResultList();
        for (UserEvent event : events) {
            if (event.getData() == null || event.getData().length == 0) {
                continue;
            }
            try {
                Map<String, Object> dataMap = objectMapper.readValue(event.getData(), Map.class);
                byte[] oldSalt = baseEncoding.decode((String) dataMap.get("password.old.salt"));
                byte[] oldHash = baseEncoding.decode((String) dataMap.get("password.old.hash"));
                if (passwordService.isValidPassword(password, oldSalt, oldHash)) {
                    return Optional.of(new Date(event.getTime().toEpochSecond() * 1000L));
                }
            } catch (IOException e) {
                String message = String.format(
                        "Error parsing event data: %d / user: %d", event.getId(), user.getId());
                logger.warn(message, e);
            }
        }
        return Optional.empty();
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
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        UserEvent userEvent = newEvent(user, UserEventType.SIGN_IN, ip, dataToSave);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Remembers user confirmation time and remote IP as a new event.
     *
     * @param user User.
     * @param ip Remote IP.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#CONFIRMATION}.
     */
    public UserEvent onConfirmation(User user, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        UserEvent userEvent = newEvent(user, UserEventType.CONFIRMATION, ip, dataToSave);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Remembers user role change time and remote IP as a new event.
     *
     * @param user User.
     * @param oldRole Old user role.
     * @raram targetUser User who performed the change.
     * @param ip Remote IP.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#ROLE_CHANGE}.
     */
    public UserEvent onRoleChange(User user, UserRole oldRole, User targetUser, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        dataToSave.put("user.old.role", oldRole);
        dataToSave.put("user.new.role", user.getRole());
        UserEvent userEvent = newEvent(user, UserEventType.ROLE_CHANGE, ip, dataToSave);
        if (targetUser != null) {
            userEvent.setTargetUser(targetUser);
        }
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Remembers user sign-in disable time and remote IP as a new event.
     *
     * @param user User.
     * @param ip Remote IP.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#DISABLE_SIGN}.
     */
    public UserEvent onSignInDisable(User user, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        UserEvent userEvent = newEvent(user, UserEventType.DISABLE_SIGN, ip, dataToSave);
        entityManagerProvider.get().persist(userEvent);
        return userEvent;
    }

    /**
     * Remembers user sign-in enable time and remote IP as a new event.
     *
     * @param user User.
     * @param ip Remote IP.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#ENABLE_SIGN}.
     */
    public UserEvent onSignInEnable(User user, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        UserEvent userEvent = newEvent(user, UserEventType.ENABLE_SIGN, ip, dataToSave);
        entityManagerProvider.get().persist(userEvent);
        entityManagerProvider.get().flush();
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
        return onUserPasswordUpdate(user, null, oldSalt, oldHash, ip, data);
    }

    /**
     * Creates user event when the user updates someone's password. Data may contain old salt and old password hash to
     * create hint for user when he tries to log in with the old credentials.
     *
     * @param user Admin who changes the password.
     * @param targetUser User whose password was updated.
     * @param oldSalt Old password salt.
     * @param oldHash Old password hash.
     * @param ip Remove IP address.
     * @param data Additional data for event, headers here.
     * @return Created user event with {@link UserEventType#PASSWORD_CHANGE}.
     */
    public UserEvent onUserPasswordUpdate(User user, User targetUser, byte[] oldSalt, byte[] oldHash,
                                          String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        dataToSave.put("password.old.salt", baseEncoding.encode(oldSalt));
        dataToSave.put("password.old.hash", baseEncoding.encode(oldHash));
        UserEvent userEvent = newEvent(user, UserEventType.PASSWORD_CHANGE, ip, dataToSave);
        userEvent.setTargetUser(targetUser);
        entityManagerProvider.get().persist(userEvent);
        entityManagerProvider.get().flush();
        return userEvent;
    }

    /**
     * Saves users search data access by source user event, like listing users. Administrative operation.
     *
     * @param source Source user who performs the action.
     * @param ip IP address of the source user.
     * @param data Additional data for event.
     * @return User event with {@link UserEventType#ACCESS}.
     */
    @SuppressWarnings("unchecked")
    public UserEvent onUsersSearchAccess(User source, String query, String ip, Map<String, ?> data) {
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        dataToSave.put("users.search.query", String.valueOf(query));
        UserEvent event = newEvent(source, UserEventType.SEARCH_USERS, ip, dataToSave);
        entityManagerProvider.get().persist(event);
        entityManagerProvider.get().flush();
        return event;
    }

    /**
     * Saves user data access event by source user event. Administrative operation.
     *
     * @param source Source user who performs the action.
     * @param target Target user who's data is accessed.
     * @param appUrl Current application URL.
     * @param ip IP address of the source user.
     * @param data Additional data for event.
     */
    public void onUserDataAccess(User source, User target, String appUrl, String ip, Map<String, ?> data) {
        // Don't log own access events assuming that owner knows own data.
        if (source.equals(target)) {
            return;
        }
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        UserEvent event = newEvent(source, UserEventType.ACCESS, ip, dataToSave);
        event.setTargetUser(target);
        event.setUrl(appUrl);
        entityManagerProvider.get().persist(event);
        entityManagerProvider.get().flush();
    }

    /**
     * Saves user data access event by source user event. Administrative operation.
     *
     * @param source Source user who performs the action.
     * @param target Target user who's data is accessed.
     * @param appUrl Current application URL.
     * @param ip IP address of the source user.
     * @param data Additional data for event.
     */
    public void onUserLogAccess(User source, User target, String appUrl, String ip, Map<String, ?> data) {
        // Don't log own access events assuming that owner knows own data.
        if (source.equals(target)) {
            return;
        }
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        UserEvent event = newEvent(source, UserEventType.EVENTS_ACCESS, ip, dataToSave);
        event.setTargetUser(target);
        event.setUrl(appUrl);
        entityManagerProvider.get().persist(event);
        entityManagerProvider.get().flush();
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
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(EVENT_DATA_NAMESPACE, data);
        dataToSave.put("user.old.username", target.getUsername());

        dataToSave.put("user.old.email", target.getEmail());
        dataToSave.put("user.old.phone", target.getPhone());
        dataToSave.put("user.old.countryId", target.getCountry() != null ? target.getCountry().getIso() : "");

        dataToSave.put("user.old.firstName", target.getFirstName());
        dataToSave.put("user.old.lastName", target.getLastName());
        dataToSave.put("user.old.middleName", String.valueOf(target.getMiddleName()));
        dataToSave.put("user.old.birthDay", target.getDateOfBirth().toString());

        dataToSave.put("user.old.created", target.getCreated().toString());
        dataToSave.put("user.old.updated", target.getUpdated().toString());
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
    public PaginationResult<UserEvent> search(
            String query,
            Map<String, Object> additionalParameters,
            int currentPage,
            int entitiesPerPage) {
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
        return "UserEvent.countSearchByUser";
    }

    @Override
    public String getEntitySearchQueryName() {
        return "UserEvent.searchByUser";
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
    private UserEvent newEvent(User user, UserEventType eventType, String ip, Map<String, ?> data) {
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setType(eventType);
        userEvent.setIp(ip);
        if (data != null) {
            try {
                userEvent.setData(objectMapper.writeValueAsBytes(data));
            } catch (JsonProcessingException jpe) {
                logger.warn(
                        "Unexpected error while generating JSON from Map. Event will be saved without payload.", jpe);
            }
        }
        return userEvent;
    }
}
