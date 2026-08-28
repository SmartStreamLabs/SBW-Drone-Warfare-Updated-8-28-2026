package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneJammerAssetReplacementTest {
    @Test
    void droneJammerUsesConvertedSignalJammerModelOutsideGui() throws Exception {
        ClassLoader loader = DroneJammerAssetReplacementTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/drone_jammer.json")) {
            assertNotNull(stream, "The Drone Jammer item model should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("forge:separate_transforms", root.get("loader").getAsString());
            assertEquals("forge:obj", root.getAsJsonObject("base").get("loader").getAsString());
            assertEquals("sbwdroneconfig:models/item/drone_jammer.obj", root.getAsJsonObject("base").get("model").getAsString());
            assertEquals("sbwdroneconfig:models/item/drone_jammer.mtl", root.getAsJsonObject("base").get("mtl_override").getAsString());

            JsonObject display = root.getAsJsonObject("base").getAsJsonObject("display");
            assertEquals(0, display.getAsJsonObject("firstperson_righthand").getAsJsonArray("rotation").get(1).getAsInt(),
                    "The Signal Jammer front face should point toward the player in first person instead of showing its thin side.");
            assertEquals(0, display.getAsJsonObject("thirdperson_righthand").getAsJsonArray("rotation").get(1).getAsInt(),
                    "The Signal Jammer front face should point outward in third person instead of being yawed into a black strip.");
            assertEquals(0.32D, display.getAsJsonObject("thirdperson_righthand").getAsJsonArray("scale").get(0).getAsDouble(), 1.0E-6D,
                    "The held Signal Jammer should be compact instead of hanging down like an oversized black slab.");
        }

        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/drone_jammer.obj"),
                "The converted Signal Jammer OBJ should be packaged.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/drone_jammer.mtl"),
                "The converted Signal Jammer material library should be packaged.");
    }

    @Test
    void droneJammerKeepsFlatReadableInventoryIcon() throws Exception {
        ClassLoader loader = DroneJammerAssetReplacementTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/drone_jammer.json")) {
            assertNotNull(stream, "The Drone Jammer item model should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject gui = root.getAsJsonObject("perspectives").getAsJsonObject("gui");
            assertEquals("minecraft:item/generated", gui.get("parent").getAsString());
            assertEquals("sbwdroneconfig:item/drone_jammer", gui.getAsJsonObject("textures").get("layer0").getAsString());

            JsonObject thirdPerson = root.getAsJsonObject("perspectives").getAsJsonObject("thirdperson_righthand");
            assertNotNull(thirdPerson, "The Drone Jammer should override third-person hand rendering.");
            assertEquals("minecraft:item/generated", thirdPerson.get("parent").getAsString(),
                    "The held Drone Jammer should use a front-facing generated icon instead of the ultra-thin OBJ side profile.");
            assertEquals("sbwdroneconfig:item/drone_jammer", thirdPerson.getAsJsonObject("textures").get("layer0").getAsString());

            JsonObject firstPerson = root.getAsJsonObject("perspectives").getAsJsonObject("firstperson_righthand");
            assertNotNull(firstPerson, "The Drone Jammer should override first-person hand rendering.");
            assertEquals("minecraft:item/generated", firstPerson.get("parent").getAsString(),
                    "The first-person Drone Jammer should also avoid showing the OBJ side profile.");
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/textures/item/drone_jammer.png")) {
            assertNotNull(stream, "The Drone Jammer inventory icon should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The Drone Jammer inventory icon should be readable as a PNG.");
            assertEquals(32, image.getWidth(), "The Drone Jammer icon should use a 32px canvas for a readable item silhouette.");
            assertEquals(32, image.getHeight(), "The Drone Jammer icon should use a 32px canvas for a readable item silhouette.");

            int visiblePixels = 0;
            int signalPixels = 0;
            int antennaPixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    int red = (argb >>> 16) & 0xFF;
                    int green = (argb >>> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    if (alpha > 32) {
                        visiblePixels++;
                    }
                    if (alpha > 32 && green > red + 35 && green > blue + 20) {
                        signalPixels++;
                    }
                    if (alpha > 32 && y < 11 && red < 120 && green < 130 && blue < 130) {
                        antennaPixels++;
                    }
                }
            }

            assertTrue(visiblePixels > 150, "The Drone Jammer icon should be visible in a small inventory slot.");
            assertTrue(signalPixels > 4, "The Drone Jammer icon should keep a small green LED accent.");
            assertTrue(antennaPixels > 35, "The Drone Jammer icon should read as the Sketchfab-style multi-antenna signal jammer.");
        }
    }
}
