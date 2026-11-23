package nl.smartstreamlabs.sbwdroneconfig;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FpvGogglesAssetSourceTest {
    @Test
    void registersFpvGogglesItemAndCreativeTabEntry() throws Exception {
        String itemsSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonItems.java"));
        assertTrue(itemsSource.contains("FPV_GOGGLES"));
        assertTrue(itemsSource.contains("\"fpv_goggles\""));
        assertTrue(itemsSource.contains("new FpvGogglesItem"));
        assertTrue(itemsSource.contains("tooltip.sbwdroneconfig.fpv_goggles.3"));

        String gogglesSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesItem.java"));
        assertTrue(gogglesSource.contains("extends ArmorItem"));
        assertTrue(gogglesSource.contains("Type.HELMET"));
        assertTrue(gogglesSource.contains("getArmorTexture"));
        assertFalse(gogglesSource.contains("InteractionResultHolder<ItemStack> use"), "FPV Goggles should keep normal armor equip behavior.");
        assertTrue(gogglesSource.contains("FpvGogglesClientExtensions.INSTANCE"));

        String modelSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesArmorModel.java"));
        assertTrue(modelSource.contains("fpv_frame"));
        assertTrue(modelSource.contains("fpv_lens"));
        assertTrue(modelSource.contains("fpv_rear_band"));

        String tabSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonCreativeTabs.java"));
        assertTrue(tabSource.contains("AddonItems.FPV_GOGGLES.get()"));
    }

    @Test
    void bundlesConvertedSketchfabGogglesAssets() throws Exception {
        ClassLoader loader = FpvGogglesAssetSourceTest.class.getClassLoader();
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/fpv_goggles.obj"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/fpv_goggles.mtl"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/credits/fpv_goggles.txt"));

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/textures/item/fpv_goggles.png")) {
            assertNotNull(stream, "The FPV Goggles texture should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The FPV Goggles texture should be readable.");
            assertTrue(image.getWidth() >= 256);
            assertTrue(image.getHeight() >= 256);
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/textures/item/fpv_goggles_icon.png")) {
            assertNotNull(stream, "The FPV Goggles inventory icon should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The FPV Goggles inventory icon should be readable.");
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());
        }
    }

    @Test
    void itemModelUsesFlatGuiIconAndNeoForgeObjForWorldRendering() throws Exception {
        ClassLoader loader = FpvGogglesAssetSourceTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/fpv_goggles.json")) {
            assertNotNull(stream, "The FPV Goggles item model should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("neoforge:separate_transforms", root.get("loader").getAsString());
            assertEquals("sbwdroneconfig:item/fpv_goggles", root.getAsJsonObject("textures").get("particle").getAsString());

            JsonObject base = root.getAsJsonObject("base");
            assertEquals("neoforge:obj", base.get("loader").getAsString());
            assertEquals("sbwdroneconfig:models/item/fpv_goggles.obj", base.get("model").getAsString());
            assertEquals("sbwdroneconfig:models/item/fpv_goggles.mtl", base.get("mtl_override").getAsString());
            assertTrue(base.get("flip_v").getAsBoolean());

            JsonObject gui = root.getAsJsonObject("perspectives").getAsJsonObject("gui");
            assertEquals("minecraft:item/generated", gui.get("parent").getAsString());
            assertEquals("sbwdroneconfig:item/fpv_goggles_icon", gui.getAsJsonObject("textures").get("layer0").getAsString());
        }
    }

    @Test
    void englishTranslationsAndCreditsKeepRequiredAttribution() throws Exception {
        ClassLoader loader = FpvGogglesAssetSourceTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("FPV Goggles", root.get("item.sbwdroneconfig.fpv_goggles").getAsString());
            assertEquals("Required headset for FPV drone control.", root.get("tooltip.sbwdroneconfig.fpv_goggles.1").getAsString());
            assertEquals("Equip on your head, then use a linked Superb Warfare Monitor to fly.", root.get("tooltip.sbwdroneconfig.fpv_goggles.2").getAsString());
            assertEquals("FPV goggles required.", root.get("message.sbwdroneconfig.fpv_goggles_required").getAsString());
            assertEquals(root.get("bio.sbwdroneconfig.fpv_goggles").getAsString(), root.get("tooltip.sbwdroneconfig.fpv_goggles.3").getAsString());
            assertFalse(root.has("message.sbwdroneconfig.fpv_goggles_view_on"));
            assertFalse(root.has("key.sbwdroneconfig.toggle_fpv_goggles_view"));
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/credits/fpv_goggles.txt")) {
            assertNotNull(stream, "The FPV Goggles attribution file should be packaged.");
            String credits = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(credits.contains("Meshy FPV Goggles"));
            assertTrue(credits.contains("LINE-O"));
            assertTrue(credits.contains("https://skfb.ly/ptSTu"));
            assertTrue(credits.contains("Creative Commons Attribution 4.0"));
        }
    }

    @Test
    void fpvGogglesRequireRealMonitorAndRemainOverlayOnly() throws Exception {
        String compatSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/SbwCompat.java"));
        String monitorMixinSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/MonitorItemMixin.java"));
        String monitor089MixinSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/MonitorItem089Mixin.java"));
        String networkSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonNetwork.java"));
        String bootstrapSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/SbwDroneRangeConfig.java"));
        String mixinConfig = Files.readString(Path.of("src/main/resources/sbwdroneconfig.mixins.json"));
        String overlaySource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesOverlayClient.java"));
        String itemSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesItem.java"));
        String cubedInputClientSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/CubedFpvInputClient.java"));
        String cubedInputMessageSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/CubedFpvInputMessage.java"));
        String cubedInputStateSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/CubedFpvInputState.java"));
        String cubedDroneSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/CubedFpvDroneEntity.java"));

        assertTrue(compatSource.contains("return isLegacyMonitor(stack);"));
        assertFalse(compatSource.contains("return isLegacyMonitor(stack) || isFpvGoggles(stack);"));
        assertFalse(compatSource.contains("getLinkedFpvGoggles"));
        assertFalse(compatSource.contains("isLinkedFpvGoggles"));
        assertFalse(compatSource.contains("matchesPilotStack(headStack"));
        assertFalse(compatSource.contains("isLinkedMonitorForDrone(headStack, drone)"));

        assertTrue(monitorMixinSource.contains("SbwCompat.hasEquippedFpvGoggles(player)"));
        assertTrue(monitorMixinSource.contains("message.sbwdroneconfig.fpv_goggles_required"));
        assertTrue(monitorMixinSource.contains("InteractionResultHolder.fail(stack)"));
        assertTrue(monitor089MixinSource.contains("SbwCompat.hasEquippedFpvGoggles(player)"));
        assertTrue(monitor089MixinSource.contains("message.sbwdroneconfig.fpv_goggles_required"));
        assertTrue(monitor089MixinSource.contains("InteractionResultHolder.fail(stack)"));

        assertTrue(bootstrapSource.contains("FpvGogglesOverlayClient.init(modBus)"));
        assertTrue(bootstrapSource.contains("CubedFpvInputClient.init(modBus)"));
        assertFalse(bootstrapSource.contains("FpvGogglesViewClient.init"));
        assertFalse(networkSource.contains("FpvGogglesToggleUseMessage"));
        assertFalse(networkSource.contains("FpvGogglesMovementMessage"));
        assertFalse(networkSource.contains("FpvGogglesMouseMoveMessage"));
        assertFalse(networkSource.contains("toggleFpvGogglesUse"));
        assertFalse(networkSource.contains("sendFpvGogglesMovement"));
        assertFalse(networkSource.contains("sendFpvGogglesMouseMove"));

        assertFalse(mixinConfig.contains("ClientMouseHandlerGogglesMixin"));
        assertFalse(mixinConfig.contains("MouseHandlerGogglesDeltaMixin"));
        assertFalse(mixinConfig.contains("DroneEntityGogglesMouseControlMixin"));
        assertTrue(mixinConfig.contains("CubedFpvCameraMixin"));

        assertTrue(overlaySource.contains("RenderGuiEvent.Post"));
        assertTrue(overlaySource.contains("SbwCompat.getEquippedFpvGoggles(player)"));
        assertTrue(overlaySource.contains("player.getMainHandItem()"));
        assertTrue(overlaySource.contains("SbwCompat.isLegacyUsingMonitor(mainHand)"));
        assertTrue(overlaySource.contains("FPV LINK"));
        assertFalse(overlaySource.contains("KeyMapping"));
        assertFalse(overlaySource.contains("AddonNetwork"));
        assertFalse(overlaySource.contains("setCameraEntity"));
        assertFalse(overlaySource.contains("RenderHandEvent"));
        assertFalse(overlaySource.contains("setCanceled"));

        assertTrue(cubedInputClientSource.contains("DroneClientViewContext.activeMonitor(minecraft)"));
        assertTrue(cubedInputClientSource.contains("SbwCompat.isUsingLinkedMonitor(monitor)"));
        assertTrue(cubedInputClientSource.contains("AddonNetwork.sendCubedFpvInput"));
        assertTrue(cubedInputMessageSource.contains("\"cubed_fpv_input\""));
        assertTrue(cubedInputMessageSource.contains("SbwCompat.isUsingLinkedMonitorForDrone(sender, drone)"));
        assertTrue(cubedInputStateSource.contains("vehicle.processInput(keys)"));
        assertTrue(cubedDroneSource.contains("CubedFpvInputState.applyRecentInput(this)"));
        assertTrue(networkSource.contains("registrar.playToServer(CubedFpvInputMessage.TYPE"));
        assertTrue(networkSource.contains("sendCubedFpvInput(CubedFpvInputMessage message)"));

        assertFalse(itemSource.contains("InteractionResultHolder<ItemStack> use"));
        assertFalse(itemSource.contains("LinkedDrone"));
        assertFalse(itemSource.contains("Using"));

        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesPilotSystem.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/MonitorMouseFallbackClient.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesToggleUseMessage.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesMovementMessage.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesMouseMoveMessage.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesViewClient.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/ClientMouseHandlerGogglesMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/MouseHandlerGogglesDeltaMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/DroneEntityGogglesMouseControlMixin.java")));
    }
}
