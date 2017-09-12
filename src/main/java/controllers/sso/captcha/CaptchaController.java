package controllers.sso.captcha;

import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.Result;
import ninja.metrics.Timed;
import ninja.params.Param;
import ninja.utils.NinjaProperties;
import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import org.slf4j.Logger;
import services.sso.CaptchaTokenService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;

/**
 * Captcha controller.
 */
@Singleton
public class CaptchaController {

    /**
     * Captcha parameter name.
     */
    public static final String CAPTCHA_PARAMETER = "cpt";

    /**
     * Captcha token service.
     */
    private final CaptchaTokenService captchaTokenService;

    /**
     * Stub image for expired / wrong captcha tokens.
     */
    private final StubImageRenderable stubImageRenderable;

    /**
     * Fish eye renderer.
     */
    private final SimpleFishEyeGimpyRenderer fishEyeGimpyRenderer;

    /**
     * Curved line.
     */
    private final CurvedLineNoiseProducer curvedLineNoiseProducer;

    /**
     * Background.
     */
    private final GradiatedBackgroundProducer backgroundLeftToRight;

    /**
     * Background.
     */
    private final GradiatedBackgroundProducer backgroundRightToLeft;

    /**
     * Logger.
     */
    private final Logger logger;

    /**
     * Captcha width.
     */
    private final int width;

    /**
     * Captcha height.
     */
    private final int height;

    /**
     * Constructs captcha controller.
     *
     * @param captchaTokenService Captcha token service.
     * @param properties Properties.
     * @param logger Logger.
     */
    @Inject
    public CaptchaController(CaptchaTokenService captchaTokenService, NinjaProperties properties, Logger logger) {
        this.captchaTokenService = captchaTokenService;
        this.fishEyeGimpyRenderer = new SimpleFishEyeGimpyRenderer();
        this.stubImageRenderable = new StubImageRenderable(
                properties.get("application.sso.captcha.expiredImage"), logger);
        this.curvedLineNoiseProducer = new CurvedLineNoiseProducer(Color.BLACK, 4);
        this.backgroundLeftToRight = new GradiatedBackgroundProducer();
        this.backgroundLeftToRight.setFromColor(Color.LIGHT_GRAY);
        this.backgroundLeftToRight.setToColor(Color.WHITE);
        this.backgroundRightToLeft = new GradiatedBackgroundProducer();
        this.backgroundRightToLeft.setFromColor(Color.WHITE);
        this.backgroundRightToLeft.setToColor(Color.LIGHT_GRAY);

        this.width = properties.getIntegerWithDefault("application.sso.captcha.width", 240);
        this.height = properties.getIntegerWithDefault("application.sso.captcha.height", 50);

        this.logger = logger;
    }

    /**
     * Renders captcha with given parameters.
     *
     * @param context Web context.
     * @param captchaToken Captcha token parameters.
     * @return Image result.
     */
    @Timed
    public Result captcha(Context context, @Param(CAPTCHA_PARAMETER) String captchaToken) {
        Result result = new Result(Result.SC_200_OK).doNotCacheContent();
        try {
            String displaySequence = captchaTokenService.extractCaptchaText(captchaToken);
            Captcha captcha = new Captcha.Builder(this.width, this.height)
                    .addText(() -> displaySequence)
                    .addBorder()
                    .gimp(fishEyeGimpyRenderer)
                    .addNoise(curvedLineNoiseProducer)
                    .addBackground(Math.random() > 0.5D ? backgroundLeftToRight : backgroundRightToLeft)
                    .build();
            return result.render(new CaptchaRenderable(captcha, logger));
        } catch (CaptchaTokenService.AlreadyUsedTokenException | ExpiredTokenException |
                IllegalTokenException ex) {
            return result.render(stubImageRenderable);
        }
    }
}
