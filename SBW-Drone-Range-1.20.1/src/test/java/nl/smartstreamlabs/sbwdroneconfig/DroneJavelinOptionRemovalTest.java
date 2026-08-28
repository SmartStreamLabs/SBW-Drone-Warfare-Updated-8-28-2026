package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class DroneJavelinOptionRemovalTest {
    @Test
    void javelinMissileIsNotContributedAsADroneAttachmentOption() {
        ClassLoader loader = DroneJavelinOptionRemovalTest.class.getClassLoader();

        assertNull(
                loader.getResource("data/sbwdroneconfig/sbw/drone_attachments/javelin_missile.json"),
                "The addon should no longer add a Javelin missile payload option to the drone attachment list."
        );
    }
}
