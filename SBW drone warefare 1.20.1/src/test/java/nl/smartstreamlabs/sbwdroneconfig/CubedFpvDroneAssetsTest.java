package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CubedFpvDroneAssetsTest {
    @Test
    void bundlesCubedDroneResources() {
        ClassLoader loader = CubedFpvDroneAssetsTest.class.getClassLoader();

        assertNotNull(loader.getResource("assets/sbwdroneconfig/geo/cubed_fpv_drone.geo.json"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/entity/cubed_fpv_drone.png"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/textures/item/cubed_fpv_drone.png"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/models/item/cubed_fpv_drone.json"));
        assertNotNull(loader.getResource("assets/sbwdroneconfig/sounds/entity/cubed_fpv_drone_engine.ogg"));
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipes/cubed_fpv_drone.json"));
        assertNotNull(loader.getResource("data/sbwdroneconfig/recipes/cubed_fpv_drone_vehicle_assembling.json"));
    }

    @Test
    void cubedDroneBodyUsesPolyMeshBodyAndAnimatedRotorMeshes() throws Exception {
        ClassLoader loader = CubedFpvDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/geo/cubed_fpv_drone.geo.json")) {
            assertNotNull(stream, "Cubed drone geometry should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject geometry = root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            JsonObject[] bodyMesh = {null};
            int[] animatedRotorBones = {0};

            geometry.getAsJsonArray("bones").forEach(element -> {
                JsonObject bone = element.getAsJsonObject();
                String name = bone.get("name").getAsString();

                if ("main_body".equals(name)) {
                    bodyMesh[0] = bone;
                }
                if (name.startsWith("wing")) {
                    assertNotNull(bone.get("poly_mesh"), "Animated rotor bones should use their imported rotor meshes.");
                    assertFalse(bone.has("cubes"), "Animated rotor bones should not fall back to the temporary cube blades.");
                    JsonObject rotorMesh = bone.getAsJsonObject("poly_mesh");
                    assertTrue(rotorMesh.getAsJsonArray("polys").size() >= 40, "Rotor bones should keep the full corner propeller geometry instead of only the support arms.");

                    var pivot = bone.getAsJsonArray("pivot");
                    double pivotX = pivot.get(0).getAsDouble();
                    double pivotZ = pivot.get(2).getAsDouble();
                    assertTrue(Math.hypot(pivotX, pivotZ) > 3.0D, "Rotor pivots should stay out at the propeller corners, not near the center support arms.");
                    animatedRotorBones[0]++;
                }
            });

            assertNotNull(bodyMesh[0], "The cubed drone should keep a dedicated main body bone.");
            assertNotNull(bodyMesh[0].get("poly_mesh"), "The cubed drone body should use the restored GLB poly mesh.");
            assertFalse(bodyMesh[0].has("cubes"), "The main body should no longer fall back to the temporary cube stack.");

            JsonObject polyMesh = bodyMesh[0].getAsJsonObject("poly_mesh");
            assertTrue(polyMesh.getAsJsonArray("positions").size() > 0, "The restored body mesh should contain flat vertex positions.");
            assertTrue(polyMesh.getAsJsonArray("normals").size() > 0, "The restored body mesh should contain flat vertex normals.");
            assertTrue(polyMesh.getAsJsonArray("uvs").size() > 0, "The restored body mesh should contain flat UVs.");
            assertTrue(polyMesh.getAsJsonArray("polys").size() > 0, "The restored body mesh should contain indexed polys.");
            assertTrue(animatedRotorBones[0] == 4, "The cubed drone should expose four animated rotor bones so the propellers can spin again.");
        }
    }

    @Test
    void soundsJsonRegistersCubedDroneEngineSound() throws Exception {
        ClassLoader loader = CubedFpvDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/sounds.json")) {
            assertNotNull(stream, "sounds.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertTrue(root.has("cubed_fpv_drone_engine"), "The Cubed FPV drone engine sound event should be registered.");

            JsonObject soundDefinition = root.getAsJsonObject("cubed_fpv_drone_engine");
            assertTrue(soundDefinition.getAsJsonArray("sounds").size() > 0, "The Cubed FPV drone engine sound should point to at least one bundled sound file.");
            String soundName = soundDefinition.getAsJsonArray("sounds").get(0).getAsJsonObject().get("name").getAsString();
            assertTrue(soundName.contains("cubed_fpv_drone_engine"), "The Cubed FPV drone engine sound should reference the bundled Cubed FPV sound asset.");
        }
    }

    @Test
    void englishTranslationsRenameCubedDroneToFpvDrone() throws Exception {
        ClassLoader loader = CubedFpvDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("FPV Drone", root.get("entity.sbwdroneconfig.cubed_fpv_drone").getAsString());
            assertEquals("FPV Drone", root.get("item.sbwdroneconfig.cubed_fpv_drone").getAsString());
        }
    }

    @Test
    void cubedDroneEngineSoundMatchesLatestSourceAudio() throws Exception {
        ClassLoader loader = CubedFpvDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/sounds/entity/cubed_fpv_drone_engine.ogg")) {
            assertNotNull(stream, "The Cubed FPV drone engine sound should be packaged.");
            assertEquals(
                    "2c8819e9f7b1c9d5b2e1f269f5b3396717e3d4b46b537f7423ba9390b25d26f0",
                    sha256Hex(stream.readAllBytes()),
                    "The packaged Cubed FPV drone sound should match the latest fpv_engine_full.ogg source audio."
            );
        }
    }

    @Test
    void vehicleAssemblingRecipePlacesFpvDroneInMiscCategory() throws Exception {
        ClassLoader loader = CubedFpvDroneAssetsTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("data/sbwdroneconfig/recipes/cubed_fpv_drone_vehicle_assembling.json")) {
            assertNotNull(stream, "The Cubed FPV drone should have a Vehicle Assembling Table recipe.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("superbwarfare:vehicle_assembling", root.get("type").getAsString());
            assertEquals("misc", root.get("category").getAsString());

            var inputs = root.getAsJsonArray("inputs");
            assertEquals(5, inputs.size(), "The Vehicle Assembling recipe should stay compact and use Superb Warfare drone components.");
            assertTrue(inputs.asList().stream().anyMatch(element -> "4 superbwarfare:propeller".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "2 superbwarfare:motor".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "superbwarfare:battery".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "superbwarfare:monitor".equals(element.getAsString())));
            assertTrue(inputs.asList().stream().anyMatch(element -> "superbwarfare:drone".equals(element.getAsString())));

            JsonObject result = root.getAsJsonObject("result");
            assertEquals("sbwdroneconfig:cubed_fpv_drone", result.get("item").getAsString());
            assertFalse(result.has("entity"), "The FPV drone Vehicle Assembling recipe should output the addon item, not spawn an entity directly.");
        }
    }

    private static String sha256Hex(byte[] bytes) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }
}
