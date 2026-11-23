package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasUavHudRedesignSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");
    private static final Path LANG = Path.of("src/main/resources/assets/sbwdroneconfig/lang/en_us.json");

    @Test
    void lucasHudUsesProfessionalUavLayoutInsteadOfDebugText() throws Exception {
        String source = Files.readString(MAIN.resolve("LucasDroneHudOverlay.java"));

        assertTrue(source.contains("renderTelemetryPanel"));
        assertTrue(source.contains("renderHeadingCompass"));
        assertTrue(source.contains("renderStatusPanel"));
        assertTrue(source.contains("renderTargetingReticle"));
        assertTrue(source.contains("renderLinkPanel"));
        assertTrue(source.contains("renderMissionPanel"));
        assertTrue(source.contains("renderSystemPanel"));
        assertTrue(source.contains("renderCameraEffects"));
        assertTrue(source.contains("MODE: MANUAL"));
        assertTrue(source.contains("CAM:"));
        assertTrue(source.contains("ZOOM:"));
        assertTrue(source.contains("GPS: ACTIVE"));
        assertTrue(source.contains("HEALTH"));
        assertTrue(source.contains("DISTANCE TO TARGET"));
        assertTrue(source.contains("REC"));

        assertFalse(source.contains("int x = 10;\n        int y = 60;"),
                "The LUCAS HUD should no longer be a basic debug text column.");
    }

    @Test
    void lucasHudScaleIsConfigurableAndExposedInGui() throws Exception {
        String config = Files.readString(MAIN.resolve("AddonConfig.java"));
        String draft = Files.readString(MAIN.resolve("DroneConfigDraft.java"));
        String screen = Files.readString(MAIN.resolve("DroneConfigScreen.java"));
        String lang = Files.readString(LANG);

        assertTrue(config.contains("LUCAS_HUD_SCALE_VALUE"));
        assertTrue(config.contains("defineInRange(\"lucasHudScale\", 1.0D, 0.60D, 1.50D)"));
        assertTrue(config.contains("lucasHudScale()"));
        assertTrue(draft.contains("lucasHudScale"));
        assertTrue(screen.contains("screen.sbwdroneconfig.config.label.lucas_hud_scale"));
        assertTrue(lang.contains("screen.sbwdroneconfig.config.label.lucas_hud_scale"));
    }

    @Test
    void cubedFpvHudRemainsSeparate() throws Exception {
        String lucas = Files.readString(MAIN.resolve("LucasDroneHudOverlay.java"));
        String cubedOverlay = Files.readString(MAIN.resolve("CubedFpvReferenceGuiOverlay.java"));
        String cubedTelemetry = Files.readString(MAIN.resolve("CubedFpvReferenceGuiTelemetry.java"));

        assertFalse(lucas.contains("CubedFpvDroneEntity"));
        assertFalse(lucas.contains("LINK: FIBER"));
        assertTrue(cubedOverlay.contains("CubedFpvReferenceGuiTelemetry"));
        assertTrue(cubedTelemetry.contains("F I B E R"));
        assertTrue(cubedTelemetry.contains("%.1fV"));
        assertFalse(cubedOverlay.contains("MODE: MANUAL"));
        assertFalse(cubedOverlay.contains("DISTANCE TO TARGET"));
    }
}
