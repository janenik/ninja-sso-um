package services.sso;

import models.sso.Country;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
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
     * Returns sorted list of countries, by native name.
     *
     * @return Sorted list of countries.
     */
    @SuppressWarnings("unchecked")
    public List<Country> getAllSortedByNativeName() {
        return (List<Country>) entityManagerProvider.get().
                createNamedQuery("Countries.getAllSortedByNativeName").getResultList();
    }

    /**
     * Returns sorted list of countries, by english name.
     *
     * @return Sorted list of countries.
     */
    @SuppressWarnings("unchecked")
    public List<Country> getAllSortedByName() {
        return (List<Country>) entityManagerProvider.get().
                createNamedQuery("Countries.getAllSortedByName").getResultList();
    }

    /**
     * Persists given gountry.
     *
     * @param country Country.
     * @return Persisted coutry.
     */
    public Country createNew(Country country) {
        entityManagerProvider.get().persist(country);
        return country;
    }
}
