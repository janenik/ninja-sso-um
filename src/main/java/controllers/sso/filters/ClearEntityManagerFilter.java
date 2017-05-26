package controllers.sso.filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

/**
 * Clears entity manager provider after the request.
 */
@Singleton
public class ClearEntityManagerFilter implements Filter {

    /**
     * Entity manager provider.
     */
    final Provider<EntityManager> entityManagerProvider;

    /**
     * Constructs the filter.
     *
     * @param entityManagerProvider Entity manager provider.
     */
    @Inject
    public ClearEntityManagerFilter(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        try {
            return filterChain.next(context);
        } finally {
            this.entityManagerProvider.get().clear();
        }
    }
}
