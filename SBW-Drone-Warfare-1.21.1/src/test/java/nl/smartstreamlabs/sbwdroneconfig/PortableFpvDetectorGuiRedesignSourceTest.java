package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortableFpvDetectorGuiRedesignSourceTest {
    @Test
    void detectorScreenIsACompactPhysicalReceiverInsteadOfAVanillaMenu() throws Exception {
        String screen = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/PortableFpvDetectorScreen.java"));
        String config = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonConfig.java"));

        assertTrue(screen.contains("drawReceiverBody"), "The detector screen should render a physical receiver body.");
        assertTrue(screen.contains("renderAntennas"), "The receiver should include visible antenna geometry.");
        assertTrue(screen.contains("renderTopStatusBar"), "The display should have a compact receiver status bar.");
        assertTrue(screen.contains("renderTargetList"), "Targets should be rendered in a compact left-side list.");
        assertTrue(screen.contains("targetListW = Math.round(displayW * 0.285F)"), "The target list should use a small share of the display.");
        assertTrue(screen.contains("renderVideoPanel"), "The intercepted video panel should be the main content area.");
        assertTrue(screen.contains("PortableFpvDetectorVideoRenderer.render"), "The screen should use the live video renderer.");
        assertTrue(screen.contains("DeviceControl"), "Controls should be custom device controls, not vanilla buttons.");
        assertTrue(screen.contains("NO WIRELESS SIGNALS"), "The empty state should be explicit and compact.");
        assertTrue(screen.contains("INTERCEPT"), "The live feed should be labeled inside the video panel.");
        assertTrue(screen.contains("int rightReserved = 56"), "The live video overlay should reserve space so text cannot overlap the signal bars.");
        assertTrue(screen.contains("drawRightScaled"), "Right-side live video labels should be right aligned instead of drawn through the title.");
        assertFalse(screen.contains("x + w - 48"), "The live feed distance should not use a fixed left coordinate that overlaps the title.");
        assertFalse(screen.contains("x + w - 35"), "The live feed signal label should not be drawn over the signal bars.");
        assertTrue(screen.contains("AddonConfig.portableFpvDetectorShowSignalPercent()"), "Numeric signal percentages should be configurable.");
        assertTrue(config.contains("portableFpvDetectorShowSignalPercent"));

        assertFalse(screen.contains("net.minecraft.client.gui.components.Button"), "Vanilla buttons should not be used in the redesigned receiver.");
        assertFalse(screen.contains("Button.builder"), "Vanilla button builders should not appear in the redesigned receiver.");
        assertFalse(screen.contains("renderBackground(guiGraphics"), "The redesigned receiver should not use the vanilla full-screen menu background.");
    }

    @Test
    void videoRendererRestoresRenderStateAndGuardsAgainstRecursiveCameraFeeds() throws Exception {
        String renderer = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/PortableFpvDetectorVideoRenderer.java"));
        String context = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneClientViewContext.java"));

        assertTrue(renderer.contains("private static boolean rendering"), "The video renderer should guard recursive feed rendering.");
        assertTrue(renderer.contains("if (rendering"), "Recursive render attempts should be rejected.");
        assertFalse(renderer.contains("Boolean.getBoolean"), "Live detector video should be enabled by default.");
        assertTrue(renderer.contains("finally"), "Render-state cleanup should be protected by finally.");
        assertTrue(renderer.contains("mainTarget.bindWrite(true)"), "The main framebuffer should be rebound after offscreen rendering.");
        assertTrue(renderer.contains("minecraft.setCameraEntity(restoredCameraEntity"), "The player camera entity should be restored.");
        assertTrue(renderer.contains("minecraft.gameRenderer.getMainCamera().setup"), "The main camera pose should be restored after the offscreen drone render.");
        assertTrue(renderer.contains("RenderSystem.restoreProjectionMatrix()"), "The GUI projection should be restored.");
        assertTrue(renderer.contains("RenderSystem.disableScissor()"), "Scissor state should be cleared after the video viewport render.");
        assertTrue(renderer.contains("RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)"), "Shader color should be reset.");
        assertTrue(context.contains("beginReadOnlyCameraIntercept"));
        assertTrue(context.contains("endReadOnlyCameraIntercept"));
    }
}
