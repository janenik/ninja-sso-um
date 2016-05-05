package controllers.sso.filters;

import com.google.common.collect.Sets;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

/**
 * IP address filter: extracts IP address from request or headers (for example, provided by nginx front server).
 */
public class IpAddressFilter implements Filter {

    /**
     * Context parameter name for remote IP.
     */
    public static final String REMOTE_IP = "remoteIp";

    /**
     * Default header name to extract IP address from. Used when remote address from request context is undefined or
     * localhost.
     */
    private static final String DEFAULT_PROXY_PASS_IP_HEADER_NAME = "X-Real-IP";

    /**
     * Localhost address, v4.
     */
    private static final String LOCALHOST_IPV4 = "127.0.0.1";

    /**
     * Localhost address, v6.
     */
    private static final String LOCALHOST_IPV6 = "::1";

    /**
     * Set that contains possible representations of localhost (IPv4 and IPv6).
     */
    private static final Set<String> LOCALHOSTS = Collections.unmodifiableSet(
            Sets.newHashSet(LOCALHOST_IPV6, LOCALHOST_IPV4, "localhost", "::1/128",
                    "0000:0000:0000:0000:0000:0000:0000:0001", "0:0:0:0:0:0:0:1"));

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Ninja properties.
     */
    final NinjaProperties properties;

    /**
     * Proxy IP header name. Value provided in header by frontend server like nginx.
     */
    final String proxyIpHeaderName;

    /**
     * Proxy IP header name, lowercased. Value provided in header by frontend server like nginx.
     */
    final String proxyIpHeaderNameLowerCased;

    @Inject
    public IpAddressFilter(NinjaProperties properties, Logger logger) {
        this.properties = properties;
        this.logger = logger;

        this.proxyIpHeaderName =
                properties.getWithDefault("application.sso.proxy.ipHeaderName", DEFAULT_PROXY_PASS_IP_HEADER_NAME);
        this.proxyIpHeaderNameLowerCased = proxyIpHeaderName.toLowerCase();
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        String ip = context.getHeader(proxyIpHeaderName);
        if (ip == null) {
            ip = context.getHeader(proxyIpHeaderNameLowerCased);
        }
        if (ip == null || ip.isEmpty() || LOCALHOSTS.contains(ip)) {
            ip = context.getRemoteAddr();
        }
        if (ip == null) {
            ip = LOCALHOST_IPV6;
        }
        context.setAttribute(REMOTE_IP, ip);
        if (logger.isInfoEnabled()) {
            logger.info("{} - {} : {}", ip, context.getMethod(), context.getRequestPath());
        }
        return filterChain.next(context);
    }
}
