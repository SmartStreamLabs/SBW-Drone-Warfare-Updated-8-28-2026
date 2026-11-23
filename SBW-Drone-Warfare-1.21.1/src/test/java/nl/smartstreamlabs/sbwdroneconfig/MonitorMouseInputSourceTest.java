package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitorMouseInputSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");

    @Test
    void monitorMouseRelayUsesExistingSbwPacketOnlyWhenNativeClientLookupFails() throws Exception {
        String bootstrap = Files.readString(MAIN.resolve("SbwDroneRangeConfig.java"));
        String source = Files.readString(MAIN.resolve("MonitorMouseInputClient.java"));

        assertTrue(bootstrap.contains("MonitorMouseInputClient.init(modBus)"));
        assertTrue(source.contains("ClientTickEvent.Post"));
        assertTrue(source.contains("SbwCompat.isLegacyUsingMonitor(monitor)"));
        assertTrue(source.contains("EntityFindUtil.findDrone(level, linkedDrone) != null"));
        assertTrue(source.contains("PacketDistributor.sendToServer(new MouseMoveMessage"));
        assertTrue(source.contains("DEFAULT_DRONE_MOUSE_SENSITIVITY = 0.4D"));
        assertTrue(source.contains("currentMousePos.x - previousMousePos.x"));
        assertTrue(source.contains("currentMousePos.y - previousMousePos.y"));

        assertFalse(source.contains("setYRot"));
        assertFalse(source.contains("setXRot"));
        assertFalse(source.contains("mouseInput("));
        assertFalse(source.contains("setDeltaMovement"));
        assertFalse(source.contains("addDeltaMovement"));
    }
}
