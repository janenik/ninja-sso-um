package controllers.sso.filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import services.sso.limits.IPCounterService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Checks if number of requests from given IP is exceeded and places special boolean attribute
 * <p>
 * {@link HitsPerIpCheckFilter#HITS_PER_IP_LIMIT_EXCEEDED} into the request. Must be invoked after
 * {@link IpAddressFilter}.
 */
@Singleton
public class HitsPerIpCheckFilter implements Filter {

    /**
     * Hits per IP limit exceeded parameter name.
     */
    public static final String HITS_PER_IP_LIMIT_EXCEEDED = "hitsPerIpLimitExceeded";

    /**
     * IP counter service.
     */
    private final IPCounterService ipCounterService;

    /**
     * Constructs per IP checker.
     *
     * @param ipCounterService IP counter service.
     */
    @Inject
    public HitsPerIpCheckFilter(IPCounterService ipCounterService) {
        this.ipCounterService = ipCounterService;
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        String ip = (String) context.getAttribute(IpAddressFilter.REMOTE_IP);
        context.setAttribute(HITS_PER_IP_LIMIT_EXCEEDED, ipCounterService.incrementAndCheckLimit(ip));
        return filterChain.next(context);
    }
}
