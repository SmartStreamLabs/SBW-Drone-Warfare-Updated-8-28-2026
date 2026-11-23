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

class AntiDroneNetPanelSourceTest {
    @Test
    void antiDroneNetPanelIsRegisteredAsSlabStyleTrapBlock() throws Exception {
        String panelSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AntiDroneNetPanelBlock.java"
        ), StandardCharsets.UTF_8);
        assertTrue(panelSource.contains("extends Block"),
                "Anti-Drone Net Panel should extend Block directly, not a connecting pane/fence parent.");
        assertTrue(!panelSource.contains("extends IronBarsBlock"),
                "Anti-Drone Net Panel must not inherit iron-bar connection behavior.");
        assertTrue(!panelSource.contains("extends PaneBlock"),
                "Anti-Drone Net Panel must not inherit pane connection behavior.");
        assertTrue(!panelSource.contains("extends FenceBlock"),
                "Anti-Drone Net Panel must not inherit fence connection behavior.");

        String blocksSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonBlocks.java"
        ), StandardCharsets.UTF_8);
        assertTrue(blocksSource.contains("ANTI_DRONE_NET_PANEL"),
                "Anti-Drone Net Panel should be registered as a block.");
        assertTrue(blocksSource.contains("\"anti_drone_net_panel\""),
                "Anti-Drone Net Panel registry id should be anti_drone_net_panel.");
        assertTrue(blocksSource.contains("new AntiDroneNetPanelBlock"),
                "Anti-Drone Net Panel should use a dedicated slab-style block class.");

        String itemsSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonItems.java"
        ), StandardCharsets.UTF_8);
        assertTrue(itemsSource.contains("ANTI_DRONE_NET_PANEL"),
                "Anti-Drone Net Panel should have a registered block item.");

        String tabSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonCreativeTabs.java"
        ), StandardCharsets.UTF_8);
        assertTrue(tabSource.contains("AddonItems.ANTI_DRONE_NET_PANEL.get()"),
                "Anti-Drone Net Panel should appear in the addon creative tab.");
    }

    @Test
    void antiDroneNetPanelSupportsSlabPlacementAndConfigurableTrigger() throws Exception {
        String panelSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AntiDroneNetPanelBlock.java"
        ), StandardCharsets.UTF_8);

        assertTrue(panelSource.contains("EnumProperty<SlabType> TYPE"),
                "The panel should store slab type using the vanilla SlabType property.");
        assertTrue(panelSource.contains("SlabType.BOTTOM"),
                "The panel should support bottom slab placement.");
        assertTrue(panelSource.contains("SlabType.TOP"),
                "The panel should support top slab placement.");
        assertTrue(panelSource.contains("SlabType.DOUBLE"),
                "The panel should support combining two panels into a double slab.");
        assertTrue(panelSource.contains("context.getClickLocation().y - context.getClickedPos().getY()"),
                "Placement should choose top/bottom from the click height inside the block.");
        assertTrue(panelSource.contains("canBeReplaced"),
                "The panel should allow placing another panel into the same block space to form a double slab.");
        assertTrue(!panelSource.contains("canSurvive("),
                "The panel should be free-floating and should not run support survival checks.");
        assertTrue(!panelSource.contains("updateShape("),
                "The panel should not remove itself during neighbor updates when support is missing.");
        assertTrue(!panelSource.contains("Blocks.AIR.defaultBlockState()"),
                "The panel should never replace itself with air because support was removed.");
        assertTrue(panelSource.contains("Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D)"),
                "Bottom slab shape should occupy the lower half.");
        assertTrue(panelSource.contains("Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D)"),
                "Top slab shape should occupy the upper half.");
        assertTrue(panelSource.contains("Shapes.block()"),
                "Double slab shape should occupy the full block.");

        String configSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonConfig.java"
        ), StandardCharsets.UTF_8);
        assertTrue(configSource.contains("antiDroneNetPanelRequiresSupport"),
                "The config should expose antiDroneNetPanelRequiresSupport.");
        assertTrue(configSource.contains("antiDroneNetPanelTriggerThickness"),
                "The config should keep antiDroneNetPanelTriggerThickness for compatibility.");
        assertTrue(configSource.contains("antiDroneNetPanelVisualThickness"),
                "The config should keep antiDroneNetPanelVisualThickness for compatibility.");
    }

    @Test
    void antiDroneNetPanelSharesNetTrapLogicAndCutoutAssets() throws Exception {
        String netSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AntiDroneNetBlock.java"
        ), StandardCharsets.UTF_8);
        assertTrue(netSource.contains("instanceof AntiDroneNetPanelBlock"),
                "The existing drone-side net scan should recognize Anti-Drone Net Panels.");
        assertTrue(netSource.contains("AntiDroneNetPanelBlock.getDroneTriggerBox"),
                "Panel trigger boxes should be based on the slab type.");

        ClassLoader loader = AntiDroneNetPanelSourceTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/blockstates/anti_drone_net_panel.json")) {
            assertNotNull(stream, "Anti-Drone Net Panel blockstate should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertTrue(!root.has("multipart"),
                    "The panel blockstate should not use multipart connection models.");
            JsonObject variants = root.getAsJsonObject("variants");
            assertEquals("sbwdroneconfig:block/anti_drone_net_panel_bottom",
                    variants.getAsJsonObject("type=bottom").get("model").getAsString());
            assertEquals("sbwdroneconfig:block/anti_drone_net_panel_top",
                    variants.getAsJsonObject("type=top").get("model").getAsString());
            assertEquals("sbwdroneconfig:block/anti_drone_net_panel_double",
                    variants.getAsJsonObject("type=double").get("model").getAsString());
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/block/anti_drone_net_panel_bottom.json")) {
            assertNotNull(stream, "Anti-Drone Net Panel bottom model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("sbwdroneconfig:block/anti_drone_net",
                    root.getAsJsonObject("textures").get("net").getAsString(),
                    "The panel should reuse the same transparent anti-drone net texture.");
            JsonObject faces = root.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces");
            assertTrue(faces.has("up"), "The flat panel model should render the top face.");
            assertTrue(faces.has("down"), "The flat panel model should render the bottom face.");
            assertTrue(!faces.has("north") && !faces.has("south") && !faces.has("east") && !faces.has("west"),
                    "The flat panel model should not render side faces that look like poles or edge strips.");
        }
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/block/anti_drone_net_panel_top.json"),
                "Anti-Drone Net Panel top model should be packaged.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/block/anti_drone_net_panel_double.json"),
                "Anti-Drone Net Panel double model should be packaged.");

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/anti_drone_net_panel.json")) {
            assertNotNull(stream, "Anti-Drone Net Panel item model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("minecraft:item/generated", root.get("parent").getAsString());
            assertEquals("sbwdroneconfig:item/anti_drone_net",
                    root.getAsJsonObject("textures").get("layer0").getAsString(),
                    "The panel inventory icon should reuse the updated transparent net icon.");
        }

        assertNotNull(loader.getResource("data/sbwdroneconfig/loot_tables/blocks/anti_drone_net_panel.json"),
                "Anti-Drone Net Panel loot table should be packaged.");
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipes/anti_drone_net_panel.json"),
                "Anti-Drone Net Panel crafting recipe should be packaged.");

        String renderLayerSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonClientRenderLayers.java"
        ), StandardCharsets.UTF_8);
        assertTrue(renderLayerSource.contains("AddonBlocks.ANTI_DRONE_NET_PANEL.get()"),
                "The panel should use cutout rendering so net holes are transparent.");
    }
}
