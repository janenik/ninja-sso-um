package conf.sso;

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
import java.util.function.BiFunction;

/**
 * Start up actions for SSO.
 */
@Singleton
public class SsoStartupActions {

    /**
     * Password service.
     */
    @Inject
    PasswordService passwordService;

    /**
     * Entity manager provider.
     */
    @Inject
    Provider<EntityManager> entityManagerProvider;

    /**
     * User service.
     */
    @Inject
    UserService userService;

    /**
     * Country service.
     */
    @Inject
    CountryService countryService;

    /**
     * Properties.
     */
    @Inject
    NinjaProperties properties;

    /**
     * Logger.
     */
    @Inject
    Logger logger;


    @Start(order = 100)
    public void addDataForDevAndTest() {
        if (properties.isProd()) {
            return;
        }

        EntityManager em = entityManagerProvider.get();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Make sure default country exists.
            Country country = em.find(Country.class, "US");
            if (country == null) {
                country = countryService.createNew(new Country("US", "USA", "United States", "United States", 1));
            }

            // Check root.
            User root = userService.getByUsername("root");
            if (root == null) {
                createNewRootUser(country);
            }

            // Create demo users if needed.
            createDemoUsersForTestAndDev(country, root != null);

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
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
        return userService.createNew(root,defaultPassword);
    }

    /**
     * Creates demo users for test and development environment.
     *
     * @param country Country.
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
