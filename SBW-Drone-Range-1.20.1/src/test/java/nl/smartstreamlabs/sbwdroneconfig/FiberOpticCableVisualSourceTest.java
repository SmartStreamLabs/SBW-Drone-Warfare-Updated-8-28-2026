package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableVisualSourceTest {
    @Test
    void configDefaultsSeparateThinVisualWireFromGameplayHitboxes() throws Exception {
        Path configSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "AddonConfig.java"
        );
        String source = Files.readString(configSource, StandardCharsets.UTF_8);

        assertTrue(source.contains(".defineInRange(\"fiberCableVisualThickness\", 0.015D, 0.008D, 0.025D"),
                "Fiber cable visuals should default to a 0.015 block ribbon and stay within the requested thin range.");
        assertTrue(source.contains(".defineInRange(\"fiberCableVisualAlpha\", 0.72D"),
                "Fiber cable visuals should expose a separate alpha control for the charcoal ribbon.");
        assertTrue(source.contains(".define(\"fiberCableVisualColor\", \"charcoal\""),
                "Fiber cable visuals should default to the requested charcoal color.");
        assertTrue(source.contains(".defineInRange(\"fiberCableMaxRenderDistance\", 192.0D"),
                "Fiber cable visuals should expose the requested 192 block render distance.");
        assertTrue(source.contains(".defineInRange(\"maxVisibleFiberCables\", 16"),
                "Fiber cable visuals should cap how many active ribbons render on one client.");
        assertTrue(source.contains(".defineInRange(\"fiberCableDebugVisualThickness\", 0.025D"),
                "Debug mode should keep its own thin visual without reintroducing thick pipes.");
        assertTrue(source.contains("public static double fiberCableVisualAlpha()"),
                "Runtime config access should expose the new fiber cable alpha value.");
        assertTrue(source.contains("public static String fiberCableVisualColor()"),
                "Runtime config access should expose the new fiber cable color preset.");
        assertTrue(source.contains("public static double fiberCableMaxRenderDistance()"),
                "Runtime config access should expose the new render distance.");
        assertTrue(source.contains("public static int maxVisibleFiberCables()"),
                "Runtime config access should expose the visible cable cap.");
        assertTrue(source.contains("public static double fiberCableDebugVisualThickness()"),
                "Runtime config access should expose the new debug-only fiber cable thickness.");
    }

    @Test
    void rendererUsesThinRibbonStyleAndLeavesSegmentEntitiesInvisibleByDefault() throws Exception {
        Path cableRendererSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticCableRenderer.java"
        );
        String cableRenderer = Files.readString(cableRendererSource, StandardCharsets.UTF_8);

        assertTrue(cableRenderer.contains("RenderType.leash()"),
                "The visible fiber cable should render as a thin colored ribbon/line instead of block cuboids.");
        assertTrue(cableRenderer.contains("AddonConfig.fiberCableVisualAlpha()"),
                "The visible fiber cable should use the new alpha config in normal mode.");
        assertTrue(cableRenderer.contains("AddonConfig.fiberCableDebugVisualThickness()"),
                "The debug render path should use a separate thicker config value.");
        assertTrue(!cableRenderer.contains("renderSingleBlock("),
                "The visible fiber cable should no longer render as tiny concrete blocks that look like a pipe.");

        Path segmentRendererSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticCableSegmentRenderer.java"
        );
        String segmentRenderer = Files.readString(segmentRendererSource, StandardCharsets.UTF_8);

        assertTrue(segmentRenderer.contains("Legacy save-compatibility entity: never render a visible cable segment."),
                "Legacy cable segment entities should never render now that the client ribbon owns all visuals.");
        assertTrue(!segmentRenderer.contains("LevelRenderer.renderLineBox"),
                "Legacy cable segment entities should not render debug hitbox markers in the new zero-entity visual path.");
    }
}
