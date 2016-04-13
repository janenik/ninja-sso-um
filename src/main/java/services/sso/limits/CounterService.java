package services.sso.limits;

import ninja.cache.NinjaCache;

/**
 * Scoped counter service based on Ninja cache.
 */
public class CounterService {

    /**
     * Scope.
     */
    protected final String scope;

    /**
     * Cache to store recent IPs and hits.
     */
    protected final NinjaCache cache;

    /**
     * Upper limit for the counter.
     */
    protected final long upperLimit;

    /**
     * Interval in seconds after which counter entry is evicted from cache if not updated.
     */
    protected final long entryTimeToLiveSeconds;

    /**
     * Previous value, as string.
     */
    protected final String entryTimeToLiveSecondsAsString;

    /**
     * Constructs counter service for given scope and time to live.
     *
     * @param scope Scope.
     * @param entryTimeToLiveSeconds Time to live, in seconds.
     * @param cache Cache.
     */
    public CounterService(String scope, long entryTimeToLiveSeconds, int upperLimit, NinjaCache cache) {
        this.scope = scope + "_";
        this.cache = cache;
        this.entryTimeToLiveSeconds = entryTimeToLiveSeconds;
        this.entryTimeToLiveSecondsAsString = entryTimeToLiveSeconds + "s";
        this.upperLimit = upperLimit;
    }

    /**
     * Increments a counter for given key.
     *
     * @param key Key.
     * @return Counter value for the given key.
     */
    public final long increment(String key) {
        String prefixedKey = getKey(key);
        Long oldValue = cache.get(prefixedKey, Long.class);
        if (oldValue == null) {
            cache.add(prefixedKey, 1L, this.entryTimeToLiveSecondsAsString);
            return 1L;
        }
        return cache.incr(prefixedKey);
    }

    /**
     * Increments given counter and checks if its value is above the limit.
     *
     * @param key IP to check.
     * @return Whether the given IP address is suspected to abuse the service
     */
    public final boolean incrementAndCheck(String key) {
        return increment(key) >= upperLimit;
    }

    /**
     * Returns counter value.
     *
     * @param key Key.
     * @return Counter value for key.
     */
    public final long getCounter(String key) {
        Long oldValue = cache.get(getKey(key), Long.class);
        return oldValue != null ? oldValue : 0L;
    }

    /**
     * Builds a key for the counter by prefixing it with the scope.
     *
     * @param key Key.
     * @return Key with prefixed scope.
     */
    public final String getKey(String key) {
        return this.scope + key;
    }
}
