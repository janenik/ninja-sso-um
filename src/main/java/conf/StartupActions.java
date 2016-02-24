package conf;

import com.google.inject.Inject;
import com.google.inject.Provider;
import models.sso.Country;
import models.sso.User;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import services.sso.PasswordService;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.time.LocalDate;


@Singleton
public class StartupActions {

    PasswordService passwordService;
    Provider<EntityManager> entityManagerProvider;
    NinjaProperties ninjaProperties;
    Logger logger;

    @Inject
    public StartupActions(
            PasswordService passwordService,
            Provider<EntityManager> entityManagerProvider,
            NinjaProperties ninjaProperties,
            Logger logger) {
        this.passwordService = passwordService;
        this.entityManagerProvider = entityManagerProvider;
        this.ninjaProperties = ninjaProperties;
        this.logger = logger;
    }

    @Start(order = 100)
    public void generateDummyDataWhenInTest() {
        logger.warn("\n\n\n\nProd: {}   Dev: {}    Test:{}\n\n\n\n",
                ninjaProperties.isProd(),
                ninjaProperties.isDev(),
                ninjaProperties.isTest());
        if (!ninjaProperties.isProd()) {
            EntityManager em = entityManagerProvider.get();
            em.getTransaction().begin();

            Country country = new Country("US", "USA", "United States", "United States", 1);
            em.persist(country);

            User user = new User("root", "root@example.org", "+1 650-999-9999");
            user.setFirstName("James");
            user.setLastName("Brown");
            user.setDateOfBirth(LocalDate.of(1984, 11, 24));
            user.setCountry(country);
            user.setPasswordSalt(passwordService.newSalt());
            user.setPasswordHash(passwordService.passwordHash("password", user.getPasswordSalt()));

            em.persist(user);

            em.getTransaction().commit();
        }
    }
}
