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

class FiberOpticConfigGuiExposureTest {
    @Test
    void fpvConfigDraftCarriesFiberOpticToggleState() throws Exception {
        assertNotNull(DroneConfigDraft.class.getDeclaredField("enableFiberOpticMode"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("antiDroneNetCutsFiber"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("fiberOpticCableLength"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("fiberOpticCableBreakDelayTicks"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("fiberOpticExtraBatteryDrainMultiplier"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("normalJammersAffectFiberOptic"));
        assertNotNull(DroneConfigDraft.class.getDeclaredField("debugFiberOptic"));
    }

    @Test
    void englishTranslationsExposeFiberOpticGuiLabels() throws Exception {
        ClassLoader loader = FiberOpticConfigGuiExposureTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("FPV Drone speed, audio, screenshot sound, and Fiber Optic link options in one dedicated panel.",
                    root.get("screen.sbwdroneconfig.config.description.fpv_drone").getAsString());
            assertEquals("Fiber Optic Mode", root.get("screen.sbwdroneconfig.config.label.fiber_optic_enabled").getAsString());
            assertEquals("Anti-Drone Net Cuts Fiber", root.get("screen.sbwdroneconfig.config.label.anti_drone_net_cuts_fiber").getAsString());
            assertEquals("Fiber Cable Length", root.get("screen.sbwdroneconfig.config.label.fiber_optic_cable_length").getAsString());
            assertEquals("Fiber Break Delay", root.get("screen.sbwdroneconfig.config.label.fiber_optic_break_delay").getAsString());
            assertEquals("Fiber Battery Drain", root.get("screen.sbwdroneconfig.config.label.fiber_optic_battery_drain").getAsString());
            assertEquals("Normal Jammers Affect Fiber", root.get("screen.sbwdroneconfig.config.label.normal_jammers_affect_fiber").getAsString());
            assertEquals("Debug Fiber Logs", root.get("screen.sbwdroneconfig.config.label.debug_fiber").getAsString());
        }
    }

    @Test
    void fpvConfigScreenWiresTheFiberOpticToggles() throws Exception {
        Path screenSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DroneConfigScreen.java");
        String source = Files.readString(screenSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("screen.sbwdroneconfig.config.label.fiber_optic_enabled"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.anti_drone_net_cuts_fiber"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.fiber_optic_cable_length"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.fiber_optic_break_delay"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.fiber_optic_battery_drain"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.normal_jammers_affect_fiber"));
        assertTrue(source.contains("screen.sbwdroneconfig.config.label.debug_fiber"));
        assertTrue(source.contains("draft.enableFiberOpticMode"));
        assertTrue(source.contains("draft.antiDroneNetCutsFiber"));
        assertTrue(source.contains("draft.fiberOpticCableLength"));
        assertTrue(source.contains("draft.fiberOpticCableBreakDelayTicks"));
        assertTrue(source.contains("draft.fiberOpticExtraBatteryDrainMultiplier"));
        assertTrue(source.contains("draft.normalJammersAffectFiberOptic"));
        assertTrue(source.contains("draft.debugFiberOptic"));
    }
}
