package controllers.sso.filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import org.slf4j.Logger;

import javax.inject.Inject;

/**
 * IP address filter: extracts IP address from request or headers (for example, provided by nginx front server).
 */
public class IpAddressFilter implements Filter {

    /**
     * Context parameter name for remote IP.
     */
    public static final String REMOTE_IP = "REMOTE_IP";

    /**
     * Header name to extract IP address from. Used when remote address from request context is undefined or localhost.
     */
    private static final String IP_HEADER_NAME = "x-real-ip";

    /**
     * Default IP remote address, v4.
     */
    private static final String DEFAULT_IP = "127.0.0.1";

    /**
     * Default IP remote address, v6.
     */
    private static final String DEFAULT_IP_V6 = "::1";

    /**
     * Default IP remote address, v6 with subnet.
     */
    private static final String DEFAULT_IP_V6_W_SUBNET = "::1/128";

    /**
     * Default full IP remote address, v6.
     */
    private static final String DEFAULT_IP_V6_FULL = "0000:0000:0000:0000:0000:0000:0000:0001";

    /**
     * Logger.
     */
    @Inject
    Logger logger;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        String ip = context.getRemoteAddr();
        if (ip == null || ip.isEmpty() || DEFAULT_IP.equals(ip) ||
                DEFAULT_IP_V6.equals(ip) || DEFAULT_IP_V6_W_SUBNET.equals(ip) || DEFAULT_IP_V6_FULL.equals(ip)) {
            ip = context.getHeader(IP_HEADER_NAME);
        }
        if (ip == null) {
            ip = DEFAULT_IP_V6;
        }
        context.setAttribute(REMOTE_IP, ip);
        if (logger.isInfoEnabled()) {
            logger.info("{} - {}:{}", ip, context.getMethod(), context.getRequestPath());
        }
        return filterChain.next(context);
    }
}
