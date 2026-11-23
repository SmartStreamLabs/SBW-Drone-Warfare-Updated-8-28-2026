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

class RadarJammerTowerSolarEnergySourceTest {
    private static final Path MAIN = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig");

    @Test
    void towerHasSolarBatteryConfigAndCachedAccessors() throws Exception {
        String config = Files.readString(MAIN.resolve("AddonConfig.java"), StandardCharsets.UTF_8);

        assertTrue(config.contains("RADAR_TOWER_MAX_BATTERY_CAPACITY_VALUE"));
        assertTrue(config.contains("RADAR_TOWER_SOLAR_CHARGE_RATE_VALUE"));
        assertTrue(config.contains("RADAR_TOWER_CHARGING_STATION_CHARGE_RATE_VALUE"));
        assertTrue(config.contains("RADAR_TOWER_JAMMER_ENERGY_DRAIN_RATE_VALUE"));
        assertTrue(config.contains("RADAR_TOWER_MINIMUM_ENERGY_TO_ACTIVATE_VALUE"));
        assertTrue(config.contains("radarTowerMaxBatteryCapacity()"));
        assertTrue(config.contains("radarTowerSolarChargeRate()"));
        assertTrue(config.contains("radarTowerChargingStationChargeRate()"));
        assertTrue(config.contains("radarTowerJammerEnergyDrainRate()"));
        assertTrue(config.contains("radarTowerMinimumEnergyToActivate()"));
    }

    @Test
    void towerStoresEnergyRequiresStationAndGatesJamming() throws Exception {
        String tower = Files.readString(MAIN.resolve("RadarJammerTowerBlockEntity.java"), StandardCharsets.UTF_8);
        String jammer = Files.readString(MAIN.resolve("JammerBlockEntity.java"), StandardCharsets.UTF_8);

        assertTrue(tower.contains("energy"), "Radar tower should store internal battery energy.");
        assertTrue(tower.contains("RadarTowerEnergy"), "Energy should be saved to NBT.");
        assertTrue(tower.contains("tickStationBattery"), "Tower should update station charging server-side.");
        org.junit.jupiter.api.Assertions.assertFalse(tower.contains("canSeeSky"), "Sunlight must not charge the tower.");
        assertTrue(tower.contains("extractEnergy"), "Station energy must be consumed, not duplicated.");
        assertTrue(tower.contains("canJammerOperate"), "Tower should expose an energy gate for jammer operation.");
        assertTrue(tower.contains("drainActiveJammerEnergy"), "Tower should drain energy while active.");
        assertTrue(jammer.contains("blockEntity.tickBeforeJammerScan(serverLevel, pos, gameTime)"),
                "Shared jammer tick should let the tower charge before scanning.");
        assertTrue(jammer.contains("blockEntity.canJammerOperate(serverLevel, pos, gameTime)"),
                "Shared jammer tick should skip tower jamming when energy is too low.");
        assertTrue(jammer.contains("blockEntity.afterActiveJammerScan(serverLevel, pos, gameTime)"),
                "Shared jammer tick should drain powered tower energy after an active scan.");
    }

    @Test
    void towerCanChargeFromSbwChargingStationsBelow() throws Exception {
        String tower = Files.readString(MAIN.resolve("RadarJammerTowerBlockEntity.java"), StandardCharsets.UTF_8);

        assertTrue(tower.contains("BuiltInRegistries.BLOCK.getKey"),
                "Charging station detection should use registry IDs instead of fragile SBW class imports.");
        assertTrue(tower.contains("stationBelow"),
                "Tower must check the station directly underneath its base.");
        assertTrue(tower.contains("worldPosition.below()"),
                "Charging station support should look below the tower/base.");
        assertTrue(tower.contains("\"superbwarfare\""), "Tower should only match Superb Warfare charging stations.");
        assertTrue(tower.contains("\"charging_station\""), "Normal SBW charging station should charge the tower.");
        assertTrue(tower.contains("\"creative_charging_station\""), "Creative SBW charging station should charge the tower.");
        assertTrue(tower.contains("radarTowerChargingStationChargeRate()"),
                "Charging station power should use a dedicated configurable charge rate.");
    }

    @Test
    void towerGuiOnlyOpensWhenPoweredAndShowsPowerStatus() throws Exception {
        String block = Files.readString(MAIN.resolve("JammerBlock.java"), StandardCharsets.UTF_8);
        String message = Files.readString(MAIN.resolve("OpenJammerConfigMessage.java"), StandardCharsets.UTF_8);
        String screen = Files.readString(MAIN.resolve("JammerScreen.java"), StandardCharsets.UTF_8);

        assertTrue(block.contains("canOpenPoweredGui"), "Tower GUI should be blocked when the battery is below activation energy.");
        assertTrue(block.contains("message.sbwdroneconfig.radar_jammer_tower.no_power"),
                "Players should get a clear no-power message.");
        assertTrue(message.contains("energy") && message.contains("maxEnergy") && message.contains("charging") && message.contains("operational"),
                "Open packet should sync tower power status to the client GUI.");
        assertTrue(screen.contains("renderTowerPowerStatus"), "Tower GUI should render a dedicated power/status panel.");
        assertTrue(screen.contains("screen.sbwdroneconfig.radar_jammer_tower.energy"));
        assertTrue(screen.contains("screen.sbwdroneconfig.radar_jammer_tower.charging"));
        assertTrue(screen.contains("screen.sbwdroneconfig.radar_jammer_tower.status"));
    }

    @Test
    void englishTranslationsExplainTowerPowerState() throws Exception {
        try (var stream = RadarJammerTowerSolarEnergySourceTest.class.getClassLoader().getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();

            assertEquals("Radar tower requires a powered Charging Station underneath.", root.get("message.sbwdroneconfig.radar_jammer_tower.no_power").getAsString());
            assertEquals("Energy: %s / %s RF", root.get("screen.sbwdroneconfig.radar_jammer_tower.energy").getAsString());
            assertEquals("Charging: %s", root.get("screen.sbwdroneconfig.radar_jammer_tower.charging").getAsString());
            assertEquals("Jammer: %s", root.get("screen.sbwdroneconfig.radar_jammer_tower.status").getAsString());
        }
    }
}
