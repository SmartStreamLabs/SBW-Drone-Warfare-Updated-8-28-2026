package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LegacyFpvDroneRemovalTest {
    @Test
    void removesLegacyFpvDroneRegistrationAndPublicAssets() throws Exception {
        assertFalse(hasField(AddonItems.class, "FPV_DRONE"),
                "The old sbwdroneconfig:fpv_drone item should no longer be registered.");
        assertFalse(hasField(AddonEntities.class, "FPV_DRONE"),
                "The old sbwdroneconfig:fpv_drone entity should no longer be registered.");
        assertFalse(hasField(SbwDroneRangeConfig.class, "FPV_DRONE_ENTITY_ID"),
                "The old sbwdroneconfig:fpv_drone entity id constant should be removed once the legacy drone is gone.");

        ClassLoader loader = LegacyFpvDroneRemovalTest.class.getClassLoader();
        assertNull(loader.getResource("assets/sbwdroneconfig/models/item/fpv_drone.json"),
                "The old fpv_drone item model should no longer be packaged.");
        assertNull(loader.getResource("assets/sbwdroneconfig/textures/item/fpv_drone.png"),
                "The old fpv_drone item texture should no longer be packaged.");
        assertNull(loader.getResource("assets/sbwdroneconfig/textures/entity/fpv_drone.png"),
                "The old fpv_drone entity texture should no longer be packaged.");
        assertNull(loader.getResource("assets/sbwdroneconfig/geo/fpv_drone.geo.json"),
                "The old fpv_drone entity geometry should no longer be packaged.");
        assertNull(loader.getResource("nl/smartstreamlabs/sbwdroneconfig/FpvDroneItem.class"),
                "The legacy FpvDroneItem class should no longer be packaged.");
        assertNull(loader.getResource("nl/smartstreamlabs/sbwdroneconfig/FpvDroneEntity.class"),
                "The legacy FpvDroneEntity class should no longer be packaged.");
        assertNull(loader.getResource("nl/smartstreamlabs/sbwdroneconfig/FpvDroneModel.class"),
                "The legacy FpvDroneModel class should no longer be packaged.");
        assertNull(loader.getResource("nl/smartstreamlabs/sbwdroneconfig/FpvDroneRenderer.class"),
                "The legacy FpvDroneRenderer class should no longer be packaged.");
    }

    @Test
    void removesLegacyFpvDroneTranslations() throws Exception {
        ClassLoader loader = LegacyFpvDroneRemovalTest.class.getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should still be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertFalse(root.has("entity.sbwdroneconfig.fpv_drone"),
                    "The removed legacy fpv_drone entity should no longer have a translation entry.");
            assertFalse(root.has("item.sbwdroneconfig.fpv_drone"),
                    "The removed legacy fpv_drone item should no longer have a translation entry.");
        }
    }

    private static boolean hasField(Class<?> owner, String fieldName) {
        return Arrays.stream(owner.getFields()).anyMatch(field -> field.getName().equals(fieldName));
    }
}
