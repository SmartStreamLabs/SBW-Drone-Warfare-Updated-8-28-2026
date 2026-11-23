package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FiberOpticCableSegmentRegistrationTest {
    @Test
    void wiresCableSegmentClassesAndRegistryFields() throws Exception {
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/FiberOpticCableSegmentEntity.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/FiberOpticCableSegmentRenderer.class");
        assertClassResourceExists("nl/smartstreamlabs/sbwdroneconfig/FiberOpticCableSegmentLayout.class");

        Field entityField = AddonEntities.class.getField("FIBER_OPTIC_CABLE_SEGMENT");
        Field entityIdField = SbwDroneRangeConfig.class.getField("FIBER_OPTIC_CABLE_SEGMENT_ENTITY_ID");

        assertEquals("FIBER_OPTIC_CABLE_SEGMENT", entityField.getName());
        assertEquals(SbwDroneRangeConfig.MOD_ID + ":fiber_optic_cable_segment", entityIdField.get(null));
    }

    private static void assertClassResourceExists(String resourcePath) {
        assertNotNull(FiberOpticCableSegmentRegistrationTest.class.getClassLoader().getResource(resourcePath),
                resourcePath + " should exist for the Fiber Optic cable segment system.");
    }
}
