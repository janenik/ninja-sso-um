package controllers.sso.captcha;

import ninja.Context;
import ninja.Renderable;
import ninja.Result;
import ninja.utils.ResponseStreams;
import nl.captcha.Captcha;
import org.slf4j.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Renderable for captcha.
 */
class CaptchaRenderable implements Renderable {

    /**
     * Captcha.
     */
    final Captcha captcha;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Construcs renderable captcha.
     *
     * @param captcha Captcha.
     * @param logger Logger.
     */
    public CaptchaRenderable(Captcha captcha, Logger logger) {
        this.captcha = captcha;
        this.logger = logger;
    }

    @Override
    public void render(Context context, Result result) {
        result.contentType("image/png");
        try {
            ResponseStreams responseStreams = context.finalizeHeaders(result);
            ImageOutputStream ios = ImageIO.createImageOutputStream(responseStreams.getOutputStream());
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            writer.setOutput(ios);
            writer.write(null, new IIOImage(captcha.getImage(), null, null), writer.getDefaultWriteParam());
        } catch (IOException ioe) {
            logger.error("Error while rendering captcha.", ioe);
        }
    }
}
