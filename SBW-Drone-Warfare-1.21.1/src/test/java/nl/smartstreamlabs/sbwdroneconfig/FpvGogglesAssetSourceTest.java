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
    void itemModelUsesFlatGuiIconAndForgeObjForWorldRendering() throws Exception {
        ClassLoader loader = FpvGogglesAssetSourceTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/fpv_goggles.json")) {
            assertNotNull(stream, "The FPV Goggles item model should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("forge:separate_transforms", root.get("loader").getAsString());
            assertEquals("sbwdroneconfig:item/fpv_goggles", root.getAsJsonObject("textures").get("particle").getAsString());

            JsonObject base = root.getAsJsonObject("base");
            assertEquals("forge:obj", base.get("loader").getAsString());
            assertEquals("sbwdroneconfig:models/item/fpv_goggles.obj", base.get("model").getAsString());
            assertEquals("sbwdroneconfig:models/item/fpv_goggles.mtl", base.get("mtl_override").getAsString());
            assertTrue(base.get("flip_v").getAsBoolean());

            JsonObject gui = root.getAsJsonObject("perspectives").getAsJsonObject("gui");
            assertEquals("minecraft:item/generated", gui.get("parent").getAsString());
            assertEquals("sbwdroneconfig:item/fpv_goggles_icon", gui.getAsJsonObject("textures").get("layer0").getAsString());

            JsonObject display = base.getAsJsonObject("display");
            assertEquals(0.42D, display.getAsJsonObject("firstperson_righthand").getAsJsonArray("scale").get(0).getAsDouble(), 1.0E-6D);
            assertEquals(0.38D, display.getAsJsonObject("thirdperson_righthand").getAsJsonArray("scale").get(0).getAsDouble(), 1.0E-6D);
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
            assertEquals("Model by LINE-O, CC BY 4.0.", root.get("tooltip.sbwdroneconfig.fpv_goggles.3").getAsString());
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
    void fpvGogglesAreCosmeticMonitorOverlayOnly() throws Exception {
        String compatSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/SbwCompat.java"));
        String cubedSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/CubedFpvDroneEntity.java"));
        String lucasSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/LucasDroneEntity.java"));
        String controlSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneControlEvents.java"));
        String networkSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonNetwork.java"));
        String bootstrapSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/SbwDroneRangeConfig.java"));
        String monitorMixinSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/MonitorItemMixin.java"));
        String mixinConfig = Files.readString(Path.of("src/main/resources/sbwdroneconfig.mixins.json"));
        String pluginSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwDroneConfigMixinPlugin.java"));
        String overlaySource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesOverlayClient.java"));
        String itemSource = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesItem.java"));

        assertTrue(compatSource.contains("return isLegacyMonitor(stack);"), "The monitor predicate must only match the real SBW monitor item.");
        assertFalse(compatSource.contains("return isLegacyMonitor(stack) || isFpvGoggles(stack);"), "FPV Goggles must not masquerade as a monitor stack.");
        assertFalse(compatSource.contains("matchesEquippedGogglesStack"), "Worn goggles must not enter the active monitor lookup.");
        assertFalse(compatSource.contains("isLinkedMonitorForDrone(headStack, drone)"), "Worn goggles must not count as the linked monitor controller stack.");
        assertFalse(compatSource.contains("getLinkedFpvGoggles"), "Goggles must not keep separate linked-drone lookup state.");
        assertFalse(compatSource.contains("isLinkedFpvGoggles"), "Goggles must not keep separate linked-drone NBT logic.");

        assertFalse(cubedSource.contains("FpvGogglesPilotSystem"), "Cubed FPV drone core must not call goggles control helpers.");
        assertFalse(lucasSource.contains("FpvGogglesPilotSystem"), "Lucas drone core must not call goggles control helpers.");
        assertFalse(controlSource.contains("FpvGogglesPilotSystem"), "Server control ticks must not route through goggles helpers.");
        assertFalse(controlSource.contains("isFpvGoggles(stack)"), "Player anchoring must not branch on goggles in the monitor control path.");
        assertTrue(monitorMixinSource.contains("SbwCompat.hasEquippedFpvGoggles(player)"), "Monitor use should require worn FPV Goggles before entering control.");
        assertTrue(monitorMixinSource.contains("message.sbwdroneconfig.fpv_goggles_required"), "Missing goggles should show a clear action-bar message.");
        assertTrue(monitorMixinSource.contains("InteractionResultHolder.fail(stack)"), "Missing goggles should block the monitor use request.");

        assertTrue(bootstrapSource.contains("FpvGogglesOverlayClient.init(modBus)"));
        assertFalse(bootstrapSource.contains("FpvGogglesViewClient.init"), "The old goggles control keybind must stay removed.");
        assertFalse(bootstrapSource.contains("FpvGogglesEvents.class"), "There must be no server-side goggles cleanup/control event.");
        assertFalse(networkSource.contains("FpvGogglesToggleUseMessage"), "Goggles must not register packets.");
        assertFalse(networkSource.contains("toggleFpvGogglesUse"), "Goggles must not expose packet send helpers.");

        assertFalse(mixinConfig.contains("SbwMonitorSource"), "Goggles must not redirect monitor main-hand checks.");
        assertFalse(pluginSource.contains("SbwMonitorSource"), "The mixin plugin must not guard removed goggles monitor-source mixins.");
        assertFalse(pluginSource.contains("SBW_CLIENT_MOUSE_HANDLER_CLASS"), "Goggles must not hook SBW mouse handling.");
        assertFalse(pluginSource.contains("SBW_VEHICLE_MOVEMENT_MESSAGE_CLASS"), "Goggles must not hook SBW movement packets.");

        assertTrue(overlaySource.contains("RenderGuiEvent.Post"));
        assertTrue(overlaySource.contains("SbwCompat.getEquippedFpvGoggles(player)"));
        assertTrue(overlaySource.contains("player.getMainHandItem()"));
        assertTrue(overlaySource.contains("SbwCompat.isLegacyUsingMonitor(mainHand)"));
        assertTrue(overlaySource.contains("FPV LINK"));
        assertTrue(overlaySource.contains("DroneClientViewContext.isActiveDeliveryView()"),
                "The cosmetic goggles overlay must not draw FPV LINK over the dedicated Delivery Drone HUD.");
        assertFalse(overlaySource.contains("KeyMapping"), "Overlay must not bind keys.");
        assertFalse(overlaySource.contains("RegisterKeyMappingsEvent"), "Overlay must not bind keys.");
        assertFalse(overlaySource.contains("TickEvent"), "Overlay must not maintain a control session.");
        assertFalse(overlaySource.contains("AddonNetwork"), "Overlay must not send packets.");
        assertFalse(overlaySource.contains("beginReadOnlyCameraIntercept"), "Overlay must not open a separate camera path.");
        assertFalse(overlaySource.contains("activeDroneEntity"), "Overlay must not resolve or manage the drone camera path.");
        assertFalse(overlaySource.contains("setCameraType"), "Overlay must not change camera mode.");
        assertFalse(overlaySource.contains("setCameraEntity"), "Overlay must not manage camera entities.");
        assertFalse(overlaySource.contains("RenderHandEvent"), "Overlay must not hide or replace the held monitor.");
        assertFalse(overlaySource.contains("setCanceled"), "Overlay must not cancel HUD/hand rendering.");

        assertFalse(itemSource.contains("InteractionResultHolder<ItemStack> use"), "Goggles item must not start control.");
        assertFalse(itemSource.contains("LinkedDrone"), "Goggles item must not store its own linked target.");
        assertFalse(itemSource.contains("Using"), "Goggles item must not store its own control state.");

        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/SbwMonitorBridge.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesPilotSystem.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesMonitorSession.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesToggleUseMessage.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesViewClient.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/FpvGogglesEvents.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/ClientMouseHandlerGogglesMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/ClientEventHandlerGogglesMonitorMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwDronePacketGogglesMonitorMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/DroneEntityGogglesMonitorLookupMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwMonitorSourceClientEventMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwMonitorSourceMouseMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwMonitorSourcePacketMixin.java")));
        assertTrue(Files.notExists(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwMonitorSourceDroneMixin.java")));

        String combinedGogglesSource = String.join("\n", overlaySource, itemSource);
        assertFalse(combinedGogglesSource.contains("MouseMoveMessage"), "Goggles must not send copied mouse-control packets.");
        assertFalse(combinedGogglesSource.contains("VehicleMovementMessage"), "Goggles must not send copied movement packets.");
        assertFalse(combinedGogglesSource.contains("setCameraEntity"), "Goggles must not manage the camera entity directly.");
        assertFalse(combinedGogglesSource.contains("setYRot"), "Goggles must not calculate yaw.");
        assertFalse(combinedGogglesSource.contains("setXRot"), "Goggles must not calculate pitch.");
        assertFalse(combinedGogglesSource.contains("CONTROLLER"), "Goggles must not assign drone controller state.");
        assertFalse(combinedGogglesSource.contains("setInvisible"), "Goggles must not hide the player.");
        assertFalse(combinedGogglesSource.contains("startRiding"), "Goggles must not create riding/passenger behavior.");
        assertFalse(combinedGogglesSource.contains("processInput"), "Goggles must not process drone movement.");
        assertFalse(combinedGogglesSource.contains("mouseInput"), "Goggles must not process mouse movement.");
        assertFalse(mixinConfig.contains("GogglesMonitor"), "Old goggles-as-monitor mixins must stay removed.");
        assertFalse(pluginSource.contains("GogglesMonitor"), "The mixin plugin must not contain stale goggles-as-monitor guards.");
        assertFalse(mixinConfig.contains("MouseHandlerGogglesDeltaMixin"));
        assertFalse(mixinConfig.contains("DroneEntityGogglesMouseControlMixin"));
    }
}
