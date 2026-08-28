package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableSeveredDescentSourceTest {
    @Test
    void severedCableDisconnectMarksForcedDescent() throws Exception {
        Path linkSystemSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticLinkSystem.java"
        );
        String source = Files.readString(linkSystemSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("TAG_FORCE_DESCENT_AFTER_SEVER"),
                "Fiber cable sever handling should keep a dedicated forced-descent flag so a severed FPV drone cannot keep hovering in place.");
        assertTrue(
                Pattern.compile("disconnectFiberLink\\(.*?drone\\.getPersistentData\\(\\)\\.putBoolean\\(TAG_FORCE_DESCENT_AFTER_SEVER, true\\);", Pattern.DOTALL)
                        .matcher(source)
                        .find(),
                "Disconnecting a fiber link should mark the drone for forced descent immediately after the cable breaks."
        );
    }

    @Test
    void forcedDescentTravelHookCutsPowerAndPushesDownward() throws Exception {
        Path linkSystemSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticLinkSystem.java"
        );
        String source = Files.readString(linkSystemSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("public static void beforeDroneTravel(Entity drone)"),
                "Fiber optic logic should participate in the drone travel hook so a severed cable can immediately change flight behavior.");
        assertTrue(source.contains("invokeBooleanSetter(drone, \"setDownInputDown\", true)"),
                "A severed cable should hold the drone in a forced descent state instead of leaving all inputs neutral.");
        assertTrue(source.contains("invokeFloatSetter(drone, \"setPower\", 0.0F)"),
                "Forced descent should explicitly shut down the drone motor power so SBW's POWER floor no longer keeps it hovering.");
        assertTrue(source.contains("Math.min(drone.getDeltaMovement().y, -0.12D)"),
                "Forced descent should inject a real downward velocity so the drone falls out of the sky after the cable is severed.");
    }
}
