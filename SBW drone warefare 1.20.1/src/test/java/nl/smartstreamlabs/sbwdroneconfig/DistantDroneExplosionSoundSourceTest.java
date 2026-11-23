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

class DistantDroneExplosionSoundSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");

    @Test
    void registersDistantDroneExplosionSoundWithFixedRange() throws Exception {
        String soundsSource = Files.readString(MAIN.resolve("AddonSounds.java"), StandardCharsets.UTF_8);

        assertTrue(soundsSource.contains("DRONE_DISTANT_EXPLOSION = SOUND_EVENTS.register("),
                "The addon should register a dedicated distant drone explosion sound event.");
        assertTrue(soundsSource.contains("SoundEvent.createFixedRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, \"drone_distant_explosion\"), 192.0F)"),
                "The distant drone explosion sound should use a fixed range so it can be heard around 100 blocks away.");
    }

    @Test
    void registersFiveThousandBlockDroneExplosionSoundWithFixedRange() throws Exception {
        String soundsSource = Files.readString(MAIN.resolve("AddonSounds.java"), StandardCharsets.UTF_8);

        assertTrue(soundsSource.contains("DRONE_5000_BLOCK_EXPLOSION = SOUND_EVENTS.register("),
                "The addon should register a dedicated 5000-block drone explosion sound event.");
        assertTrue(soundsSource.contains("SoundEvent.createFixedRangeEvent(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, \"drone_5000_block_explosion\"), 5000.0F)"),
                "The 5000-block drone explosion sound should use a fixed range so it can be heard from extremely far away.");
    }

    @Test
    void packagesTheDistantExplosionOggAndSoundsJsonEntry() throws Exception {
        ClassLoader loader = DistantDroneExplosionSoundSourceTest.class.getClassLoader();
        assertNotNull(loader.getResource("assets/sbwdroneconfig/sounds/explosion_audio/drone_distant_user.ogg"),
                "The provided afstand-explosie.ogg should be packaged under a lowercase mod sound path.");

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/sounds.json")) {
            assertNotNull(stream, "sounds.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject entry = root.getAsJsonObject("drone_distant_explosion");
            assertNotNull(entry, "sounds.json should define the distant drone explosion key.");
            assertEquals("subtitles.sbwdroneconfig.drone_distant_explosion", entry.get("subtitle").getAsString());
            assertEquals("sbwdroneconfig:explosion_audio/drone_distant_user",
                    entry.getAsJsonArray("sounds").get(0).getAsJsonObject().get("name").getAsString());
        }
    }

    @Test
    void packagesTheFiveThousandBlockExplosionOggAndSoundsJsonEntry() throws Exception {
        ClassLoader loader = DistantDroneExplosionSoundSourceTest.class.getClassLoader();
        assertNotNull(loader.getResource("assets/sbwdroneconfig/sounds/explosion_audio/drone_distant_user.ogg"),
                "The provided afstand-5000-blocks-explosie.ogg should be packaged under a lowercase mod sound path.");

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/sounds.json")) {
            assertNotNull(stream, "sounds.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject entry = root.getAsJsonObject("drone_5000_block_explosion");
            assertNotNull(entry, "sounds.json should define the 5000-block drone explosion key.");
            assertEquals("subtitles.sbwdroneconfig.drone_5000_block_explosion", entry.get("subtitle").getAsString());
            assertEquals("sbwdroneconfig:explosion_audio/drone_distant_user",
                    entry.getAsJsonArray("sounds").get(0).getAsJsonObject().get("name").getAsString());
        }
    }

    @Test
    void droneCrashExplosionPlaysTheDistantExplosionSoundForFpvAndLucasCrashes() throws Exception {
        String crashSource = Files.readString(MAIN.resolve("DroneCrashExplosionSystem.java"), StandardCharsets.UTF_8);

        assertTrue(crashSource.contains("DISTANT_EXPLOSION_SOUND_VOLUME"),
                "Drone crash effects should define a dedicated distant sound volume.");
        assertTrue(crashSource.contains("AddonSounds.DRONE_DISTANT_EXPLOSION.get()"),
                "Drone crash effects should play the custom distant explosion sound.");
        assertTrue(crashSource.contains("AddonSounds.DRONE_5000_BLOCK_EXPLOSION.get()"),
                "Drone crash effects should also play the custom 5000-block explosion sound.");
        assertTrue(crashSource.contains("playDistantExplosionSound(level, pos, fpvDrone, lucasDrone, glideBomb);"),
                "FPV, LUCAS and Glide Bomb crash effects should route through the distant sound helper with their own crash profiles.");
        assertTrue(crashSource.contains("glideBomb ? FPV_DISTANT_EXPLOSION_SOUND_VOLUME * 2.0F"),
                "The Glide Bomb should keep the distant explosion audible with its own heavier crash profile.");
    }
}
