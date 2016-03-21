package controllers.sso.captcha;

import com.google.common.io.ByteStreams;
import ninja.Context;
import ninja.Renderable;
import ninja.Result;
import ninja.utils.ResponseStreams;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Expired stub image.
 */
class StubImageRenderable implements Renderable {

    static final String RESOURCE_PATH = "assets/i/expired-captcha.png";
    final byte[] stubBytes;
    final Logger logger;

    public StubImageRenderable(String resourcePath, Logger logger) {
        this.logger = logger;
        byte[] stubImage;
        try {
            stubImage = ByteStreams.toByteArray(getClass().getClassLoader().getResourceAsStream(resourcePath));
        } catch (IOException ex) {
            stubImage = new byte[10];
            logger.warn("Unable to load " + RESOURCE_PATH, ex);
        }
        stubBytes = stubImage;
    }

    @Override
    public void render(Context context, Result result) {
        result.contentType("image/png");
        ResponseStreams responseStreams = context.finalizeHeaders(result);
        try {
            responseStreams.getOutputStream().write(stubBytes);
        } catch (IOException e) {
            logger.error("Problem while rendering stub captcha.", e);
        }
    }
}
