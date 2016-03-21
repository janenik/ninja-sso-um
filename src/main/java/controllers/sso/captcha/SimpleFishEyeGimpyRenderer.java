package controllers.sso.captcha;

import nl.captcha.gimpy.GimpyRenderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;

/**
 * FishEye without lines, with random eye position. (left, middle, right).
 */
class SimpleFishEyeGimpyRenderer implements GimpyRenderer {

    /**
     * Multiplier for algorithm.
     */
    static final double[] MULTIPLIERS = new double[]{0.2, 0.4d, 0.5d, 0.6d, 0.8d};

    /**
     * Random.
     */
    static final Random random = new SecureRandom();

    @Override
    public void gimp(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();

        Graphics2D graph = (Graphics2D) image.getGraphics();

        int pix[] = new int[height * width];
        int j = 0;

        for (int j1 = 0; j1 < width; j1++) {
            for (int k1 = 0; k1 < height; k1++) {
                pix[j] = image.getRGB(j1, k1);
                j++;
            }
        }

        double distance = ranInt(width / 8, width / 4);

        int wMid = (int) (((double) image.getWidth()) * MULTIPLIERS[random.nextInt(MULTIPLIERS.length)]);
        int hMid = image.getHeight() / 2;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int relX = x - wMid;
                int relY = y - hMid;
                double d1 = Math.sqrt(relX * relX + relY * relY);
                if (d1 < distance) {
                    int j2 = wMid + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (x - wMid));
                    int k2 = hMid + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (y - hMid));
                    image.setRGB(x, y, pix[j2 * height + k2]);
                }
            }
        }

        graph.dispose();
    }

    /**
     * Returns random integer between i and j (including).
     *
     * @param i From.
     * @param j To.
     * @return Random integer between i and j (including).
     */
    private int ranInt(int i, int j) {
        return (int) (i + ((j - i) + 1) * Math.random());
    }

    /**
     * Fish eye formula.
     *
     * @param s Parameter.
     * @return Result.
     */
    private static double fishEyeFormula(double s) {
        // implementation of:
        // g(s) = - (3/4)s3 + (3/2)s2 + (1/4)s, with s from 0 to 1.
        if (s < 0.0D) {
            return 0.0D;
        }
        if (s > 1.0D) {
            return s;
        }
        return -0.75D * s * s * s + 1.5D * s * s + 0.25D * s;
    }
}
