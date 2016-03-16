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
public final class IPCounterService {

    /**
     * Whether the service is enabled.
     */
    private volatile boolean enabled = true;

    /**
     * Cache to store recent IPs and hits.
     */
    private final NinjaCache cache;

    /**
     * Interval in seconds after which the IP counter is evicted from cache if not updated.
     */
    private final long entryTimeToLiveSeconds;

    /**
     * Safe number of safe requests to hold before showing captcha.
     */
    private final int numberOfSafeRequests;

    /**
     * Constructs IP counter service.
     *
     * @param ninjaCache Cache.
     * @param properties Project properties.
     */
    @Inject
    public IPCounterService(NinjaCache ninjaCache, NinjaProperties properties) {
        this.cache = ninjaCache;
        this.entryTimeToLiveSeconds = properties.getIntegerWithDefault("ipcounter.entryTimeToLiveSeconds", 30);
        this.numberOfSafeRequests = properties.getIntegerWithDefault("ipcounter.numberOfSafeRequests", 5);
    }

    /**
     * Increments IP counter and checks if hits from the given IP address exceed safe parameter
     * "ipcounter.numberOfSafeRequests".
     *
     * @param ip IP to check.
     * @return Whether the given IP address is suspected to abuse the service
     */
    public boolean incrementAndCheck(String ip) {
        if (ip == null || !enabled) {
            return false;
        }
        return increment(ip) >= numberOfSafeRequests;
    }

    /**
     * Increments a counter for given IP.
     *
     * @param ip IP.
     * @return Number of hits for this IP.
     */
    public long increment(String ip) {
        String ipVisitsKey = getIpKey(ip);
        Long oldValue = cache.get(ipVisitsKey, Long.class);
        if (oldValue == null) {
            cache.add(ipVisitsKey, 1L, this.entryTimeToLiveSeconds + "s");
            return 1L;
        }
        return cache.incr(ipVisitsKey);
    }

    /**
     * Returns number of IP hits.
     *
     * @param ip IP.
     * @return Number of IP hits.
     */
    public long getIpHits(String ip) {
        Long oldValue = cache.get(getIpKey(ip), Long.class);
        return oldValue != null ? oldValue : 0L;
    }

    /**
     * Whether the service is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables the service.
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Disables the service.
     */
    public void disable() {
        enabled = false;
    }

    /**
     * Constructs IP key for cache.
     *
     * @param ip IP.
     * @return IP cache key.
     */
    String getIpKey(String ip) {
        return "ip_" + ip;
    }
}
