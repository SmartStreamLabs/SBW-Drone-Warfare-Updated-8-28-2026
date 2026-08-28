package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneExternalAudioSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");

    @Test
    void clientSetupUsesUnifiedDroneAudioManagerOnly() throws Exception {
        String modSource = Files.readString(MAIN.resolve("SbwDroneRangeConfig.java"), StandardCharsets.UTF_8);

        assertTrue(modSource.contains("DroneAudioManager.init(modBus);"),
                "Client setup should register the unified external drone audio manager.");
        assertFalse(modSource.contains("DroneEngineSoundClient.init(modBus);"),
                "The old Cubed FPV engine sound hook should not be initialized beside the new manager.");
        assertFalse(modSource.contains("LucasDroneSoundManager.init(modBus);"),
                "The old LUCAS sound manager should not be initialized beside the new manager.");
    }

    @Test
    void externalAudioManagerUsesPersistentLayeredSounds() throws Exception {
        String managerSource = Files.readString(MAIN.resolve("DroneAudioManager.java"), StandardCharsets.UTF_8);

        assertTrue(managerSource.contains("ACTIVE_AUDIO"),
                "The external audio system should keep one managed sound set per drone instead of restarting sounds.");
        assertTrue(managerSource.contains("selectAndUpdate("),
                "The manager should select prioritized audible drones before updating layer sounds.");
        assertTrue(managerSource.contains("MIN_LAYER_START_VOLUME"),
                "Near/mid/far layers should not all start when their target gain is effectively zero.");
        String occlusionSource = Files.readString(MAIN.resolve("DroneSoundOcclusion.java"), StandardCharsets.UTF_8);
        assertTrue(occlusionSource.contains("UPDATE_INTERVAL_TICKS = 6"),
                "Occlusion should be sampled on a lightweight interval rather than every layer every tick.");
        assertTrue(managerSource.contains("DroneClientViewContext.isLocalFpvViewOf"),
                "Cubed FPV should detect the local internal FPV listener path.");
        assertFalse(managerSource.contains("new LucasDroneEngineSoundInstance"),
                "The old single-loop LUCAS sound instance should not be spawned by the new manager.");
    }

    @Test
    void registersNearMidFarSoundEventsAndProfiles() throws Exception {
        String soundSource = Files.readString(MAIN.resolve("AddonSounds.java"), StandardCharsets.UTF_8);
        String profileSource = Files.readString(MAIN.resolve("DroneAudioProfile.java"), StandardCharsets.UTF_8);

        assertTrue(soundSource.contains("CUBED_FPV_DRONE_ENGINE_NEAR"));
        assertTrue(soundSource.contains("CUBED_FPV_DRONE_ENGINE_MID"));
        assertTrue(soundSource.contains("CUBED_FPV_DRONE_ENGINE_FAR"));
        assertTrue(soundSource.contains("LUCAS_DRONE_ENGINE_NEAR"));
        assertTrue(soundSource.contains("LUCAS_DRONE_ENGINE_MID"));
        assertTrue(soundSource.contains("LUCAS_DRONE_ENGINE_FAR"));
        assertTrue(profileSource.contains("160.0D"),
                "Cubed FPV external audio should fade out at 160 blocks.");
        assertTrue(profileSource.contains("220.0D"),
                "LUCAS external audio should fade out at 220 blocks.");
        assertTrue(profileSource.contains("AddonConfig.fpvInternalAudioGain()"),
                "Internal Cubed FPV listeners should use the configured quieter gain.");
    }

    @Test
    void exposesRequestedAudioConfigDefaults() throws Exception {
        String configSource = Files.readString(MAIN.resolve("AddonConfig.java"), StandardCharsets.UTF_8);

        assertTrue(configSource.contains("\"cubedFpvExternalVolume\", 1.0D"));
        assertTrue(configSource.contains("\"cubedFpvNearVolume\", 1.0D"));
        assertTrue(configSource.contains("\"cubedFpvMidVolume\", 0.8D"));
        assertTrue(configSource.contains("\"cubedFpvFarVolume\", 0.55D"));
        assertTrue(configSource.contains("\"lucasExternalVolume\", 1.1D"));
        assertTrue(configSource.contains("\"lucasNearVolume\", 1.0D"));
        assertTrue(configSource.contains("\"lucasMidVolume\", 0.85D"));
        assertTrue(configSource.contains("\"lucasFarVolume\", 0.60D"));
        assertTrue(configSource.contains("\"fpvInternalAudioGain\", 0.65D"));
        assertTrue(configSource.contains("\"droneDopplerEnabled\", true"));
        assertTrue(configSource.contains("\"droneDopplerStrength\", 0.20D"));
        assertTrue(configSource.contains("\"droneAudioOcclusion\", true"));
        assertTrue(configSource.contains("\"maxAudibleCubedFpvDrones\", 12"));
        assertTrue(configSource.contains("\"maxAudibleLucasDrones\", 8"));
    }

    @Test
    void soundsJsonDefinesLayeredEntries() throws Exception {
        try (var stream = DroneExternalAudioSourceTest.class.getClassLoader()
                .getResourceAsStream("assets/sbwdroneconfig/sounds.json")) {
            assertNotNull(stream, "sounds.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertNotNull(root.getAsJsonObject("cubed_fpv_drone_engine_near"));
            assertNotNull(root.getAsJsonObject("cubed_fpv_drone_engine_mid"));
            assertNotNull(root.getAsJsonObject("cubed_fpv_drone_engine_far"));
            assertNotNull(root.getAsJsonObject("lucas_drone_engine_near"));
            assertNotNull(root.getAsJsonObject("lucas_drone_engine_mid"));
            assertNotNull(root.getAsJsonObject("lucas_drone_engine_far"));
        }
    }
}
