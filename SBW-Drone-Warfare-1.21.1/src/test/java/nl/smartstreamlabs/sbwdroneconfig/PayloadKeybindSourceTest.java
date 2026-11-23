package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadKeybindSourceTest {
    @Test
    void dropMortarPayloadDefaultsToH() throws Exception {
        String source = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DronePayloadClient.java"));

        assertTrue(source.contains("GLFW.GLFW_KEY_H"), "Payload drop key should default to H.");
        assertFalse(source.contains("GLFW.GLFW_KEY_G"), "Payload drop key should no longer default to G.");
    }
}
