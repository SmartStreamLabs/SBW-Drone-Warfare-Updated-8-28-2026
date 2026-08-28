package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntiDroneNetVisualAssetTest {
    @Test
    void antiDroneNetBlockTextureIsThirtyTwoPixelsAndTransparent() throws Exception {
        ClassLoader loader = AntiDroneNetVisualAssetTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/textures/block/anti_drone_net.png")) {
            assertNotNull(stream, "The Anti-Drone Net block texture should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The Anti-Drone Net block texture should be readable as a PNG.");
            assertEquals(32, image.getWidth(), "The Anti-Drone Net texture should use the requested 32px resolution.");
            assertEquals(32, image.getHeight(), "The Anti-Drone Net texture should use the requested 32px resolution.");

            int transparentPixels = 0;
            int visiblePixels = 0;
            int metalPixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    int red = (argb >>> 16) & 0xFF;
                    int green = (argb >>> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    if (alpha < 16) {
                        transparentPixels++;
                    }
                    if (alpha > 128) {
                        visiblePixels++;
                    }
                    if (alpha > 128 && red < 130 && green < 140 && blue < 140) {
                        metalPixels++;
                    }
                }
            }

            assertTrue(transparentPixels > 300, "The net holes/background should be transparent so players can see through it.");
            assertTrue(visiblePixels > 120, "The net cords and metal knots should remain visible after transparency conversion.");
            assertTrue(metalPixels > 80, "The visible net should keep dark metal/cord pixels instead of becoming a blank alpha mask.");
        }
    }

    @Test
    void antiDroneNetItemTextureMatchesTheNewTransparentNet() throws Exception {
        ClassLoader loader = AntiDroneNetVisualAssetTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/textures/item/anti_drone_net.png")) {
            assertNotNull(stream, "The Anti-Drone Net inventory texture should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The Anti-Drone Net inventory texture should be readable as a PNG.");
            assertEquals(32, image.getWidth(), "The Anti-Drone Net inventory icon should use the same 32px net art as the block.");
            assertEquals(32, image.getHeight(), "The Anti-Drone Net inventory icon should use the same 32px net art as the block.");

            int transparentPixels = 0;
            int visiblePixels = 0;
            int metalPixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    int red = (argb >>> 16) & 0xFF;
                    int green = (argb >>> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    if (alpha < 16) {
                        transparentPixels++;
                    }
                    if (alpha > 128) {
                        visiblePixels++;
                    }
                    if (alpha > 128 && red < 130 && green < 140 && blue < 140) {
                        metalPixels++;
                    }
                }
            }

            assertTrue(transparentPixels > 300, "The Anti-Drone Net item icon should keep transparent holes instead of a filled square background.");
            assertTrue(visiblePixels > 120, "The Anti-Drone Net item icon should keep visible net cords and metal knots.");
            assertTrue(metalPixels > 80, "The Anti-Drone Net item icon should use the new dark net art instead of the old placeholder.");
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/anti_drone_net.json")) {
            assertNotNull(stream, "The Anti-Drone Net item model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("minecraft:item/generated", root.get("parent").getAsString());
            assertEquals("sbwdroneconfig:item/anti_drone_net", root.getAsJsonObject("textures").get("layer0").getAsString(),
                    "The inventory item should render from the updated item texture path.");
        }
    }

    @Test
    void antiDroneNetUsesThinPaneModelAndCutoutRenderLayer() throws Exception {
        ClassLoader loader = AntiDroneNetVisualAssetTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/block/anti_drone_net_side.json")) {
            assertNotNull(stream, "The Anti-Drone Net side model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("sbwdroneconfig:block/anti_drone_net", root.getAsJsonObject("textures").get("bars").getAsString());
            assertTrue(root.get("ambientocclusion").getAsBoolean() == false,
                    "The flat net pane should keep ambient occlusion disabled for cleaner transparent rendering.");

            JsonArray firstFrom = root.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonArray("from");
            JsonArray firstTo = root.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonArray("to");
            assertEquals(firstFrom.get(0).getAsDouble(), firstTo.get(0).getAsDouble(), 1.0E-6D,
                    "The net side should be a thin pane plane, not a full solid cube.");
        }

        Path renderLayerSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "AddonClientRenderLayers.java"
        );
        String source = Files.readString(renderLayerSource, StandardCharsets.UTF_8);
        assertTrue(source.contains("ItemBlockRenderTypes.setRenderLayer(AddonBlocks.ANTI_DRONE_NET.get(), net.minecraft.client.renderer.RenderType.cutout())"),
                "The Anti-Drone Net block should use cutout rendering so transparent holes render correctly.");
    }
}
