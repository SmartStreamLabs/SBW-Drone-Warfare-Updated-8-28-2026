package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasDroneAssetsTest {
    @Test
    void bundlesLucasDroneResources() {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();

        assertNotNull(loader.getResource("assets/sbwdroneconfig/geo/lucas_drone.geo.json"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/entity/lucas_drone.png"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/item/lucas_drone.png"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/lucas_drone.json"));
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipes/lucas_drone.json"));
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipes/lucas_drone_vehicle_assembling.json"));
        assertNotNull(loader.getResource("data/sbwdroneconfig/sbw/vehicles/lucas_drone.json"));
    }

    @Test
    void lucasDroneGeometryDoesNotStartWithUtf8Bom() throws Exception {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/geo/lucas_drone.geo.json")) {
            assertNotNull(stream, "The LUCAS drone geometry should be packaged.");

            byte[] bytes = stream.readNBytes(3);
            boolean hasUtf8Bom = bytes.length == 3
                    && (bytes[0] & 0xFF) == 0xEF
                    && (bytes[1] & 0xFF) == 0xBB
                    && (bytes[2] & 0xFF) == 0xBF;

            assertFalse(hasUtf8Bom, "The LUCAS drone geometry must not start with a UTF-8 BOM because GeckoLib fails to parse it at runtime.");
        }
    }

    @Test
    void lucasDroneGeometryKeepsTheImportedPolyMeshSilhouette() throws Exception {
        try(var stream=getClass().getClassLoader().getResourceAsStream("assets/sbwdroneconfig/geo/lucas_drone.geo.json")){
            var geometry=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            assertEquals("geometry.sbwdroneconfig.lucas_drone",geometry.getAsJsonObject("description").get("identifier").getAsString());
            var map=new java.util.HashMap<String,JsonObject>();geometry.getAsJsonArray("bones").forEach(x->map.put(x.getAsJsonObject().get("name").getAsString(),x.getAsJsonObject()));
            for(String name:new String[]{"fuselage","nose_section","delta_wing_L","delta_wing_R","engine_exterior","propeller"})assertTrue(map.get(name).getAsJsonObject("poly_mesh").getAsJsonArray("polys").size()>0,name);
            assertEquals(23.58,map.get("propeller").getAsJsonArray("pivot").get(2).getAsDouble(),1e-8);
            assertEquals("lc01_root",map.get("engine_exterior").get("parent").getAsString());
        }
    }

    @Test
    void itemModelAndRecipesTargetTheLucasDroneItem() throws Exception {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/lucas_drone.json")) {
            assertNotNull(stream, "The LUCAS drone item model should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("minecraft:item/generated", root.get("parent").getAsString());
            assertEquals("sbwdroneconfig:item/lucas_drone", root.getAsJsonObject("textures").get("layer0").getAsString());
        }

        try (var stream = loader.getResourceAsStream("data/sbwdroneconfig/recipes/lucas_drone.json")) {
            assertNotNull(stream, "The LUCAS drone should have a manual crafting recipe.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("minecraft:crafting_shaped", root.get("type").getAsString());
            assertEquals("sbwdroneconfig:lucas_drone", root.getAsJsonObject("result").get("id").getAsString());
            assertEquals("superbwarfare:drone", root.getAsJsonObject("key").getAsJsonObject("D").get("item").getAsString());
        }

        try (var stream = loader.getResourceAsStream("data/sbwdroneconfig/recipes/lucas_drone_vehicle_assembling.json")) {
            assertNotNull(stream, "The LUCAS drone should have a Vehicle Assembling Table recipe.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("superbwarfare:vehicle_assembling", root.get("type").getAsString());
            assertEquals("misc", root.get("category").getAsString());
            assertEquals("sbwdroneconfig:lucas_drone", root.getAsJsonObject("result").get("item").getAsString());
        }
    }

    @Test
    void lucasVehicleDataOverridesTheFallbackGravity() throws Exception {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("data/sbwdroneconfig/sbw/vehicles/lucas_drone.json")) {
            assertNotNull(stream, "The LUCAS drone should ship dedicated Superb Warfare vehicle data instead of falling back to the default gravity.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("sbwdroneconfig:lucas_drone", root.get("ID").getAsString());
            assertEquals("Drone", root.get("Type").getAsString());
            assertEquals(0.0D, root.get("Gravity").getAsDouble(), 1.0E-6D,
                    "The LUCAS drone needs addon-defined gravity so the fixed-wing takeoff lift is not canceled by Superb Warfare's fallback vehicle gravity.");
        }
    }

    @Test
    void englishTranslationsNameTheLucasDroneAndDescribeItsSharedSystems() throws Exception {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("LUCAS Drone", root.get("entity.sbwdroneconfig.lucas_drone").getAsString());
            assertEquals("LUCAS Drone", root.get("item.sbwdroneconfig.lucas_drone").getAsString());
            assertEquals("Gasoline-powered fixed-wing attack drone.", root.get("tooltip.sbwdroneconfig.lucas_drone.1").getAsString());
            assertEquals("Use a linked Monitor and FPV Goggles; refuel before powered flight.", root.get("tooltip.sbwdroneconfig.lucas_drone.2").getAsString());
        }
    }

    @Test
    void jerrycanItemsUseConvertedPhysicalModelAssets() throws Exception {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();

        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/jerrycan.obj"),
                "The jerrycan GLB should be converted into a Forge OBJ model.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/gasoline_jerrycan.mtl"),
                "The gasoline jerrycan should have a dedicated material library.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/empty_jerrycan.mtl"),
                "The empty jerrycan should have a dedicated material library.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/item/gasoline_jerrycan_3d.png"),
                "The gasoline jerrycan should package the converted red jerrycan texture as PNG.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/item/empty_jerrycan_3d.png"),
                "The empty jerrycan should package a distinct neutral empty texture as PNG.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/item/gasoline_jerrycan_icon.png"),
                "The gasoline jerrycan should package a separate flat inventory icon.");
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/item/empty_jerrycan_icon.png"),
                "The empty jerrycan should package a separate flat inventory icon.");

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/gasoline_jerrycan.json")) {
            assertNotNull(stream, "The gasoline jerrycan item model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("neoforge:separate_transforms", root.get("loader").getAsString());
            assertEquals("neoforge:obj", root.getAsJsonObject("base").get("loader").getAsString());
            assertEquals("sbwdroneconfig:models/item/jerrycan.obj", root.getAsJsonObject("base").get("model").getAsString());
            assertEquals("sbwdroneconfig:models/item/gasoline_jerrycan.mtl", root.getAsJsonObject("base").get("mtl_override").getAsString());
        }

        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/models/item/empty_jerrycan.json")) {
            assertNotNull(stream, "The empty jerrycan item model should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("neoforge:separate_transforms", root.get("loader").getAsString());
            assertEquals("neoforge:obj", root.getAsJsonObject("base").get("loader").getAsString());
            assertEquals("sbwdroneconfig:models/item/jerrycan.obj", root.getAsJsonObject("base").get("model").getAsString());
            assertEquals("sbwdroneconfig:models/item/empty_jerrycan.mtl", root.getAsJsonObject("base").get("mtl_override").getAsString());
        }
    }

    @Test
    void jerrycanInventoryIconsUseSeparateFlatGuiModels() throws Exception {
        assertJerrycanSeparateGuiModel(
                "assets/sbwdroneconfig/models/item/gasoline_jerrycan.json",
                "sbwdroneconfig:item/gasoline_jerrycan_icon",
                "sbwdroneconfig:models/item/gasoline_jerrycan.mtl");
        assertJerrycanSeparateGuiModel(
                "assets/sbwdroneconfig/models/item/empty_jerrycan.json",
                "sbwdroneconfig:item/empty_jerrycan_icon",
                "sbwdroneconfig:models/item/empty_jerrycan.mtl");
    }

    @Test
    void emptyJerrycanTextureIsReadableInsteadOfNearlyBlack() throws Exception {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/textures/item/empty_jerrycan_3d.png")) {
            assertNotNull(stream, "The empty jerrycan texture should be packaged.");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "The empty jerrycan texture should be readable as a PNG.");

            long red = 0;
            long green = 0;
            long blue = 0;
            long samples = 0;
            for (int y = 0; y < image.getHeight(); y += 8) {
                for (int x = 0; x < image.getWidth(); x += 8) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    if (alpha == 0) {
                        continue;
                    }
                    red += (argb >>> 16) & 0xFF;
                    green += (argb >>> 8) & 0xFF;
                    blue += argb & 0xFF;
                    samples++;
                }
            }

            assertTrue(samples > 0, "The empty jerrycan texture should contain visible pixels.");
            double averageBrightness = (red + green + blue) / (samples * 3.0D);
            assertTrue(averageBrightness >= 85.0D,
                    "The empty jerrycan texture should be bright enough to read in an inventory slot.");
        }
    }

    private static void assertImportedPolyMesh(JsonObject bone, String name, int minimumPolys) {
        assertNotNull(bone, "The LUCAS drone should keep the imported " + name + " bone.");
        assertNotNull(bone.get("poly_mesh"), "The " + name + " bone should use the imported poly mesh.");
        assertFalse(bone.has("cubes"), "The " + name + " bone should not fall back to placeholder cubes.");

        JsonObject polyMesh = bone.getAsJsonObject("poly_mesh");
        assertTrue(polyMesh.getAsJsonArray("positions").size() > 0, "The " + name + " bone should include flat vertex positions.");
        assertTrue(polyMesh.getAsJsonArray("normals").size() > 0, "The " + name + " bone should include flat vertex normals.");
        assertTrue(polyMesh.getAsJsonArray("uvs").size() > 0, "The " + name + " bone should include flat UVs.");
        assertTrue(polyMesh.getAsJsonArray("polys").size() >= minimumPolys, "The " + name + " bone should preserve the imported mesh silhouette.");
    }

    private static void assertJerrycanSeparateGuiModel(String resourcePath, String expectedIcon, String expectedMaterial) throws Exception {
        ClassLoader loader = LucasDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream(resourcePath)) {
            assertNotNull(stream, resourcePath + " should be packaged.");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("neoforge:separate_transforms", root.get("loader").getAsString(),
                    "Jerrycan items should use a flat generated GUI icon instead of forcing the OBJ into a 16x16 slot.");
            assertEquals(expectedMaterial, root.getAsJsonObject("base").get("mtl_override").getAsString());

            JsonObject gui = root.getAsJsonObject("perspectives").getAsJsonObject("gui");
            assertEquals("minecraft:item/generated", gui.get("parent").getAsString());
            assertEquals(expectedIcon, gui.getAsJsonObject("textures").get("layer0").getAsString());
        }
    }

    private static int countLegacyTailPropellerPolys(JsonObject polyMesh) {
        var positions = polyMesh.getAsJsonArray("positions");
        var polys = polyMesh.getAsJsonArray("polys");
        int matches = 0;

        for (var polyElement : polys) {
            var poly = polyElement.getAsJsonArray();
            double minZ = Double.POSITIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;
            double maxAbsX = 0.0D;
            double maxAbsY = 0.0D;

            for (var vertexElement : poly) {
                int vertexIndex = vertexElement.getAsJsonArray().get(0).getAsInt();
                double x = positions.get(vertexIndex * 3).getAsDouble();
                double y = positions.get(vertexIndex * 3 + 1).getAsDouble();
                double z = positions.get(vertexIndex * 3 + 2).getAsDouble();

                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
                maxAbsX = Math.max(maxAbsX, Math.abs(x));
                maxAbsY = Math.max(maxAbsY, Math.abs(y));
            }

            if (maxZ < -0.457D
                    && minZ > -0.478D
                    && maxAbsX > 0.05D
                    && maxAbsX < 0.25D
                    && maxAbsY < 0.02D) {
                matches++;
            }
        }

        return matches;
    }
}
