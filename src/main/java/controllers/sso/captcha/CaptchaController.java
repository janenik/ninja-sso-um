package controllers.sso.captcha;

import models.sso.token.ExpiredTokenException;
import models.sso.token.IllegalTokenException;
import ninja.Context;
import ninja.Result;
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
    final CaptchaTokenService captchaTokenService;

    /**
     * Stub image for expired / wrong captcha tokens.
     */
    final StubImageRenderable stubImageRenderable;

    /**
     * Fish eye renderer.
     */
    final SimpleFishEyeGimpyRenderer fishEyeGimpyRenderer;

    /**
     * Curved line.
     */
    final CurvedLineNoiseProducer curvedLineNoiseProducer;

    /**
     * Background.
     */
    final GradiatedBackgroundProducer background1;

    /**
     * Background.
     */
    final GradiatedBackgroundProducer background2;

    /**
     * Logger.
     */
    final Logger logger;

    /**
     * Captcha width.
     */
    final int width;

    /**
     * Captcha height.
     */
    final int height;

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
        this.background1 = new GradiatedBackgroundProducer();
        this.background1.setFromColor(Color.LIGHT_GRAY);
        this.background1.setToColor(Color.WHITE);
        this.background2 = new GradiatedBackgroundProducer();
        this.background2.setFromColor(Color.WHITE);
        this.background2.setToColor(Color.LIGHT_GRAY);

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
    public Result captcha(Context context, @Param(CAPTCHA_PARAMETER) String captchaToken) {
        Result result = new Result(Result.SC_200_OK).doNotCacheContent();
        try {
            String displaySequence = captchaTokenService.extractCaptchaText(captchaToken);
            Captcha captcha = new Captcha.Builder(this.width, this.height)
                    .addText(() -> displaySequence)
                    .addBorder()
                    .gimp(fishEyeGimpyRenderer)
                    .addNoise(curvedLineNoiseProducer)
                    .addBackground(Math.random() > 0.5D ? background1 : background2)
                    .build();
            return result.render(new CaptchaRenderable(captcha, logger));
        } catch (CaptchaTokenService.AlreadyUsedTokenException | ExpiredTokenException |
                IllegalTokenException ex) {
            return result.render(stubImageRenderable);
        }
    }
}
