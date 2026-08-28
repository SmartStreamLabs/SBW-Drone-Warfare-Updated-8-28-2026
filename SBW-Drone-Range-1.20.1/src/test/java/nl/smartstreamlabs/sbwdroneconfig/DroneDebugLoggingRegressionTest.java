package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DroneDebugLoggingRegressionTest {
    @Test
    void droneEnergyMixinDoesNotContainAlwaysOnDroneDebugSpam() throws IOException {
        Path mixinSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "mixin",
                "DroneEnergyMixin.java"
        );

        String source = Files.readString(mixinSource);
        assertFalse(source.contains("[DroneDebug]"), "DroneEnergyMixin should not contain the always-on DroneDebug log marker.");
        assertFalse(source.contains("sbwdroneconfig$logDroneDebug(drone);"), "DroneEnergyMixin should not call the old per-tick drone debug logger.");
    }
}
