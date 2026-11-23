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

class AntiDroneNetCarpetSourceTest {
    @Test
    void antiDroneNetAndCarpetUseWoolBlockSounds() throws Exception {
        String blocksSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonBlocks.java"
        ), StandardCharsets.UTF_8);

        assertTrue(blocksSource.contains("ANTI_DRONE_NET_CARPET"),
                "The flat Anti-Drone Net Carpet block should be registered.");
        assertTrue(blocksSource.contains("\"anti_drone_net_carpet\""),
                "The carpet registry id should be anti_drone_net_carpet.");
        assertTrue(blocksSource.contains("new AntiDroneNetCarpetBlock"),
                "The carpet should use a dedicated flat carpet block class.");
        assertTrue(blocksSource.contains("SoundType.WOOL"),
                "Anti-drone net blocks should use wool-style block sounds.");
    }

    @Test
    void antiDroneNetCarpetSharesTrapLogicWithTheRegularNet() throws Exception {
        String carpetSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AntiDroneNetCarpetBlock.java"
        ), StandardCharsets.UTF_8);
        assertTrue(carpetSource.contains("DRONE_TRIGGER_HEIGHT = 0.25D"),
                "The carpet should expose a 0.25 block high drone trigger zone.");
        assertTrue(carpetSource.contains("Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D)"),
                "The carpet visual shape should be one pixel high like vanilla carpet.");
        assertTrue(carpetSource.contains("Shapes.empty()"),
                "The carpet should not block players like a full collision cube.");

        String netSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AntiDroneNetBlock.java"
        ), StandardCharsets.UTF_8);
        assertTrue(netSource.contains("isAntiDroneNetTrapBlock"),
                "The drone-side scan should use a shared trap block helper.");
        assertTrue(netSource.contains("instanceof AntiDroneNetCarpetBlock"),
                "The Anti-Drone Net scan should also recognize the carpet variant.");
        assertTrue(netSource.contains("AntiDroneNetCarpetBlock.getDroneTriggerBox"),
                "The carpet variant should use its lower trigger area instead of a full cube.");
    }

    @Test
    void antiDroneNetCarpetHasCutoutAssetsAndRecipe() throws Exception {
        ClassLoader loader = AntiDroneNetCarpetSourceTest.class.getClassLoader();

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/blockstates/anti_drone_net_carpet.json")) {
            assertNotNull(stream, "Anti-Drone Net Carpet blockstate should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject variants = root.getAsJsonObject("variants");
            assertEquals("sbwdroneconfig:block/anti_drone_net_carpet",
                    variants.getAsJsonObject("").get("model").getAsString());
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/block/anti_drone_net_carpet.json")) {
            assertNotNull(stream, "Anti-Drone Net Carpet block model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("sbwdroneconfig:block/anti_drone_net",
                    root.getAsJsonObject("textures").get("net").getAsString(),
                    "The carpet should reuse the same transparent anti-drone net texture.");
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/anti_drone_net_carpet.json")) {
            assertNotNull(stream, "Anti-Drone Net Carpet item model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("minecraft:item/generated", root.get("parent").getAsString());
            assertEquals("sbwdroneconfig:item/anti_drone_net",
                    root.getAsJsonObject("textures").get("layer0").getAsString(),
                    "The carpet inventory item should reuse the updated transparent net icon.");
        }

        assertNotNull(loader.getResource("data/sbwdroneconfig/loot_tables/blocks/anti_drone_net_carpet.json"),
                "Anti-Drone Net Carpet loot table should be packaged.");
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipes/anti_drone_net_carpet.json"),
                "Anti-Drone Net Carpet crafting recipe should be packaged.");

        String renderLayerSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonClientRenderLayers.java"
        ), StandardCharsets.UTF_8);
        assertTrue(renderLayerSource.contains("AddonBlocks.ANTI_DRONE_NET_CARPET.get()"),
                "The carpet should also be registered as cutout so transparent holes render.");
    }
}
