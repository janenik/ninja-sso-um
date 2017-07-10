package conf.sso;

import services.sso.annotations.entitiestopreload.PreloadedCountries;
import models.sso.Country;
import models.sso.User;
import models.sso.UserGender;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import services.sso.CountryService;
import services.sso.PasswordService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Start up actions for SSO.
 */
@Singleton
public class SsoStartupActions {

    /**
     * Password service.
     */
    private final PasswordService passwordService;

    /**
     * User service.
     */
    private final UserService userService;

    /**
     * Country service.
     */
    private final CountryService countryService;

    /**
     * Entity manager provider.
     */
    private final Provider<EntityManager> entityManagerProvider;

    /**
     * Preloaded countries.
     */
    private final List<Country> preloadedCountries;

    /**
     * Properties.
     */
    private final NinjaProperties properties;

    /**
     * Logger.
     */
    private final Logger logger;

    /**
     * Constructs SSO start up actions.
     *
     * @param passwordService       Password service.
     * @param userService           User service.
     * @param countryService        Country service.
     * @param entityManagerProvider Entity manager provider.
     * @param properties            Properties.
     * @param logger                Logger.
     */
    @Inject
    public SsoStartupActions(PasswordService passwordService,
                             UserService userService,
                             CountryService countryService,
                             @PreloadedCountries List<Country> preloadedCountries,
                             Provider<EntityManager> entityManagerProvider,
                             NinjaProperties properties,
                             Logger logger) {
        this.passwordService = passwordService;
        this.userService = userService;
        this.countryService = countryService;
        this.preloadedCountries = preloadedCountries;
        this.entityManagerProvider = entityManagerProvider;
        this.properties = properties;
        this.logger = logger;
    }

    @Start(order = 100)
    public void addRequiredData() {
        EntityManager em = entityManagerProvider.get();
        EntityTransaction transaction = em.getTransaction();

        logger.info("Checking required data...");
        try {
            transaction.begin();

            Country country = preloadCountries(em);

            // Check root.
            User root = userService.getByUsername("root");
            if (root == null) {
                createNewRootUser(country);
            }

            // Create demo users if not in production.
            createDemoUsersForTestAndDev(country, root != null);

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            logger.error("Error while checking required data.", e);
            return;
        }
        logger.info("Checking required data: done.");
    }

    /**
     * Preloads countries.
     *
     * @param em Entity manager.
     * @return Country that was used to check countries' presence.
     */
    private Country preloadCountries(EntityManager em) {
        String keyCountryIsoCode = "US";
        Country country = em.find(Country.class, keyCountryIsoCode);
        if (country == null) {
            for (Country preloadedCountry : preloadedCountries) {
                preloadedCountry = countryService.createNew(preloadedCountry);
                if (keyCountryIsoCode.equals(preloadedCountry.getIso())) {
                    country = preloadedCountry;
                }
            }
        }
        return country;
    }

    /***
     * Creates new root user.
     *
     * @param country Country for root.
     * @return Newly created root user.
     */
    private User createNewRootUser(Country country) {
        logger.info("Adding new root user...");

        BiFunction<String, String, String> get = properties::getWithDefault;

        String rootEmail = get.apply("application.root.defaultEmail", "root@localhost");
        String rootPhone = get.apply("application.root.defaultPhone", "+1 650-999-9999");

        User root = new User("root", rootEmail, rootPhone);
        root.setFirstName(get.apply("application.root.defaultFirstName", "Alex"));
        root.setLastName(get.apply("application.root.defaultLastName", "Brown"));
        root.setDateOfBirth(LocalDate.of(1984, 11, 24));
        root.setCountry(country);
        root.setGender(UserGender.OTHER);
        root.setLastUsedLocale("en");
        root.confirm();

        String defaultPassword = get.apply("application.root.defaultPassword", "+1 650-999-9999");
        return userService.createNew(root, defaultPassword);
    }

    /**
     * Creates demo users for test and development environment.
     *
     * @param country     Country.
     * @param rootExisted Whether the root existed prior this application start.
     */
    private void createDemoUsersForTestAndDev(Country country, boolean rootExisted) {
        if (properties.isTest() || properties.isDev() && !rootExisted) {
            int numberOfDemoUsers = properties.getIntegerWithDefault("application.demo.users", 25);
            String demoUsernamePrefix = properties.getWithDefault("application.demo.usernameprefix", "demouser");
            logger.info("Adding {} new demo users...", numberOfDemoUsers);

            String login;
            for (int i = 1; i <= numberOfDemoUsers; i++) {
                login = demoUsernamePrefix + i;
                User user = userService.getByUsername(login);

                if (user != null) {
                    continue;
                }
                user = new User(login, demoUsernamePrefix + i + "@example.org", "+1 650-999-" + i);
                user.setFirstName("Alex" + i);
                user.setLastName("Brown" + i);
                user.setDateOfBirth(LocalDate.of(1984 + i / 100, 1 + i % 12, 1 + i % 30));
                user.setCountry(country);
                user.setGender(UserGender.OTHER);
                user.setLastUsedLocale("en");
                user.confirm();

                userService.createNew(user, demoUsernamePrefix + "password" + i);
            }
        }
    }
}
