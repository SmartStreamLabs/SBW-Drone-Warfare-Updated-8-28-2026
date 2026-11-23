package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticDebugCommandSourceTest {
    @Test
    void registersSbwDroneFiberDebugCommand() throws Exception {
        Path commandSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticDebugCommand.java"
        );
        String source = Files.readString(commandSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("RegisterCommandsEvent"));
        assertTrue(source.contains("Commands.literal(\"sbwdrone\")"));
        assertTrue(source.contains("Commands.literal(\"fiberdebug\")"));
        assertTrue(source.contains("client sync last sent"));
        assertTrue(source.contains("active cable segments"));
        assertTrue(source.contains("spool module found"));
    }

    @Test
    void mainModRegistersFiberDebugCommandHandler() throws Exception {
        Path mainSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "SbwDroneRangeConfig.java"
        );
        String source = Files.readString(mainSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("MinecraftForge.EVENT_BUS.register(FiberOpticDebugCommand.class);"));
    }
}
