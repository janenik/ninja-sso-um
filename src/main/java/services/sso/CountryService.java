package services.sso;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import models.sso.Country;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Country service.
 */
@Singleton
public class CountryService {

    /**
     * Entity manager provider.
     */
    final Provider<EntityManager> entityManagerProvider;

    /**
     * Constructs country service.
     *
     * @param entityManagerProvider Entity manager.
     */
    @Inject
    public CountryService(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    /**
     * Returns country by ISO code.
     *
     * @param iso ISO code.
     * @return Country by ISO code.
     */
    public Country get(String iso) {
        return entityManagerProvider.get().find(Country.class, iso);
    }

    /**
     * Returns sorted list of countries, by nice name.
     *
     * @return Sorted list of countries.
     */
    @SuppressWarnings("unchecked")
    public List<Country> getAllSortedByNiceName() {
        return (List<Country>) entityManagerProvider.get().
                createNamedQuery("Countries.getAllSortedByNiceName").getResultList();
    }
}
