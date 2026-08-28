package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneSpeedSystemSourceTest {
    @Test
    void droneSpeedSystemClassExistsWithTravelHook() {
        assertDoesNotThrow(
                () -> Class.forName("nl.smartstreamlabs.sbwdroneconfig.DroneSpeedSystem"),
                "DroneSpeedSystem should exist so the FPV Drone speed slider can affect real in-game movement."
        );
    }

    @Test
    void droneSpeedSystemUsesConfiguredMultiplierAndRealDroneInputs() throws Exception {
        Path systemSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DroneSpeedSystem.java");
        String source = Files.readString(systemSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("AddonConfig.droneSpeedMultiplier()"));
        assertTrue(source.contains("VehicleEntity.DELTA_ROT"));
        assertTrue(source.contains("VehicleEntity.POWER"));
        assertTrue(source.contains("forwardInputDown()"));
        assertTrue(source.contains("upInputDown()"));
    }

    @Test
    void droneTravelMixinInvokesTheSpeedSystem() throws Exception {
        Path mixinSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "mixin", "DroneEnergyMixin.java");
        String source = Files.readString(mixinSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("DroneSpeedSystem.afterDroneTravel(drone);"));
    }
}
