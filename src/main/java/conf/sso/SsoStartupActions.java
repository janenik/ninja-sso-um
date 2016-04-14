package conf.sso;

import com.google.inject.Inject;
import com.google.inject.Provider;
import models.sso.Country;
import models.sso.User;
import models.sso.UserGender;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import services.sso.PasswordService;

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

            em.persist(new Country("GB", "GBR", "United Kingdom", "United Kingdom", 44));
            em.persist(new Country("CA", "CAN", "Canada", "Canada", 1));
            country = new Country("US", "USA", "United States", "United States", 1);
            em.persist(country);
        }

        User user = em.find(User.class, 1L);
        if (user == null) {
            logger.info("Adding new root user... {}, test: {}...", properties.isDev(), properties.isTest());

            user = new User("root", "root@example.org", "+1 650-999-9999");
            user.setFirstName("James");
            user.setLastName("Brown");
            user.setDateOfBirth(LocalDate.of(1984, 11, 24));
            user.setCountry(country);
            user.setGender(UserGender.OTHER);
            user.setPasswordSalt(passwordService.newSalt());
            user.setPasswordHash(passwordService.passwordHash("password", user.getPasswordSalt()));

            em.persist(user);
        }
        em.getTransaction().commit();
    }
}
