package web.sso.common;

import models.sso.User;
import models.sso.UserGender;
import services.sso.CountryService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;

/**
 * Utility class for entities in test.
 */
@Singleton
public final class TestEntitiesFactory {

    /**
     * User service.
     */
    private final UserService userService;

    /**
     * Country service.
     */
    private final CountryService countryService;

    /**
     * Constructs test factory.
     *
     * @param userService    User service.
     * @param countryService Country service.
     */
    @Inject
    public TestEntitiesFactory(UserService userService, CountryService countryService) {
        this.userService = userService;
        this.countryService = countryService;
    }

    /**
     * Country for test account.
     */
    public static final String COUNTRY_ID = "US";

    /**
     * Birth year for test account.
     */
    public static final int YEAR = 1988;

    /**
     * Birth month for test account.
     */
    public static final int MONTH = 12;

    /**
     * Birth day for test account.
     */
    public static final int DAY_OF_MONTH = 24;


    /**
     * First name for test account.
     */
    public static final String FIRST_NAME = "FirstName";

    /**
     * Last name for test account.
     */
    public static final String LAST_NAME = "LastName";

    /**
     * Username for test user.
     */
    public static final String USERNAME = "username1984";

    /**
     * Password for test user.
     */
    public static final String EMAIL = "email@example.org";

    /**
     * PHONE for test user.
     */
    public static final String PHONE = "+1 650 999 9999";

    /**
     * Password for test user.
     */
    public static final String PASSWORD = "userPassword";

    /**
     * Creates new user for test purposes.
     *
     * @return New user,
     */
    public User createNewUser() {
        User user = new User(USERNAME, EMAIL, PHONE);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setCountry(countryService.get(COUNTRY_ID));
        user.setDateOfBirth(LocalDate.of(YEAR, MONTH, DAY_OF_MONTH));
        user.setGender(UserGender.FEMALE);
        userService.createNew(user, PASSWORD);
        return user;
    }
}
