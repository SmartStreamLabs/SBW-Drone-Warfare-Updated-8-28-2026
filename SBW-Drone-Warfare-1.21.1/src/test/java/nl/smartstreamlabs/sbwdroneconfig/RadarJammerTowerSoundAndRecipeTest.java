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

class RadarJammerTowerSoundAndRecipeTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");
    private static final Path RECIPES = Path.of("src/main/resources/data/sbwdroneconfig/recipes");

    @Test
    void registersDedicatedRadarJammerTowerSoundEvent() throws Exception {
        String source = Files.readString(MAIN.resolve("AddonSounds.java"), StandardCharsets.UTF_8);

        assertTrue(source.contains("RADAR_JAMMER_TOWER_LOOP"),
                "Radar Jammer Tower should have its own sound event instead of reusing random sounds directly.");
        assertTrue(source.contains("\"radar_jammer_tower_loop\""),
                "The sound event id should be stable for resource packs and logs.");
    }

    @Test
    void radarJammerTowerPlaysSoundFromItsActiveServerTick() throws Exception {
        String jammerSource = Files.readString(MAIN.resolve("JammerBlockEntity.java"), StandardCharsets.UTF_8);
        String towerSource = Files.readString(MAIN.resolve("RadarJammerTowerBlockEntity.java"), StandardCharsets.UTF_8);

        assertTrue(jammerSource.contains("playActiveJammerSound(serverLevel, pos, gameTime)"),
                "The shared jammer server tick should expose a sound hook after active scanning.");
        assertTrue(towerSource.contains("playActiveJammerSound"),
                "Only the Radar Jammer Tower should override the sound hook.");
        assertTrue(towerSource.contains("AddonSounds.RADAR_JAMMER_TOWER_LOOP.get()"),
                "The tower should play its dedicated radar loop event.");
    }

    @Test
    void soundsJsonRoutesTowerLoopToRadarAudio() throws Exception {
        try (var stream = RadarJammerTowerSoundAndRecipeTest.class.getClassLoader().getResourceAsStream("assets/sbwdroneconfig/sounds.json")) {
            assertNotNull(stream, "sounds.json should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject entry = root.getAsJsonObject("radar_jammer_tower_loop");

            assertNotNull(entry, "sounds.json should expose radar_jammer_tower_loop.");
            assertEquals("subtitles.sbwdroneconfig.radar_jammer_tower_loop", entry.get("subtitle").getAsString());
            assertEquals("sbwdroneconfig:radar_jammer_tower_loop",
                    entry.getAsJsonArray("sounds").get(0).getAsJsonObject().get("name").getAsString());
        }
    }

    @Test
    void radarJammerTowerHasCraftingAndVehicleAssemblyRecipes() throws Exception {
        JsonObject crafting = JsonParser.parseString(Files.readString(RECIPES.resolve("radar_jammer_tower.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("minecraft:crafting_shaped", crafting.get("type").getAsString());
        assertEquals("sbwdroneconfig:radar_jammer_tower", crafting.getAsJsonObject("result").get("id").getAsString());

        JsonObject assembling = JsonParser.parseString(Files.readString(RECIPES.resolve("radar_jammer_tower_vehicle_assembling.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("superbwarfare:vehicle_assembling", assembling.get("type").getAsString());
        assertEquals("misc", assembling.get("category").getAsString());
        assertEquals("sbwdroneconfig:radar_jammer_tower", assembling.getAsJsonObject("result").get("item").getAsString());
    }
}
