package controllers.sso.auth.type;

/**
 * Device input type based on pointing devices.
 * See <a href="https://en.wikipedia.org/wiki/Pointing_device">Pointing devices</a>}).
 */
public enum DeviceInputType {

    /**
     * Device with a pointer, like mouse, touchpad, based on motion of an object. For example, PC, laptop, gaming
     * console.
     */
    POINTER,

    /**
     * Device where user uses a touchscreen to operate a device with finger or stylus. For example, iPhone, iPad,
     * tablet.
     */
    TOUCHSCREEN,

    /**
     * Unknown device input type.
     */
    UNKNOWN;
}
