package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticHudVisibilitySourceTest {
    @Test
    void fiberHudRequiresTheActiveMonitorToStillControlTheSyncedDrone() throws Exception {
        Path clientSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "DroneFiberOpticClient.java"
        );
        String source = Files.readString(clientSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("ItemStack activeMonitor = SbwCompat.getActiveLinkedMonitor(player);"),
                "The Fiber HUD should look at the active monitor before rendering so stale synced state cannot stay on screen.");
        assertTrue(source.contains("return activeMonitor.isEmpty() ? null : SbwCompat.getLinkedDroneUuid(activeMonitor);"),
                "The Fiber HUD should resolve the currently controlled drone from the active monitor.");
        assertTrue(source.contains("linkedDroneId != null && linkedDroneId.equals(hudState.droneId())"),
                "The Fiber HUD should only render when the active monitor still targets the synced fiber drone.");
    }
}
