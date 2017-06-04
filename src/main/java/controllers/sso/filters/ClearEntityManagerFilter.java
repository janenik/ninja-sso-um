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
     * Assets prefix for the exclusion.
     */
    private static final String ASSETS_PREFIX = "/assets/";

    /**
     * Entity manager provider.
     */
    private final Provider<EntityManager> entityManagerProvider;

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
        if (context.getRequestPath().startsWith(ASSETS_PREFIX)) {
            return filterChain.next(context);
        }
        try {
            return filterChain.next(context);
        } finally {
            this.entityManagerProvider.get().clear();
        }
    }
}
