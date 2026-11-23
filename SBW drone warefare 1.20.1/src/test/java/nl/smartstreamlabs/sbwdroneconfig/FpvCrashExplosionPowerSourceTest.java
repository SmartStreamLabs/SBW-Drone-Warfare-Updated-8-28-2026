package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FpvCrashExplosionPowerSourceTest {
    @Test
    void cubedFpvUsesDedicatedStrongCrashExplosionProfile() throws Exception {
        Path crashSystem = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "DroneCrashExplosionSystem.java"
        );
        String source = Files.readString(crashSystem, StandardCharsets.UTF_8);

        assertTrue(source.contains("FPV_CRASH_EXPLOSION_POWER"),
                "Cubed FPV should have its own crash power instead of using the weaker base drone explosion.");
        assertTrue(source.contains("drone instanceof CubedFpvDroneEntity"),
                "The stronger FPV explosion must only target the addon FPV drone.");
        assertTrue(source.contains("fpvDrone ? FPV_CRASH_EXPLOSION_POWER"),
                "Explosion power selection should route Cubed FPV through the stronger FPV profile.");
        assertTrue(source.contains("FPV_CRASH_SOUND_VOLUME"),
                "Cubed FPV should also get a louder nearby crash sound.");
        assertTrue(source.contains("FPV_DISTANT_EXPLOSION_SOUND_VOLUME"),
                "Cubed FPV should get a stronger distant explosion sound without changing the base profile.");
    }
}
