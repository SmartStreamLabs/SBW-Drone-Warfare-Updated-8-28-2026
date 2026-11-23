package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RadarJammerTowerOwnerAlertSourceTest {
    @Test
    void towerNotifiesOwnerWhenDroneIsNearby() throws Exception {
        String tower = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "RadarJammerTowerBlockEntity.java"));
        String jammer = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "JammerBlockEntity.java"));
        String lang = Files.readString(Path.of(
                "src", "main", "resources", "assets", "sbwdroneconfig", "lang", "en_us.json"));

        assertTrue(jammer.contains("getOwnerUuid()"), "Tower should be able to read its owner.");
        assertTrue(tower.contains("notifyOwnerOfNearbyDrone"), "Tower should have owner alert logic.");
        assertTrue(tower.contains("getServer().getPlayerList().getPlayer(ownerUuid)"),
                "Tower should notify the owning player only.");
        assertTrue(tower.contains("displayClientMessage(Component.translatable"),
                "Tower should show an on-screen message.");
        assertTrue(tower.contains("message.sbwdroneconfig.radar_jammer_tower.drone_nearby"),
                "Tower should use a translatable owner-alert message.");
        assertTrue(tower.contains("Math.round"), "Tower should report distance in blocks.");
        assertTrue(tower.contains("OWNER_ALERT_INTERVAL_TICKS"), "Tower should rate-limit owner alerts.");
        assertTrue(tower.contains("SbwCompat.isDrone(entity)") && tower.contains("LucasDroneEntity"),
                "Tower should detect normal drones and LUCAS drones.");
        assertTrue(lang.contains("\"message.sbwdroneconfig.radar_jammer_tower.drone_nearby\""),
                "Owner-alert lang key should exist.");
    }
}
