package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonArray;
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

class FuelMixerSourceIntegrationTest {
    @Test
    void fuelMixerBlockItemBlockEntityMenuAndScreenAreRegistered() throws Exception {
        String blocks = readSource("AddonBlocks.java");
        String items = readSource("AddonItems.java");
        String blockEntities = readSource("AddonBlockEntities.java");
        String menus = readSource("AddonMenus.java");
        String mod = readSource("SbwDroneRangeConfig.java");
        String client = readSource("FuelMixerClient.java");

        assertTrue(blocks.contains("FUEL_MIXER"), "Fuel Mixer block should be registered.");
        assertTrue(items.contains("FUEL_MIXER"), "Fuel Mixer block item should be registered.");
        assertTrue(items.contains("GASOLINE_CANISTER"), "Gasoline Canister item should be registered.");
        assertTrue(blockEntities.contains("FuelMixerBlockEntity"), "Fuel Mixer block entity should be registered.");
        assertTrue(menus.contains("FUEL_MIXER"), "Fuel Mixer menu type should be registered.");
        assertTrue(mod.contains("AddonMenus.MENU_TYPES.register(modBus)"), "Menu types should be attached to the mod event bus.");
        assertTrue(client.contains("FuelMixerScreen::new"), "Fuel Mixer screen should be registered on the client.");
    }

    @Test
    void fuelMixerBlockEntityProcessesThreeIngredientsIntoGasolineCanister() throws Exception {
        String blockEntity = readSource("FuelMixerBlockEntity.java");

        assertTrue(blockEntity.contains("SLOT_COAL"));
        assertTrue(blockEntity.contains("SLOT_BLAZE_POWDER"));
        assertTrue(blockEntity.contains("SLOT_REDSTONE"));
        assertTrue(blockEntity.contains("SLOT_OUTPUT"));
        assertTrue(blockEntity.contains("Items.COAL"));
        assertTrue(blockEntity.contains("Items.CHARCOAL"));
        assertTrue(blockEntity.contains("Items.BLAZE_POWDER"));
        assertTrue(blockEntity.contains("Items.REDSTONE"));
        assertTrue(blockEntity.contains("AddonItems.GASOLINE_CANISTER"));
        assertTrue(blockEntity.contains("AddonConfig.fuelMixerProcessTime()"), "Processing time should come from config.");
        assertTrue(blockEntity.contains("progress++"), "Mixer should advance progress server-side.");
        assertTrue(blockEntity.contains("consumeIngredients"), "Mixer should consume exactly one of each ingredient.");
        assertTrue(blockEntity.contains("ContainerHelper.saveAllItems"), "Inventory should persist.");
        assertTrue(blockEntity.contains("ContainerHelper.loadAllItems"), "Inventory should load.");
    }

    @Test
    void fuelMixerConfigExposesProcessTimeAndJerrycanFuelAmount() throws Exception {
        String config = readSource("AddonConfig.java");

        assertTrue(config.contains("FUEL_MIXER_PROCESS_TIME_VALUE"));
        assertTrue(config.contains("GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE"));
        assertTrue(config.contains("fuelMixerProcessTime"));
        assertTrue(config.contains("gasolineJerrycanFuelAmount"));
        assertTrue(config.contains(".defineInRange(\"fuelMixerProcessTime\", 200"));
        assertTrue(config.contains(".defineInRange(\"gasolineJerrycanFuelAmount\", 1000"));
    }

    @Test
    void gasolineJerrycanUsesConfigurableFuelAmount() throws Exception {
        String source = readSource("LucasFuelSystem.java");

        assertTrue(source.contains("AddonConfig.gasolineJerrycanFuelAmount()"),
                "Refueling should use the configurable Gasoline Jerrycan amount instead of a hard-coded value.");
        assertFalse(source.contains("final int fuelAmount = 250"), "Old hard-coded 250 fuel value should be removed.");
    }

    @Test
    void recipesUseFuelMixerCanisterFlowInsteadOfDirectGasolineCrafting() throws Exception {
        JsonObject vanillaGasolineRecipe = readRecipe("gasoline_jerrycan.json");
        JsonArray vanillaIngredients = vanillaGasolineRecipe.getAsJsonArray("ingredients");
        assertEquals("minecraft:crafting_shapeless", vanillaGasolineRecipe.get("type").getAsString());
        assertEquals("sbwdroneconfig:empty_jerrycan", vanillaIngredients.get(0).getAsJsonObject().get("item").getAsString());
        assertEquals("sbwdroneconfig:gasoline_canister", vanillaIngredients.get(1).getAsJsonObject().get("item").getAsString());
        assertEquals("sbwdroneconfig:gasoline_jerrycan", vanillaGasolineRecipe.getAsJsonObject("result").get("item").getAsString());

        JsonObject assemblingRecipe = readRecipe("gasoline_jerrycan_vehicle_assembling.json");
        String assemblingText = Files.readString(Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", "gasoline_jerrycan_vehicle_assembling.json"), StandardCharsets.UTF_8);
        assertEquals("superbwarfare:vehicle_assembling", assemblingRecipe.get("type").getAsString());
        assertTrue(assemblingText.contains("sbwdroneconfig:empty_jerrycan"));
        assertTrue(assemblingText.contains("sbwdroneconfig:gasoline_canister"));
        assertFalse(assemblingText.contains("minecraft:coal"));
        assertFalse(assemblingText.contains("minecraft:redstone"));
        assertFalse(assemblingText.contains("minecraft:blaze_powder"));
    }

    @Test
    void assetsAndTranslationsExistForFuelMixerAndCanister() throws Exception {
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "blockstates", "fuel_mixer.json")));
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "models", "block", "fuel_mixer.json")));
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "models", "item", "fuel_mixer.json")));
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "models", "item", "gasoline_canister.json")));
        assertTrue(Files.exists(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "textures", "item", "gasoline_canister.png")));

        try (var stream = FuelMixerSourceIntegrationTest.class.getClassLoader().getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("Fuel Mixer", root.get("block.sbwdroneconfig.fuel_mixer").getAsString());
            assertEquals("Gasoline Canister", root.get("item.sbwdroneconfig.gasoline_canister").getAsString());
            assertEquals("Produces Gasoline Canisters from coal or charcoal, Blaze Powder and Redstone.", root.get("tooltip.sbwdroneconfig.fuel_mixer.1").getAsString());
            assertEquals("Open the machine and fill the three ingredient slots.", root.get("tooltip.sbwdroneconfig.fuel_mixer.2").getAsString());
            assertEquals("Processing stops when ingredients are missing or the output is full.", root.get("tooltip.sbwdroneconfig.fuel_mixer.3").getAsString());
            assertEquals("Fuel ingredient produced by the Fuel Mixer.", root.get("tooltip.sbwdroneconfig.gasoline_canister.1").getAsString());
            assertEquals("Craft with an Empty Jerrycan to make a usable refueling can.", root.get("tooltip.sbwdroneconfig.gasoline_canister.2").getAsString());
            assertEquals("Refuels compatible gasoline-powered drones.", root.get("tooltip.sbwdroneconfig.gasoline_jerrycan.1").getAsString());
            assertEquals("Slot 1: Coal or Charcoal", root.get("screen.sbwdroneconfig.fuel_mixer.tooltip.coal").getAsString());
            assertEquals("Slot 2: Blaze Powder", root.get("screen.sbwdroneconfig.fuel_mixer.tooltip.blaze_powder").getAsString());
            assertEquals("Slot 3: Redstone", root.get("screen.sbwdroneconfig.fuel_mixer.tooltip.redstone").getAsString());
            assertEquals("Output: Gasoline Canister", root.get("screen.sbwdroneconfig.fuel_mixer.tooltip.output").getAsString());
            assertEquals("Wait until the bar fills to make 1 Gasoline Canister.", root.get("screen.sbwdroneconfig.fuel_mixer.tooltip.progress").getAsString());
        }
    }

    @Test
    void fuelMixerBlockItemAndScreenShowHelpfulTooltips() throws Exception {
        String items = readSource("AddonItems.java");
        String screen = readSource("FuelMixerScreen.java");

        assertTrue(items.contains("\"tooltip.sbwdroneconfig.fuel_mixer.1\""));
        assertTrue(items.contains("\"tooltip.sbwdroneconfig.fuel_mixer.2\""),
                "Fuel Mixer block item should show both tooltip lines.");
        assertTrue(items.contains("\"tooltip.sbwdroneconfig.fuel_mixer.3\""),
                "Fuel Mixer block item should explain how the output becomes a Gasoline Jerrycan.");
        assertTrue(screen.contains("renderFuelMixerHints"), "Fuel Mixer screen should render hover hints.");
        assertTrue(screen.contains("screen.sbwdroneconfig.fuel_mixer.tooltip.coal"));
        assertTrue(screen.contains("screen.sbwdroneconfig.fuel_mixer.tooltip.blaze_powder"));
        assertTrue(screen.contains("screen.sbwdroneconfig.fuel_mixer.tooltip.redstone"));
        assertTrue(screen.contains("screen.sbwdroneconfig.fuel_mixer.tooltip.output"));
        assertTrue(screen.contains("screen.sbwdroneconfig.fuel_mixer.tooltip.progress"));
    }

    @Test
    void fuelMixerHasActiveParticlesAndSoundWhileProcessing() throws Exception {
        String block = readSource("FuelMixerBlock.java");
        String blockEntity = readSource("FuelMixerBlockEntity.java");
        String blockstate = Files.readString(Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "blockstates", "fuel_mixer.json"), StandardCharsets.UTF_8);

        assertTrue(block.contains("LIT"), "Fuel Mixer block should expose a lit state for active client effects.");
        assertTrue(block.contains("animateTick"), "Fuel Mixer should spawn client-only effects from animateTick.");
        assertTrue(block.contains("ParticleTypes.SMOKE"), "Active Fuel Mixer should emit smoke particles.");
        assertTrue(block.contains("ParticleTypes.FLAME"), "Active Fuel Mixer should emit flame/spark particles.");
        assertTrue(block.contains("SoundEvents.FURNACE_FIRE_CRACKLE"), "Active Fuel Mixer should play a looping-ish processing sound.");
        assertTrue(block.contains("SoundSource.BLOCKS"), "Fuel Mixer sound should be positional block audio.");
        assertTrue(block.contains("state.getValue(LIT)"), "Effects should only run when the Fuel Mixer is lit.");

        assertTrue(blockEntity.contains("FuelMixerBlock.LIT"), "Server processing should update the lit blockstate.");
        assertTrue(blockEntity.contains("setFuelMixerLit"), "Lit sync should be isolated in a helper.");
        assertTrue(blockstate.contains("lit=true"), "Blockstate should include active lit variants.");
        assertTrue(blockstate.contains("lit=false"), "Blockstate should include inactive lit variants.");
    }

    private static String readSource(String fileName) throws Exception {
        return Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", fileName), StandardCharsets.UTF_8);
    }

    private static JsonObject readRecipe(String fileName) throws Exception {
        Path path = Path.of("src", "main", "resources", "data", "sbwdroneconfig", "recipes", fileName);
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
