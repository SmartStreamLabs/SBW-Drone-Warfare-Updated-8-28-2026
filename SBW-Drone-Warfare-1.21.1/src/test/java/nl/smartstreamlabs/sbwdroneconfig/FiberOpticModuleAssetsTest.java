package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticModuleAssetsTest {
    @Test
    void bundlesFiberOpticModuleItemModel() {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/fiber_optic_spool_upgrade.json"));
    }

    @Test
    void fiberOpticItemModelReusesTheGeneratedItemPipeline() throws Exception {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/fiber_optic_spool_upgrade.json")) {
            assertNotNull(stream, "The Fiber Optic module item model should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("item/generated", root.get("parent").getAsString());
            assertEquals(
                    "sbwdroneconfig:item/fiber_optic_spool_upgrade",
                    root.getAsJsonObject("textures").get("layer0").getAsString(),
                    "The Fiber Optic module should point at its own dedicated texture instead of reusing the spotlight module art."
            );
        }
    }

    @Test
    void bundlesDedicatedFiberOpticTexture() {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        assertNotNull(
                loader.getResource("assets/sbwdroneconfig/textures/item/fiber_optic_spool_upgrade.png"),
                "The Fiber Optic module should package its own item texture."
        );
    }

    @Test
    void bundlesFiberOpticUpgradeRecipes() {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipes/fiber_optic_spool_upgrade.json"));
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipe/fiber_optic_spool_upgrade_vehicle_assembling.json"));
    }

    @Test
    void englishTranslationsDescribeTheFiberOpticModuleAndCableBreak() throws Exception {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("Fiber Optic Spool Upgrade", root.get("item.sbwdroneconfig.fiber_optic_spool_upgrade").getAsString());
            assertEquals("Switches a compatible drone from wireless to a physical fiber link.", root.get("tooltip.sbwdroneconfig.fiber_optic_spool_upgrade.1").getAsString());
            assertEquals("Install in the drone inventory before connecting.", root.get("tooltip.sbwdroneconfig.fiber_optic_spool_upgrade.2").getAsString());
            assertEquals("Fiber optic cable snapped: FPV control disconnected.", root.get("message.sbwdroneconfig.fiber_optic_cable_broken").getAsString());
            assertEquals("Fiber optic spool destroyed: link reverted to wireless.", root.get("message.sbwdroneconfig.fiber_optic_spool_destroyed").getAsString());
        }
    }

    @Test
    void englishTranslationsExposeFiberHudLabels() throws Exception {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("LINK: FIBER", root.get("overlay.sbwdroneconfig.link_fiber").getAsString());
            assertEquals("CABLE: %s / %s", root.get("overlay.sbwdroneconfig.fiber_cable").getAsString());
            assertEquals("TENSION: %s%%", root.get("overlay.sbwdroneconfig.fiber_tension").getAsString());
            assertEquals("SPOOL: %s%%", root.get("overlay.sbwdroneconfig.fiber_spool").getAsString());
        }
    }

    @Test
    void englishTranslationsDescribeFiberCableCutByNet() throws Exception {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("Anti-drone net severed the fiber optic cable.", root.get("message.sbwdroneconfig.fiber_optic_net_cut").getAsString());
        }
    }

    @Test
    void vehicleAssemblingRecipePlacesFiberOpticUpgradeInMiscCategory() throws Exception {
        ClassLoader loader = FiberOpticModuleAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("data/sbwdroneconfig/recipe/fiber_optic_spool_upgrade_vehicle_assembling.json")) {
            assertNotNull(stream, "The Fiber Optic Spool Upgrade should have a Vehicle Assembling Table recipe.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("superbwarfare:vehicle_assembling", root.get("type").getAsString());
            assertEquals("misc", root.get("category").getAsString());

            var inputs = root.getAsJsonArray("inputs");
            assertEquals(6, inputs.size(), "The Vehicle Assembling recipe should keep the Fiber Optic upgrade in the misc tab with a compact SBW electronics list.");
            assertTrue(inputs.asList().stream().anyMatch(element -> "3 minecraft:string".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "minecraft:glass_pane".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "2 superbwarfare:longer_wire".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "2 superbwarfare:copper_plate".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "superbwarfare:cell".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "superbwarfare:monitor".equals(element.getAsString())));

            JsonObject result = root.getAsJsonObject("result");
            assertEquals("sbwdroneconfig:fiber_optic_spool_upgrade", result.get("item").getAsString());
        }
    }
}
