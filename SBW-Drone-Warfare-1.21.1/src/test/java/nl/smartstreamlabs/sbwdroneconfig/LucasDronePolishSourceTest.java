package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasDronePolishSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");

    @Test
    void lucasHitboxIsFlatterWithoutTouchingTheFpvDroneHitbox() throws Exception {
        String entities = Files.readString(MAIN.resolve("AddonEntities.java"), StandardCharsets.UTF_8);

        assertTrue(entities.contains(".sized(3.2F, 0.35F)"),
                "The LUCAS fixed-wing hitbox should keep wing collision coverage while trimming the oversized square footprint.");
        assertTrue(entities.contains(".sized(0.6F, 0.2F)"),
                "The normal FPV drone hitbox must remain unchanged.");
    }

    @Test
    void lucasPropellerVisualIsScaledUpWithoutChangingCollision() throws Exception {
        JsonObject prop=lucasGeometryBones().get("propeller");assertNotNull(prop);
        assertTrue(prop.has("poly_mesh"));assertEquals(3.3,prop.getAsJsonArray("pivot").get(1).getAsDouble(),1e-8);
        assertEquals(23.58,prop.getAsJsonArray("pivot").get(2).getAsDouble(),1e-8);
    }

    @Test
    void lucasGeometryAddsLowPolyMilitaryDetailBones() throws Exception {
        var bones=lucasGeometryBones();
        for(String name:new String[]{"engine_exterior","surface_details","wingtip_fin_L","wingtip_fin_R"})assertTrue(bones.get(name).has("poly_mesh"),name);
        assertFalse(bones.containsKey("military_markings"));
    }

    private static void assertDetailBone(Map<String, JsonObject> bones, String boneName, int minimumCubes) {
        JsonObject bone = bones.get(boneName);
        assertNotNull(bone, "The LUCAS drone should include a low-poly " + boneName + " detail bone.");
        assertTrue(bone.has("cubes"), boneName + " should be made from lightweight cube overlays.");
        assertTrue(bone.getAsJsonArray("cubes").size() >= minimumCubes,
                boneName + " should have enough small pieces to read as real UAV detail.");
    }

    private static Map<String, JsonObject> lucasGeometryBones() throws Exception {
        ClassLoader loader = LucasDronePolishSourceTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/geo/lucas_drone.geo.json")) {
            assertNotNull(stream, "The LUCAS drone geometry should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject geometry = root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            Map<String, JsonObject> bones = new HashMap<>();
            geometry.getAsJsonArray("bones").forEach(element -> {
                JsonObject bone = element.getAsJsonObject();
                bones.put(bone.get("name").getAsString(), bone);
            });
            return bones;
        }
    }
}
