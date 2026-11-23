package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasFlightConfigExposureTest {
    @Test
    void draftCarriesLucasFlightState() throws Exception {
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasTakeoffAssistTicks"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasTakeoffForwardBoost"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasTakeoffUpBoost"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasMinSpeed"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasMaxSpeed"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasAcceleration"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasDrag"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasTurnRate"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasPitchRate"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasClimbSpeed"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasDescendSpeed"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasLiftStrength"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasRollVisualAmount"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasMinLiftSpeed"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasStallGravity"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasStallEnabled"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasCanHover"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("lucasEngineVolumeMultiplier"));
    }

    @Test
    void englishTranslationsExposeLucasFlightHudAndConfigLabels() throws Exception {
        try (var stream = LucasFlightConfigExposureTest.class.getClassLoader()
                .getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "The English language file should exist.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();

            assertEquals("LUCAS Takeoff Assist", root.get("screen.sbwdroneconfig.config.label.lucas_takeoff_assist_ticks").getAsString());
            assertEquals("LUCAS Takeoff Up Boost", root.get("screen.sbwdroneconfig.config.label.lucas_takeoff_up_boost").getAsString());
            assertEquals("LUCAS Climb Speed", root.get("screen.sbwdroneconfig.config.label.lucas_climb_speed").getAsString());
            assertEquals("LUCAS Stall Enabled", root.get("screen.sbwdroneconfig.config.label.lucas_stall_enabled").getAsString());
            assertEquals("LUCAS Engine Volume", root.get("screen.sbwdroneconfig.config.label.lucas_engine_volume").getAsString());
            assertEquals("LUCAS Drone Inventory", root.get("screen.sbwdroneconfig.lucas_drone_inventory").getAsString());
            assertEquals("LUCAS Cruise Speed", root.get("screen.sbwdroneconfig.config.label.lucas_min_speed").getAsString());
            assertEquals("LUCAS Max Speed", root.get("screen.sbwdroneconfig.config.label.lucas_max_speed").getAsString());
            assertEquals("LINK: WIRELESS", root.get("overlay.sbwdroneconfig.link_wireless").getAsString());
            assertEquals("STALL: %s", root.get("overlay.sbwdroneconfig.lucas_stall").getAsString());
        }
    }

    @Test
    void lucasDefaultsFavorTheMuchFasterPreset() {
        String source;
        try {
            source = Files.readString(
                    Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonConfig.java"),
                    StandardCharsets.UTF_8
            );
        } catch (Exception exception) {
            throw new AssertionError("The LUCAS config source should be readable.", exception);
        }

        assertTrue(source.contains(".defineInRange(\"lucasTakeoffForwardBoost\", 1.15D"),
                "The stronger LUCAS preset should further raise the takeoff forward boost default.");
        assertTrue(source.contains(".defineInRange(\"lucasMinSpeed\", 0.85D"),
                "The faster LUCAS preset should raise the cruise speed default.");
        assertTrue(source.contains(".defineInRange(\"lucasMaxSpeed\", 3.20D"),
                "The faster LUCAS preset should raise the top speed default.");
        assertTrue(source.contains(".defineInRange(\"lucasAcceleration\", 0.085D"),
                "The faster LUCAS preset should raise the acceleration default.");
        assertTrue(source.contains(".defineInRange(\"lucasEngineVolumeMultiplier\", 5.0D"),
                "The LUCAS audio config should default to a very loud engine multiplier while still capping at 500%.");
    }

    @Test
    void configScreenWiresTheArcadeLucasControls() throws Exception {
        String source = Files.readString(
                Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DroneConfigScreen.java"),
                StandardCharsets.UTF_8
        );

        assertTrue(source.contains("screen.sbwdroneconfig.config.label.lucas_takeoff_assist_ticks"));
        assertTrue(source.contains("draft.lucasTakeoffAssistTicks"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.lucas_climb_speed"));
        assertTrue(source.contains("draft.lucasClimbSpeed"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.lucas_stall_enabled"));
        assertTrue(source.contains("draft.lucasStallEnabled"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.lucas_engine_volume"));
        assertTrue(source.contains("draft.lucasEngineVolumeMultiplier"));
    }
}
