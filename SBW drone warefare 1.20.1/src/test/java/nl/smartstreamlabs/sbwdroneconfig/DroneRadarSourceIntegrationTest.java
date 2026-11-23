package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneRadarSourceIntegrationTest {
    @Test
    void handheldDetectorUsesServerScanAndExcludesFiberLinks() throws Exception {
        String client = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneJammerClient.java"));
        String server = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/PortableFpvDetectorServer.java"));
        String events = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneJammerEvents.java"));

        assertTrue(server.contains("AddonConfig.portableFpvDetectorRange()"));
        assertTrue(server.contains("AddonConfig.portableFpvDetectorScanIntervalTicks()"));
        assertTrue(server.contains("level.getEntities(player, searchBox"));
        assertTrue(server.contains("SbwCompat.isDrone(entity)"));
        assertTrue(server.contains("FiberOpticLinkSystem.getLinkMode(entity)"));
        assertTrue(server.contains("DroneLinkMode.FIBER_OPTIC"));
        assertTrue(client.contains("PortableFpvDetectorScanMessage"));
        assertTrue(client.contains("SoundEvents.NOTE_BLOCK_HAT"));
        assertFalse(client.contains("minecraft.level.getEntities"), "Detector targets should come from server scan packets.");
        assertFalse(events.contains("recordInfluence"), "The handheld detector must not apply the old jamming effect.");
    }

    @Test
    void handheldDetectorUsesSmallGameplaySummaryAndRaisedReceiver() throws Exception {
        String client = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneJammerClient.java"));
        String config = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonConfig.java"));
        String item = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneJammerItem.java"));
        String model = Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/models/item/drone_jammer.json"));

        assertTrue(config.contains("PORTABLE_FPV_DETECTOR_HUD_ENABLED_VALUE"), "Detector HUD should be configurable.");
        assertTrue(config.contains("defineInRange(\"portableFpvDetectorRange\", 1500"), "Default detector range should be 1500 blocks.");
        assertTrue(config.contains("defineInRange(\"portableFpvDetectorScanIntervalTicks\", 10"), "Default detector scan interval should be 10 ticks.");
        assertTrue(config.contains("defineInRange(\"portableFpvDetectorMaximumTargets\", 16"), "Detector should cap scan packets to 16 targets by default.");

        assertTrue(client.contains("RenderGuiOverlayEvent.Post"), "A small gameplay summary should show detector status again.");
        assertFalse(client.contains("renderCompactHud"), "The old floating detector panel should be removed.");
        assertTrue(client.contains("HUD_WIDTH = 146"), "The restored detector summary should stay compact.");
        assertTrue(client.contains("Wireless FPV signal detected"), "New detections should be reported once through the action bar.");
        assertTrue(client.contains("FPV signal lost"), "Lost signals should be reported once through the action bar.");
        assertTrue(client.contains("handleBeep"), "The controlled warning beep system should remain.");
        assertTrue(item.contains("setActive(stack, true)"), "Shift-right-click should activate the detector before the raised scan opens.");
        assertTrue(model.contains("builtin/entity"), "The native handheld mesh is rendered by the PD-01 item renderer.");
    }

    @Test
    void detectorVideoRendererUsesReadOnlyOffscreenCameraWithStateRestore() throws Exception {
        String renderer = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/PortableFpvDetectorVideoRenderer.java"));
        String client = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneJammerClient.java"));
        String context = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneClientViewContext.java"));

        assertTrue(renderer.contains("new TextureTarget"), "Video feed should render to a client-side offscreen target.");
        assertTrue(renderer.contains("minecraft.setCameraEntity(drone)"), "Video feed should temporarily view from the selected drone.");
        assertTrue(renderer.contains("minecraft.gameRenderer.renderLevel"), "Video feed should render the live world view, not a fake image.");
        assertTrue(renderer.contains("finally"), "Camera/render state restoration must be guarded by finally.");
        assertTrue(renderer.contains("mainTarget.bindWrite(true)"), "Main framebuffer should be restored after the video render.");
        assertTrue(renderer.contains("RenderSystem.restoreProjectionMatrix()"), "GUI projection should be restored after the video render.");
        assertTrue(renderer.contains("minecraft.gameRenderer.setRenderHand(false)"), "The intercepted feed must not draw the player's hand or HUD.");
        assertTrue(renderer.contains("DroneClientViewContext.beginReadOnlyCameraIntercept"), "Camera mixins should know this is read-only interception.");
        assertTrue(renderer.contains("MainRenderTargetSwap.swap"), "Forge 1.20.1 should route nested level renders into the detector texture.");
        assertTrue(renderer.contains("mainTargetSwap.close()"), "The real main framebuffer must be restored after the detector video render.");
        assertTrue(client.contains("PortableFpvDetectorVideoRenderer.render"), "The raised detector display should use the live video renderer.");
        assertTrue(context.contains("beginReadOnlyCameraIntercept"));
        assertTrue(context.contains("endReadOnlyCameraIntercept"));
    }

    @Test
    void radarConfigIsExposedInConfigScreenSecondJammerPage() throws Exception {
        String config = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonConfig.java"));
        String draft = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneConfigDraft.java"));
        String screen = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneConfigScreen.java"));

        assertTrue(config.contains("defineInRange(\"droneRadarDetectionRange\", 256"));
        assertTrue(config.contains("define(\"droneRadarBeepEnabled\", true)"));
        assertTrue(config.contains("defineInRange(\"droneRadarBeepVolume\", 0.45D"));
        assertTrue(config.contains("define(\"droneRadarOnlyBeepWhenHeld\", true)"));
        assertTrue(config.contains("define(\"debugDroneRadar\", false)"));
        assertTrue(config.contains("defineInRange(\"droneRadarHudScale\", 0.55D"));
        assertTrue(config.contains("defineInRange(\"droneRadarHudXOffset\", 8"));
        assertTrue(config.contains("defineInRange(\"droneRadarHudYOffset\", 8"));
        assertTrue(config.contains("define(\"droneRadarCompactMode\", true)"));
        assertTrue(draft.contains("droneRadarDetectionRange"));
        assertTrue(draft.contains("droneRadarBeepVolume"));
        assertTrue(draft.contains("droneRadarHudScale"));
        assertTrue(draft.contains("droneRadarCompactMode"));
        assertTrue(screen.contains("case JAMMER -> 2"));
        assertTrue(screen.contains("screen.sbwdroneconfig.config.label.radar_detection_range"));
        assertTrue(screen.contains("screen.sbwdroneconfig.config.label.radar_hud_scale"));
    }

    @Test
    void radarSoundAndTranslationsAreRegistered() throws Exception {
        String sounds = Files.readString(Path.of("src/main/resources/assets/sbwdroneconfig/sounds.json"));
        String soundRegistry = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonSounds.java"));

        assertTrue(soundRegistry.contains("DRONE_RADAR_BEEP"));
        assertTrue(sounds.contains("\"drone_radar_beep\""));
        assertTrue(sounds.contains("subtitles.sbwdroneconfig.drone_radar_beep"));

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "English language file should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertTrue(root.has("overlay.sbwdroneconfig.radar_title"));
            assertTrue(root.has("overlay.sbwdroneconfig.radar_fiber"));
            assertTrue(root.has("screen.sbwdroneconfig.config.label.radar_beep_volume"));
        }
    }
}
