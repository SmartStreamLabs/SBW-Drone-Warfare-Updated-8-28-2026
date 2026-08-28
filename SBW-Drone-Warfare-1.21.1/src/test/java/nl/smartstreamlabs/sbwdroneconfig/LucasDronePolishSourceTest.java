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
        JsonObject rearPropeller = lucasGeometryBones().get("rear_propeller");
        assertNotNull(rearPropeller, "The LUCAS drone should keep its dedicated rear propeller bone.");

        JsonObject blade = rearPropeller.getAsJsonArray("cubes").get(0).getAsJsonObject();
        assertEquals(-0.3D, blade.getAsJsonArray("origin").get(0).getAsDouble(), 1.0E-6D,
                "The propeller blade should extend 1.5x farther left visually.");
        assertEquals(0.6D, blade.getAsJsonArray("size").get(0).getAsDouble(), 1.0E-6D,
                "The propeller blade should be about 1.5x wider visually.");
    }

    @Test
    void lucasGeometryAddsLowPolyMilitaryDetailBones() throws Exception {
        Map<String, JsonObject> bones = lucasGeometryBones();

        assertDetailBone(bones, "panel_lines", 8);
        assertDetailBone(bones, "nose_sensor_detail", 3);
        assertDetailBone(bones, "antenna_details", 3);
        assertFalse(bones.containsKey("military_markings"),
                "The LUCAS drone should not render broad white stripe overlays; markings must come from the texture, not floating cube strips.");
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
