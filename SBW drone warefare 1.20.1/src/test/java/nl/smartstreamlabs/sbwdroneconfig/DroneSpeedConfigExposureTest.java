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

class DroneSpeedConfigExposureTest {
    @Test
    void fpvConfigDraftCarriesDroneSpeedMultiplierState() throws Exception {
        assertNotNull(DroneConfigDraft.class.getDeclaredField("droneSpeedMultiplier"));
    }

    @Test
    void englishTranslationsExposeDroneSpeedGuiLabels() throws Exception {
        ClassLoader loader = DroneSpeedConfigExposureTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals(
                    "FPV Drone speed, audio, screenshot sound, and Fiber Optic link options in one dedicated panel.",
                    root.get("screen.sbwdroneconfig.config.description.fpv_drone").getAsString()
            );
            assertEquals(
                    "FPV Drone Flight Speed",
                    root.get("screen.sbwdroneconfig.config.label.drone_speed").getAsString()
            );
        }
    }

    @Test
    void fpvConfigScreenWiresTheDroneSpeedSlider() throws Exception {
        Path screenSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DroneConfigScreen.java");
        String source = Files.readString(screenSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("screen.sbwdroneconfig.config.label.drone_speed"));
        assertTrue(source.contains("draft.droneSpeedMultiplier"));
    }
}
