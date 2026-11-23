package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CubedFpvDroneRegistrationTest {
    @Test
    void wiresVariantClassesAndRegistryFields() throws Exception {
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/CubedFpvDroneItem.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/CubedFpvDroneEntity.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/CubedFpvDroneModel.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/CubedFpvDroneRenderer.class");

        Field itemField = AddonItems.class.getField("CUBED_FPV_DRONE");
        Field entityField = AddonEntities.class.getField("CUBED_FPV_DRONE");
        Field entityIdField = SbwDroneRangeConfig.class.getField("CUBED_FPV_DRONE_ENTITY_ID");

        assertEquals("CUBED_FPV_DRONE", itemField.getName());
        assertEquals("CUBED_FPV_DRONE", entityField.getName());
        assertEquals(SbwDroneRangeConfig.MOD_ID + ":" + CubedFpvDroneDefinition.ID, entityIdField.get(null));
    }

    private static void assertClassResourceExists(String resourcePath) {
        assertNotNull(CubedFpvDroneRegistrationTest.class.getClassLoader().getResource(resourcePath),
                resourcePath + " should exist for the cubed drone variant.");
    }
}
