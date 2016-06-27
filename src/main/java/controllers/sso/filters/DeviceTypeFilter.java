package controllers.sso.filters;

import controllers.sso.auth.type.DeviceInputType;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumSet;

/**
 * Filter that identifies the device and its input type: pointer, touchscreen or unknown.
 */
@Singleton
public class DeviceTypeFilter implements Filter {

    /**
     * Device input type constant.
     */
    public static final String DEVICE_INPUT_TYPE = "deviceInputType";

    /**
     * Device operating system.
     */
    public static final String OPERATING_SYSTEM = "operatingSystem";

    /**
     * Device browser.
     */
    public static final String BROWSER = "browser";

    /**
     * Device browser version.
     */
    public static final String BROWSER_VERSION = "browserVersion";

    /**
     * Pointer device types.
     */
    private static final EnumSet<DeviceType> POINTER_TYPES =
            EnumSet.of(DeviceType.COMPUTER, DeviceType.DMR, DeviceType.GAME_CONSOLE);

    /**
     * Touchscreen device types.
     */
    private static final EnumSet<DeviceType> TOUCHSCREEN_TYPES =
            EnumSet.of(DeviceType.MOBILE, DeviceType.TABLET, DeviceType.WEARABLE);

    /**
     * Logger.
     */
    @Inject
    Logger logger;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        UserAgent userAgent = UserAgent.parseUserAgentString(context.getHeader("User-Agent"));
        OperatingSystem os = userAgent.getOperatingSystem();
        context.setAttribute(OPERATING_SYSTEM, os);
        context.setAttribute(BROWSER, userAgent.getBrowser());
        context.setAttribute(BROWSER_VERSION, userAgent.getBrowserVersion());
        DeviceType deviceType = os.getDeviceType();
        if (POINTER_TYPES.contains(deviceType)) {
            context.setAttribute(DEVICE_INPUT_TYPE, DeviceInputType.POINTER);
        } else if (TOUCHSCREEN_TYPES.contains(deviceType)) {
            context.setAttribute(DEVICE_INPUT_TYPE, DeviceInputType.TOUCHSCREEN);
        } else {
            context.setAttribute(DEVICE_INPUT_TYPE, DeviceInputType.UNKNOWN);
        }
        if (logger.isInfoEnabled()) {
            logger.info("DEVICE TYPE: {} -> {}", deviceType, context.getAttribute(DEVICE_INPUT_TYPE));
        }
        return filterChain.next(context);
    }
}