package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModLogoAssetTest {
    @Test
    void modLogoIsSquareTransparentAndReadableInTheModList() throws Exception {
        try (var stream = ModLogoAssetTest.class.getClassLoader().getResourceAsStream("cacc.png")) {
            assertNotNull(stream, "The Forge mod logo referenced by mods.toml should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The Forge mod logo should be readable as a PNG.");

            assertEquals(256, image.getWidth(), "The mod logo should use a square 256px canvas for clean mod-list scaling.");
            assertEquals(256, image.getHeight(), "The mod logo should use a square 256px canvas for clean mod-list scaling.");
            assertCornerTransparent(image, 0, 0);
            assertCornerTransparent(image, image.getWidth() - 1, 0);
            assertCornerTransparent(image, 0, image.getHeight() - 1);
            assertCornerTransparent(image, image.getWidth() - 1, image.getHeight() - 1);

            int visiblePixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (((image.getRGB(x, y) >>> 24) & 0xFF) > 32) {
                        visiblePixels++;
                    }
                }
            }

            assertTrue(visiblePixels > 8_000, "The SBW logo should stay readable after scaling down in the mod list.");
        }
    }

    @Test
    void modLogoUsesLucasOnlyArtworkWithoutTheOldBlueFpvQuadcopter() throws Exception {
        try (var stream = ModLogoAssetTest.class.getClassLoader().getResourceAsStream("cacc.png")) {
            assertNotNull(stream, "The Forge mod logo referenced by mods.toml should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The Forge mod logo should be readable as a PNG.");

            int saturatedBluePixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    int red = (argb >>> 16) & 0xFF;
                    int green = (argb >>> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    if (alpha > 32 && blue > red + 35 && blue > green + 20) {
                        saturatedBluePixels++;
                    }
                }
            }

            assertTrue(saturatedBluePixels < 200,
                    "The mod logo should use the new LUCAS-only artwork instead of the old blue FPV quadcopter logo.");
        }
    }

    private static void assertCornerTransparent(BufferedImage image, int x, int y) {
        int alpha = (image.getRGB(x, y) >>> 24) & 0xFF;
        assertTrue(alpha <= 8, "The mod logo corners should be transparent instead of a black square background.");
    }
}
