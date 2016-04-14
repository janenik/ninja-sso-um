package services.sso.limits;

import ninja.cache.NinjaCache;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Counter service that counts generic key usage (number of tries, etc).
 */
@Singleton
public class GenericCounterService extends CounterService {

    /**
     * Constructs service instance.
     *
     * @param cache Cache.
     * @param properties Project properties.
     */
    @Inject
    public GenericCounterService(NinjaCache cache, NinjaProperties properties) {
        super("generic",
                properties.getIntegerWithDefault("counters.generic.entryTimeToLiveSeconds", 3600),
                properties.getIntegerWithDefault("counters.generic.numberOfSafeRequests", 5),
                cache);
    }
}
