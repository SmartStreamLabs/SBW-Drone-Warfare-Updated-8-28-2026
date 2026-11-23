package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntiVehicleMortarPayloadMountSourceTest {
    @Test
    void mortarPayloadDropsFromDroneInventoryInsteadOfMountedEntity() throws Exception {
        String system = readSource("DronePayloadSystem.java");
        String projectile = readSource("AntiVehicleMortarPayloadEntity.java");
        String renderer = readSource("AntiVehicleMortarPayloadRenderer.java");

        assertTrue(system.contains("new DroneInventoryContainer(drone)"),
                "The payload key should consume Anti-vehicle mortar from the drone inventory.");
        assertTrue(system.contains("consumePayloadFromDroneInventory"),
                "The drop flow should use an inventory consume helper.");
        assertTrue(system.contains("new AntiVehicleMortarPayloadEntity"),
                "Dropping should spawn a fresh falling payload entity.");
        assertTrue(system.contains("getMortarDropPosition"),
                "The payload should use a drop position, not a persistent mount point.");
        assertFalse(system.contains("TAG_MOUNTED_MORTAR_PAYLOAD_UUID"),
                "Inventory payloads should not create persistent mounted payload UUID state.");
        assertFalse(system.contains("mountMortarPayload"),
                "Right-click mounting should not be part of the restored inventory-drop flow.");
        assertFalse(system.contains("consumeMountedMortarPayload"),
                "The drop key should not require a mounted payload entity.");
        assertFalse(projectile.contains("isMounted"),
                "The payload entity should only represent the released falling mortar.");
        assertFalse(renderer.contains("entity.isMounted()"),
                "Renderer scale should no longer depend on mounted state.");
    }

    private static String readSource(String fileName) throws Exception {
        return Files.readString(Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                fileName
        ), StandardCharsets.UTF_8);
    }
}
