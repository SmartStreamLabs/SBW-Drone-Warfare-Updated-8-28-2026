package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LucasDroneRegistrationTest {
    @Test
    void wiresVariantClassesAndRegistryFields() throws Exception {
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/LucasDroneDefinition.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/LucasDroneItem.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/LucasDroneEntity.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/LucasDroneModel.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/LucasDroneRenderer.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/LucasDroneRenderMath.class");

        Field itemField = AddonItems.class.getField("LUCAS_DRONE");
        Field entityField = AddonEntities.class.getField("LUCAS_DRONE");
        Field entityIdField = SbwDroneRangeConfig.class.getField("LUCAS_DRONE_ENTITY_ID");
        Class<?> definitionClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.LucasDroneDefinition");
        Field idField = definitionClass.getField("ID");

        assertEquals("LUCAS_DRONE", itemField.getName());
        assertEquals("LUCAS_DRONE", entityField.getName());
        assertEquals(SbwDroneRangeConfig.MOD_ID + ":" + idField.get(null), entityIdField.get(null));
    }

    private static void assertClassResourceExists(String resourcePath) {
        assertNotNull(LucasDroneRegistrationTest.class.getClassLoader().getResource(resourcePath),
                resourcePath + " should exist for the LUCAS drone variant.");
    }
}
