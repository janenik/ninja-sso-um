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
import java.time.LocalDate;

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
        em.getTransaction().begin();

        Country country = em.find(Country.class, "US");
        if (country == null) {
            logger.info("Adding new countries... dev: {}, test: {}...", properties.isDev(), properties.isTest());

            countryService.createNew(country = new Country("US", "USA", "United States", "United States", 1));
            countryService.createNew(new Country("GB", "GBR", "United Kingdom", "United Kingdom", 44));
            countryService.createNew(new Country("CA", "CAN", "Canada", "Canada", 1));
        }

        User user = userService.getByUsername("root");
        if (user == null) {
            logger.info("Adding new root user...");

            user = new User("root", "root@example.org", "+1 650-999-9999");
            user.setFirstName("James");
            user.setLastName("Brown");
            user.setDateOfBirth(LocalDate.of(1984, 11, 24));
            user.setCountry(country);
            user.setGender(UserGender.OTHER);
            user.setLastUsedLocale("en");
            user.confirm();

            userService.createNew(user, "password");
        }

        if (properties.isTest() || properties.isDev()) {
            logger.info("Adding {} new test users...", 100);

            String login;
            for (int i = 1; i <= 100; i++) {
                login = "demouser" + i;
                user = userService.getByUsername(login);
                if (user == null) {
                    user = new User(login, "demouser" + i + "@example.org", "+1 650-999-99" + i);
                    user.setFirstName("Alexis" + i);
                    user.setLastName("Brown" + i);
                    user.setDateOfBirth(LocalDate.of(1984 + i / 100, 1 + i % 12, 1 + i % 30));
                    user.setCountry(country);
                    user.setGender(UserGender.OTHER);
                    user.setLastUsedLocale("en");
                    user.confirm();

                    userService.createNew(user, "password");
                }
            }
        }

        em.getTransaction().commit();
    }
}
