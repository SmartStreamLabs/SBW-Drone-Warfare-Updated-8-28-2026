package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableVisibleRendererSourceTest {
    @Test
    void dedicatedCableRendererUsesCameraRelativeContinuousCableRendering() throws Exception {
        Path rendererSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticCableRenderer.java"
        );
        String source = Files.readString(rendererSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS"));
        assertTrue(source.contains("event.getCamera().getPosition()"));
        assertTrue(source.contains("poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)"));
        assertTrue(source.contains("DroneFiberOpticClient.routePointsFor(state)"));
        assertTrue(source.contains("renderedDroneIds"));
        assertTrue(source.contains("trimDroneEndRoutePoints"));
        assertTrue(source.contains("staleInsideDrone"));
        assertTrue(source.contains("FiberOpticCableRenderMath.buildRouteCurvePoints"));
        assertTrue(source.contains("return 96;"));
        assertTrue(source.contains("return 64;"));
        assertTrue(source.contains("return 32;"));
        assertTrue(source.contains("AddonConfig.debugFiberCableRender()"));
        assertTrue(source.contains("AddonConfig.fiberCableVisualThickness()"));
        assertTrue(source.contains("AddonConfig.fiberCableVisualAlpha()"));
        assertTrue(source.contains("AddonConfig.fiberCableVisualColor()"));
        assertTrue(source.contains("AddonConfig.fiberCableDebugVisualThickness()"));
        assertTrue(source.contains("AddonConfig.renderRedDebugCable()"));
        assertTrue(source.contains("RenderType.leash()"));
        assertFalse(source.contains("renderSingleBlock("));
    }

    @Test
    void mainModInitializesTheDedicatedCableRenderer() throws Exception {
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

        assertTrue(source.contains("FiberOpticCableRenderer.init(modBus);"),
                "The dedicated visible cable renderer should be initialized on the client side.");
    }
}
