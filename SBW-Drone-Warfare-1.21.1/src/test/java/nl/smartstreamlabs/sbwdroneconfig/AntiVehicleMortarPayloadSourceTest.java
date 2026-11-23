package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntiVehicleMortarPayloadSourceTest {
    @Test
    void mortarPayloadIsRegisteredAsItemEntityRendererAndNetworkMessage() throws Exception {
        String items = readSource("AddonItems.java");
        String entities = readSource("AddonEntities.java");
        String renderers = readSource("AddonEntityRenderers.java");
        String network = readSource("AddonNetwork.java");

        assertTrue(items.contains("ANTI_VEHICLE_MORTAR_PAYLOAD"));
        assertTrue(items.contains("\"anti_vehicle_mortar_payload\""));
        assertTrue(entities.contains("ANTI_VEHICLE_MORTAR_PAYLOAD"));
        assertTrue(entities.contains("AntiVehicleMortarPayloadEntity"));
        assertTrue(renderers.contains("AntiVehicleMortarPayloadRenderer"));
        assertTrue(network.contains("DropMortarPayloadMessage"));
        assertTrue(network.contains("dropMortarPayload()"));
    }

    @Test
    void mortarPayloadServerLogicDropsFromDroneInventoryAndPrioritizesVehicleDamage() throws Exception {
        String system = readSource("DronePayloadSystem.java");
        String projectile = readSource("AntiVehicleMortarPayloadEntity.java");
        String mod = readSource("SbwDroneRangeConfig.java");

        assertTrue(system.contains("consumePayloadFromDroneInventory"));
        assertTrue(system.contains("new DroneInventoryContainer(drone)"));
        assertTrue(system.contains("getMortarDropPosition"));
        assertTrue(system.contains("new AntiVehicleMortarPayloadEntity"));
        assertFalse(system.contains("TAG_MOUNTED_MORTAR_PAYLOAD_UUID"));
        assertFalse(system.contains("mountMortarPayload"));
        assertFalse(system.contains("consumeMountedMortarPayload"));
        assertFalse(mod.contains("NeoForge.EVENT_BUS.register(DronePayloadSystem.class)"));
        assertTrue(system.contains("SbwCompat.findActiveLinkedDrone"));
        assertTrue(system.contains("AddonConfig.antiVehicleMortarPayloadCooldownTicks()"));
        assertTrue(projectile.contains("releaseFromDrone"));
        assertFalse(projectile.contains("mountToDrone"));
        assertFalse(projectile.contains("isMounted"));
        assertTrue(projectile.contains("AddonConfig.antiVehicleMortarVehicleDamage()"));
        assertTrue(projectile.contains("AddonConfig.antiVehicleMortarEntityDamage()"));
        assertTrue(projectile.contains("AddonConfig.antiVehicleMortarExplosionRadius()"));
        assertTrue(projectile.contains("SbwCompat.isSbwVehicle"));
        assertFalse(projectile.contains("applyVehicleDirectDamage"));
        assertTrue(projectile.contains("Vc01Damage.component"));
    }

    @Test
    void mortarPayloadAssetsAndVehicleAssemblyRecipeExist() {
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "models", "item", "anti_vehicle_mortar_payload.json")));
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "textures", "item", "anti_vehicle_mortar_payload.png")));
        assertTrue(Files.exists(Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", "anti_vehicle_mortar_payload_vehicle_assembling.json")));
    }

    @Test
    void mortarPayloadLangIncludesGameplayTooltipWithoutAttribution() throws Exception {
        String lang = Files.readString(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "lang", "en_us.json"), StandardCharsets.UTF_8);

        assertTrue(lang.contains("item.sbwdroneconfig.anti_vehicle_mortar_payload"));
        assertTrue(lang.contains("tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.1"));
        assertTrue(lang.contains("tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.2"));
        assertTrue(lang.contains("tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.3"));
        assertTrue(lang.contains("Load it into a compatible drone inventory and use the payload control."));
        assertTrue(lang.contains("No Anti-vehicle mortar in the drone inventory."));
        assertFalse(lang.contains("Mortar rounds"));
        assertFalse(lang.contains("Model credit"));
    }

    private static String readSource(String fileName) throws Exception {
        return Files.readString(Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                fileName
        ), StandardCharsets.UTF_8);
    }
}
