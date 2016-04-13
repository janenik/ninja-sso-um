package services.sso.limits;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.cache.NinjaCache;
import ninja.utils.NinjaProperties;

/**
 * IP counter service. Counts number of requests from given IP.
 * <p>
 * Expects "ipcounter.entryTimeToLiveSeconds" and "ipcounter.numberOfSafeRequests" to be defined in project properties.
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
                properties.getIntegerWithDefault("ipcounter.entryTimeToLiveSeconds", 30),
                properties.getIntegerWithDefault("ipcounter.numberOfSafeRequests", 5),
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
