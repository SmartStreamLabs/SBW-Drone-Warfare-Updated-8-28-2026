package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticDroneSelfCollisionSourceTest {
    @Test
    void mixinRegistrationIncludesFiberCableCollisionGuard() throws Exception {
        Path mixinConfig = Path.of("src", "main", "resources", "sbwdroneconfig.mixins.json");
        String source = Files.readString(mixinConfig, StandardCharsets.UTF_8);

        assertTrue(source.contains("\"DroneEntityFiberCableCollisionMixin\""),
                "The fiber optic drone self-collision guard must stay registered so SBW drones stop treating cable segments as crash targets.");
    }

    @Test
    void crashGuardCancelsHitEntityCrashForFiberCableSegments() throws Exception {
        Path mixinSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "mixin",
                "DroneEntityFiberCableCollisionMixin.java"
        );
        String source = Files.readString(mixinSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("@Mixin(targets = \"com.atsuishio.superbwarfare.entity.vehicle.DroneEntity\")"),
                "The self-collision guard must patch the SBW DroneEntity directly.");
        assertTrue(source.contains("method = \"hitEntityCrash\""),
                "The guard must intercept SBW's entity crash path where the drone damages itself after touching nearby entities.");
        assertTrue(
                Pattern.compile("if \\(target instanceof FiberOpticCableSegmentEntity\\) \\{\\s*ci\\.cancel\\(\\);\\s*\\}", Pattern.DOTALL).matcher(source).find(),
                "Fiber cable segments must be excluded from DroneEntity hitEntityCrash so the drone cannot sever itself by touching its own cable."
        );
    }
}
