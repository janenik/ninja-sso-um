package services.sso.limits;

import ninja.cache.NinjaCache;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * IP counter service. Counts number of requests from given IP.
 * <p>
 * Expects "counters.ip.entryTimeToLiveSeconds" and "counters.ip.numberOfSafeRequests" to be defined in project
 * properties.
 */
@Singleton
public final class IPCounterService extends CounterService {

    /**
     * Constructs IP counter service.
     *
     * @param cache Cache.
     * @param properties Project properties.
     */
    @Inject
    public IPCounterService(NinjaCache cache, NinjaProperties properties) {
        super("ip",
                properties.getIntegerWithDefault("counters.ip.entryTimeToLiveSeconds", 30),
                properties.getIntegerWithDefault("counters.ip.numberOfSafeRequests", 5),
                cache);
    }

    /**
     * Returns number of IP hits.
     *
     * @param ip IP.
     * @return Number of IP hits.
     */
    public long getIpHits(String ip) {
        return getCounter(ip);
    }
}
