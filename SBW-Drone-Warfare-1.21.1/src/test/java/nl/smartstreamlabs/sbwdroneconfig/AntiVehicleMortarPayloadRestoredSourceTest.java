package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AntiVehicleMortarPayloadRestoredSourceTest {
    @Test
    void antiVehicleMortarPayloadIsRegisteredAndVisibleInForge120() throws Exception {
        String items = readMainSource("AddonItems.java");
        String creativeTab = readMainSource("AddonCreativeTabs.java");
        String lang = Files.readString(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "lang", "en_us.json"), StandardCharsets.UTF_8);

        assertTrue(items.contains("ANTI_VEHICLE_MORTAR_PAYLOAD"));
        assertTrue(items.contains("\"anti_vehicle_mortar_payload\""));
        assertTrue(items.contains("new Item.Properties().stacksTo(16).rarity(Rarity.RARE)"));
        assertTrue(creativeTab.contains("AddonItems.ANTI_VEHICLE_MORTAR_PAYLOAD.get()"));
        assertTrue(lang.contains("\"item.sbwdroneconfig.anti_vehicle_mortar_payload\": \"Anti-vehicle mortar\""));
        assertTrue(lang.contains("\"tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.1\""));
        assertTrue(lang.contains("\"tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.2\""));
        assertTrue(lang.contains("\"tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.3\""));
    }

    @Test
    void antiVehicleMortarPayloadAssetsAndRecipeExist() throws Exception {
        Path model = Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "models", "item", "anti_vehicle_mortar_payload.json");
        Path texture = Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "textures", "item", "anti_vehicle_mortar_payload.png");
        Path recipe = Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", "anti_vehicle_mortar_payload_vehicle_assembling.json");

        assertTrue(Files.exists(model));
        assertTrue(Files.exists(texture));
        assertTrue(Files.exists(recipe));

        String modelJson = Files.readString(model, StandardCharsets.UTF_8);
        String recipeJson = Files.readString(recipe, StandardCharsets.UTF_8);
        assertTrue(modelJson.contains("sbwdroneconfig:item/anti_vehicle_mortar_payload"));
        assertTrue(recipeJson.contains("\"superbwarfare:mortar_shell\""));
        assertTrue(recipeJson.contains("\"superbwarfare:detonator\""));
        assertTrue(recipeJson.contains("\"sbwdroneconfig:anti_vehicle_mortar_payload\""));
    }

    @Test
    void heavyBomberAcceptsRestoredPayloadItemWithoutChangingNativePayloadDamage() throws Exception {
        String heavyBomber = readMainSource("HeavyBomberDroneEntity.java");
        String entities = readMainSource("AddonEntities.java");
        String renderers = readMainSource("AddonEntityRenderers.java");
        String payloadEntity = readMainSource("AntiVehicleMortarPayloadEntity.java");

        assertTrue(heavyBomber.contains("ANTI_VEHICLE_MORTAR_PAYLOAD_ITEM_ID"));
        assertTrue(heavyBomber.contains("\"sbwdroneconfig:anti_vehicle_mortar_payload\""));
        assertTrue(heavyBomber.contains("new PayloadDefinition(ANTI_VEHICLE_MORTAR_PAYLOAD_ITEM_ID, \"anti_vehicle_mortar_payload\")"));
        assertTrue(heavyBomber.contains("new AntiVehicleMortarPayloadEntity"));
        assertTrue(heavyBomber.contains("new MortarShellEntity(ModEntities.MORTAR_SHELL.get(), level)"));
        assertTrue(entities.contains("ANTI_VEHICLE_MORTAR_PAYLOAD"));
        assertTrue(renderers.contains("AntiVehicleMortarPayloadRenderer"));
        assertTrue(payloadEntity.contains("VEHICLE_DAMAGE = 85.0D"));
        assertTrue(payloadEntity.contains("applyVehicleDirectDamage"));
    }

    private static String readMainSource(String fileName) throws Exception {
        return Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", fileName), StandardCharsets.UTF_8);
    }
}
