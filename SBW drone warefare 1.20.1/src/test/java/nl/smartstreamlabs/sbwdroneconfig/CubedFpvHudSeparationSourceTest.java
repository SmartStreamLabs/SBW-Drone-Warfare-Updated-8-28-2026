package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CubedFpvHudSeparationSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");
    private static final Path MIXIN = MAIN.resolve("mixin/DroneHudOverlayMixin.java");
    private static final Path LANG = Path.of("src/main/resources/assets/sbwdroneconfig/lang/en_us.json");

    @Test
    void cubedFpvHasDedicatedReferenceStyleGuiAndDoesNotRenderOldHudLabels() throws Exception {
        String overlay = Files.readString(MAIN.resolve("CubedFpvReferenceGuiOverlay.java"));
        String telemetry = Files.readString(MAIN.resolve("CubedFpvReferenceGuiTelemetry.java"));

        assertTrue(overlay.contains("RenderGuiEvent.Post"), "Cubed FPV interface should render through a GUI post hook.");
        assertTrue(overlay.contains("RenderGuiOverlayEvent.Pre"), "Cubed FPV interface should suppress selected vanilla overlays.");
        assertTrue(overlay.contains("RenderHandEvent"), "Cubed FPV interface should suppress hands only while active.");
        assertTrue(telemetry.contains("%.1fV"), "Battery voltage should be formatted only when a synced voltage exists.");
        assertTrue(telemetry.contains("LQ %03d"), "Link quality should be sourced from jammer sync state.");
        assertTrue(telemetry.contains("RSSI --"), "RSSI row should stay visible with a placeholder when no real RSSI source exists.");
        assertTrue(telemetry.contains("LAT --"), "Latency row should stay visible with a placeholder when no real latency source exists.");
        assertTrue(telemetry.contains("S %03d"), "FPV OSD should show horizontal speed with the strict one-letter label.");
        assertTrue(telemetry.contains("H %03d"), "FPV OSD should show altitude with the strict one-letter label.");
        assertTrue(telemetry.contains("A %.1f\\u00B0"), "FPV OSD should show actual camera/body pitch as the compact angle row.");
        assertTrue(telemetry.contains("--.-V"), "Voltage row should stay visible when battery sync is unavailable.");
        assertTrue(telemetry.contains("--%"), "Secondary battery row should stay visible when percent sync is unavailable.");
        assertTrue(telemetry.contains("A C R O"), "Cubed FPV should report its current compact flight mode label.");
        assertTrue(telemetry.contains("C U B E D   F P V"), "Cubed FPV should have the final spaced fallback callsign.");

        assertFalse(overlay.contains("RenderLevelStageEvent"), "New FPV interface must not render through world-space level events.");
        assertFalse(overlay.contains("REC"), "Cubed FPV interface must not show a fake recording indicator.");
        assertFalse(telemetry.contains("SPD %03d"), "Cubed FPV interface should not keep the rejected SPD label.");
        assertFalse(telemetry.contains("ALT %03d"), "Cubed FPV interface should not keep the rejected ALT label.");
        assertFalse(telemetry.contains("V/S %+.1f"), "Cubed FPV interface should not keep the rejected V/S label.");
        assertFalse(telemetry.contains("V %+.1f"), "Cubed FPV interface should not keep the rejected vertical speed row.");
        assertFalse(telemetry.contains("DIST:"), "Cubed FPV interface should not restore the old bottom-corner distance line.");
        assertFalse(telemetry.contains("HDG:"), "Cubed FPV interface should not duplicate heading outside the compass.");
        assertFalse(overlay.contains("HEALTH"), "Cubed FPV HUD must not show military health labels.");
        assertFalse(overlay.contains("AMMO"), "Cubed FPV HUD must not show military ammo labels.");
        assertFalse(overlay.contains("TARGET RANGE"), "Cubed FPV HUD must not show military target range.");
        assertFalse(overlay.contains("ZOOM"), "Cubed FPV HUD must not show UAV zoom labels.");
    }

    @Test
    void defaultSbwHudIsCancelledOnlyForCubedFpvDrone() throws Exception {
        String mixin = Files.readString(MIXIN);

        assertTrue(mixin.contains("sbwdroneconfig$replaceCubedFpvHud"));
        assertTrue(mixin.contains("CubedFpvReferenceGuiOverlay.shouldReplaceDefaultSbwHud()"));
        assertTrue(mixin.contains("ci.cancel()"));
    }

    @Test
    void lucasKeepsSeparateUavHudAddition() throws Exception {
        String source = Files.readString(MAIN.resolve("LucasDroneHudOverlay.java"));

        assertTrue(source.contains("LucasDroneEntity"));
        assertTrue(source.contains("FUEL:"));
        assertTrue(source.contains("SPD:"));
        assertTrue(source.contains("ALT:"));
        assertFalse(source.contains("BAT:"), "LUCAS HUD should not reuse the Cubed FPV battery OSD.");
    }

    @Test
    void fpvCameraOptionsAreRegisteredInConfigAndGui() throws Exception {
        String config = Files.readString(MAIN.resolve("AddonConfig.java"));
        String draft = Files.readString(MAIN.resolve("DroneConfigDraft.java"));
        String screen = Files.readString(MAIN.resolve("DroneConfigScreen.java"));
        String lang = Files.readString(LANG);
        String bootstrap = Files.readString(MAIN.resolve("SbwDroneRangeConfig.java"));

        assertTrue(config.contains("FPV_CAMERA_FOV_VALUE"));
        assertTrue(config.contains("defineInRange(\"fpvCameraFov\", CubedFpvCameraViewMath.DEFAULT_FPV_FOV, CubedFpvCameraViewMath.MIN_FPV_FOV, CubedFpvCameraViewMath.MAX_FPV_FOV)"));
        assertTrue(config.contains("FPV_CAMERA_TILT_DEGREES_VALUE"));
        assertTrue(config.contains("defineInRange(\"fpvCameraTiltDegrees\", 20.0D"));
        assertTrue(config.contains("ENABLE_FPV_FISHEYE_EFFECT_VALUE"));
        assertTrue(config.contains("ENABLE_FPV_CAMERA_VIBRATION_VALUE"));
        assertTrue(draft.contains("fpvCameraFov"));
        assertTrue(screen.contains("addFpvCameraPage"));
        assertTrue(screen.contains("case FPV_DRONE -> 2"));
        assertTrue(lang.contains("screen.sbwdroneconfig.config.label.fpv_camera_fov"));
        assertTrue(bootstrap.contains("CubedFpvReferenceGuiOverlay.init(modBus);"));
        assertTrue(bootstrap.contains("CubedFpvCameraEffectsClient.init(modBus);"));
        assertTrue(bootstrap.contains("LucasDroneHudOverlay.init(modBus);"));
        assertFalse(bootstrap.contains("CubedFpvHudOverlay.init(modBus);"));
        assertFalse(bootstrap.contains("CubedFpvViewModelRenderer.init(modBus);"));
    }

    @Test
    void cubedFpvSelfViewUsesRendererSuppressionAndScreenSpaceReferenceGui() throws Exception {
        String mixinConfig = Files.readString(Path.of("src/main/resources/sbwdroneconfig.mixins.json"));
        String context = Files.readString(MAIN.resolve("DroneClientViewContext.java"));
        String renderer = Files.readString(MAIN.resolve("CubedFpvDroneRenderer.java"));
        String overlay = Files.readString(MAIN.resolve("CubedFpvReferenceGuiOverlay.java"));
        String layout = Files.readString(MAIN.resolve("CubedFpvReferenceGuiLayout.java"));
        String frame = Files.readString(MAIN.resolve("CubedFpvReferenceGuiFrameRenderer.java"));
        String text = Files.readString(MAIN.resolve("CubedFpvReferenceGuiTextRenderer.java"));
        String bootstrap = Files.readString(MAIN.resolve("SbwDroneRangeConfig.java"));

        assertTrue(mixinConfig.contains("CubedFpvCameraMixin"));
        assertTrue(mixinConfig.contains("DroneThirdPersonCameraMixin"));
        assertTrue(Files.readString(MAIN.resolve("mixin/CubedFpvCameraMixin.java")).contains("activeCubedFpvDrone(minecraft)"));
        assertTrue(Files.readString(MAIN.resolve("mixin/DroneThirdPersonCameraMixin.java")).contains("activeCubedFpvDrone(minecraft)"));
        assertTrue(context.contains("isLocalFpvViewOf(CubedFpvDroneEntity drone)"));
        assertFalse(context.contains("CameraType"), "The local FPV self-view predicate must not depend on Minecraft camera type.");
        assertTrue(renderer.contains("if (DroneClientViewContext.isLocalFpvViewOf(entity))"));
        assertTrue(renderer.contains("return;"));
        assertTrue(bootstrap.contains("CubedFpvReferenceGuiOverlay.init(modBus);"));
        assertTrue(overlay.contains("MinecraftForge.EVENT_BUS.register(CubedFpvReferenceGuiOverlay.class)"));
        assertTrue(overlay.contains("DroneClientViewContext.isLocalFpvViewOf"));
        assertTrue(layout.contains("CANVAS_WIDTH = 1080.0F"));
        assertTrue(layout.contains("CANVAS_HEIGHT = 607.0F"));
        assertTrue(layout.contains("float sx = screenWidth / CANVAS_WIDTH"));
        assertTrue(layout.contains("float sy = screenHeight / CANVAS_HEIGHT"));
        assertTrue(layout.contains("Math.min(sx, sy)"));
        assertTrue(overlay.contains("COMPASS_CENTER_X = 550.0F"));
        assertTrue(overlay.contains("COMPASS_Y = 90.0F"));
        assertTrue(overlay.contains("CALLSIGN_BASELINE_Y = 146.0F"));
        assertTrue(overlay.contains("STATUS_X = 185.0F"));
        assertTrue(overlay.contains("STATUS_Y = 125.0F"));
        assertTrue(overlay.contains("LINK_X = 740.0F"));
        assertTrue(overlay.contains("LINK_Y = 90.0F"));
        assertTrue(overlay.contains("LINK_SPACING = 17.0F"));
        assertTrue(overlay.contains("SPEED_Y = 280.0F"));
        assertTrue(overlay.contains("ALTITUDE_Y = 350.0F"));
        assertTrue(overlay.contains("ANGLE_Y = 390.0F"));
        assertTrue(overlay.contains("BATTERY_Y = 430.0F"));
        assertTrue(overlay.contains("SECONDARY_BATTERY_Y = 468.0F"));
        assertTrue(overlay.contains("RETICLE_X = 560.0F"));
        assertTrue(overlay.contains("RETICLE_Y = 314.0F"));
        assertTrue(overlay.contains("MODE_Y = 437.0F"));
        assertTrue(overlay.contains("SECONDARY_MODE_X = 470.0F"));
        assertTrue(overlay.contains("SECONDARY_MODE_Y = 476.0F"));
        assertTrue(overlay.contains("setOverlayMessage(Component.empty(), false)"));
        assertTrue(overlay.contains("case 270 -> \"W\""));
        assertFalse(overlay.contains("case 315 -> \"NW\""));
        assertTrue(overlay.contains("renderTinySignalGlyph"));
        assertTrue(frame.contains("{0.0F, 188.0F}"));
        assertTrue(frame.contains("{405.0F, 314.0F}"));
        assertTrue(frame.contains("{385.0F, 350.0F}"));
        assertTrue(frame.contains("{400.0F, 320.0F}"));
        assertTrue(frame.contains("ARM_OUTLINE = 0xFF151719"));
        assertTrue(frame.contains("ARM_MAIN = 0xFF2C2F33"));
        assertTrue(frame.contains("ARM_SECONDARY = 0xFF35393D"));
        assertTrue(frame.contains("ARM_BEVEL = 0xFF555B60"));
        assertTrue(frame.contains("ARM_EDGE = 0xFF686E72"));
        assertTrue(frame.contains("ARM_SEAM = 0xFF1D2023"));
        assertTrue(frame.contains("LEFT_ARM_LOWER_BEVEL"));
        assertTrue(frame.contains("LEFT_ARM_PANEL_SEAM"));
        assertFalse(frame.contains("{458.0F, 314.0F}"));
        assertFalse(frame.contains("{435.0F, 354.0F}"));
        assertFalse(frame.contains("{449.0F, 320.0F}"));
        assertFalse(frame.contains("{428.0F, 341.0F}"));
        assertFalse(frame.contains("{438.0F, 325.0F}"));
        assertFalse(frame.contains("{426.0F, 334.0F}"));
        assertFalse(frame.contains("ARM_HIGHLIGHT"));
        assertFalse(frame.contains("0xCC697177"));
        assertTrue(frame.contains("{390.0F, 607.0F}"));
        assertTrue(frame.contains("{540.0F, 445.0F}"));
        assertTrue(frame.contains("side(LEFT_ARM_BASE, right)"));
        assertTrue(frame.contains("right ? mirror(points) : points"));
        assertTrue(frame.contains("Tesselator"));
        assertTrue(frame.contains("VertexFormat.Mode.TRIANGLES"));
        assertTrue(text.contains("drawOutlinedCharacter"));
        assertFalse(frame.contains("RenderLevelStageEvent"));
        assertFalse(frame.contains("ResourceLocation"), "The new frame must not embed a reference texture.");
        assertFalse(Files.exists(MAIN.resolve("CubedFpvHudOverlay.java")), "Old Cubed FPV HUD class should be removed.");
        assertFalse(Files.exists(MAIN.resolve("CubedFpvViewModelRenderer.java")), "Old world-space FPV renderer should be removed.");
        assertFalse(Files.exists(MAIN.resolve("CubedFpvViewModel.java")), "Old world-space FPV model should be removed.");
        assertFalse(Files.exists(MAIN.resolve("CubedFpvViewModelTransform.java")), "Old viewmodel transform constants should be removed.");
    }

    @Test
    void fpvCameraEffectsUseForgeViewportEvents() throws Exception {
        String source = Files.readString(MAIN.resolve("CubedFpvCameraEffectsClient.java"));

        assertTrue(source.contains("ViewportEvent.ComputeFov"));
        assertTrue(source.contains("RegisterKeyMappingsEvent"));
        assertTrue(source.contains("key.sbwdroneconfig.zoom_fpv_camera"));
        assertTrue(source.contains("GLFW.GLFW_KEY_C"));
        assertTrue(source.contains("FPV_ZOOM_FOV = 45.0D"));
        assertTrue(source.contains("ZOOM_FPV_CAMERA.isDown()"));
        assertTrue(source.contains("setFOV(currentFpvFov())"));
        assertTrue(source.contains("ViewportEvent.ComputeCameraAngles"));
        assertTrue(source.contains("AddonConfig.fpvCameraTiltDegrees()"));
    }
}
