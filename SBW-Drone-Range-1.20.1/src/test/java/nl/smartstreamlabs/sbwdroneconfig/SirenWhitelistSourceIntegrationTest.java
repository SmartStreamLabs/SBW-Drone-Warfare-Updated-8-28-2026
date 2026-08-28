package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SirenWhitelistSourceIntegrationTest {
    @Test
    void sirenWhitelistUsesControllerAndOwnerGamertagsBeforeTriggeringAlarm() throws Exception {
        String source = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DroneDetectionSirenBlockEntity.java"), StandardCharsets.UTF_8);
        String compat = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "SbwCompat.java"), StandardCharsets.UTF_8);

        assertTrue(source.contains("whitelistedGamertags"), "Siren should store a per-block trusted pilot whitelist.");
        assertTrue(source.contains("findWhitelistedGamertag(level, entity, ownerIdentity)"), "Siren detection should check gamertag whitelist before accepting a drone.");
        assertTrue(source.contains("SbwCompat.getDroneControllerUuid(drone)"), "Active controller gamertag should be checked.");
        assertTrue(source.contains("SbwCompat.resolveDroneOwner(level, entity)"), "Stored drone owner identity should be checked.");
        assertTrue(source.contains("SbwCompat.findLinkedMonitorController(level, drone)"),
                "Siren whitelist needs a monitor-link fallback when the drone entity exposes no controller.");
        assertTrue(compat.contains("findLinkedMonitorController"), "SbwCompat should expose a linked-monitor controller lookup.");
        assertTrue(compat.contains("player.getInventory().items"), "Linked-monitor lookup should scan inventory stacks too.");
        assertTrue(source.contains("return false;"), "Whitelisted pilots should prevent the siren from detecting their drone.");
        assertTrue(source.contains("WhitelistedGamertags"), "Whitelist should persist to a clear NBT key.");
        assertTrue(source.contains("BlacklistedGamertags"), "Legacy saved siren lists should still migrate/load.");
        assertFalse(source.contains("return blacklistedGamertags.contains(normalizedName)"),
                "Siren GUI entries must not be treated as drone-name blacklist entries anymore.");
    }

    @Test
    void addonDroneItemsAssignOwnerImmediatelyWhenSpawned() throws Exception {
        String cubedItem = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "CubedFpvDroneItem.java"), StandardCharsets.UTF_8);
        String lucasItem = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "LucasDroneItem.java"), StandardCharsets.UTF_8);

        assertTrue(cubedItem.contains("SbwCompat.assignDroneOwner(entity, context.getPlayer())"),
                "Cubed FPV drones should get an owner as soon as they are placed.");
        assertTrue(cubedItem.contains("SbwCompat.assignDroneOwner(entity, player)"),
                "Cubed FPV drones spawned into liquid should also get an owner.");
        assertTrue(lucasItem.contains("SbwCompat.assignDroneOwner(entity, context.getPlayer())"),
                "LUCAS drones should get an owner as soon as they are placed.");
        assertTrue(lucasItem.contains("SbwCompat.assignDroneOwner(entity, player)"),
                "LUCAS drones spawned into liquid should also get an owner.");
    }

    @Test
    void sirenTranslationsUseWhitelistLanguage() throws Exception {
        try (var stream = SirenWhitelistSourceIntegrationTest.class.getClassLoader().getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("Whitelisted players", root.get("screen.sbwdroneconfig.siren.blacklist").getAsString());
            assertEquals("Gamertag whitelist", root.get("screen.sbwdroneconfig.siren.input").getAsString());
            assertEquals("Added siren whitelist entry: %s", root.get("message.sbwdroneconfig.siren_blacklist_added").getAsString());
            assertEquals("Removed siren whitelist entry: %s", root.get("message.sbwdroneconfig.siren_blacklist_removed").getAsString());
            assertEquals("Can output redstone and whitelist trusted pilot gamertags.",
                    root.get("tooltip.sbwdroneconfig.drone_detection_siren.2").getAsString());
        }
    }
}
