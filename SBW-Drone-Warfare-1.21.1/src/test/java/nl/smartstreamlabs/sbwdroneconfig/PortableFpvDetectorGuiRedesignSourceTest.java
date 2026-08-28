package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortableFpvDetectorGuiRedesignSourceTest {
    @Test
    void detectorRaisedViewUsesTheHeldItemInsteadOfAVanillaScreen() throws Exception {
        Path screenPath = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/PortableFpvDetectorScreen.java");
        String client = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneJammerClient.java"));
        String config = Files.readString(Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonConfig.java"));

        assertFalse(Files.exists(screenPath), "The old raised detector Screen/tablet renderer should be removed.");
        assertTrue(client.contains("RenderHandEvent"), "The raised receiver should replace the detector hand render.");
        assertTrue(client.contains("event.setCanceled(true)"), "The normal detector item render must be suppressed while raised.");
        assertTrue(client.contains("ItemDisplayContext.FIXED"), "The raised view should render the existing physical item model, not the first-person flat icon.");
        assertTrue(client.contains("renderRaisedDetectorModel"), "The actual held detector model should be moved into view.");
        assertTrue(client.contains("float scale = Mth.lerp(progress, 0.46F, 0.70F)"), "The raised held item should be much smaller than the giant first pass.");
        assertTrue(client.contains("renderRaisedDetectorDisplay"), "The detector screen contents should be drawn over the raised item screen region.");
        assertTrue(client.contains("InputEvent.MouseScrollingEvent"), "Mouse wheel should switch targets without a visible GUI list.");
        assertTrue(client.contains("GLFW_KEY_LEFT"));
        assertTrue(client.contains("GLFW_KEY_RIGHT"));
        assertTrue(client.contains("GLFW_KEY_S"));
        assertTrue(client.contains("GLFW_KEY_V"));
        assertTrue(client.contains("WHEEL/ARROWS  S SORT  V VIDEO  ESC CLOSE"), "Controls should be tiny temporary hints, not buttons.");
        assertTrue(client.contains("FPV RX"), "The physical display should use the compact receiver label.");
        assertTrue(client.contains("NO SIG"), "The empty state should be short enough for the item screen.");
        assertTrue(client.contains("INT"), "The live feed should use the compact intercepted-video indicator.");
        assertTrue(client.contains("PortableFpvDetectorVideoRenderer.render"), "The raised item display should use the live video renderer.");
        assertTrue(client.contains("int rightReserved = 46"), "The live video overlay should reserve space so text cannot overlap the signal bars.");
        assertTrue(client.contains("drawRightScaled(guiGraphics, font, direction.cardinal(), x + w - 4, bottomY + 5"), "The small live feed should use only the compact direction label near the signal bars.");
        assertFalse(client.contains("direction.vertical() + \" \" + Math.round"), "The small live feed should not draw altitude text over the signal bars.");
        assertTrue(config.contains("portableFpvDetectorShowSignalPercent"));

        assertFalse(client.contains("net.minecraft.client.gui.components.Button"), "Vanilla buttons should not be used in the raised receiver.");
        assertFalse(client.contains("Button.builder"), "Vanilla button builders should not appear in the raised receiver.");
        assertFalse(client.contains("DeviceControl"), "On-screen detector buttons should be gone.");
        assertFalse(client.contains("renderRaisedArm"), "The raised detector should not render a detached player arm.");
        assertFalse(client.contains("renderRightHand"), "The raised detector should not push the player's arm into view.");
        assertFalse(client.contains("renderLeftHand"), "The raised detector should not push the player's arm into view.");
        assertFalse(client.contains("\"EXIT\""), "The old EXIT button should be gone.");
        assertFalse(client.contains("\"TARGETS\""), "The permanent target-list panel should be gone.");
        assertFalse(client.contains("renderBackground(guiGraphics"), "The raised receiver should not use a vanilla full-screen menu background.");
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
        assertTrue(renderer.contains("MainRenderTargetSwap.swap"), "The nested level render should be routed away from the real main framebuffer.");
        assertTrue(renderer.contains("mainTargetSwap.close()"), "The real main framebuffer should be restored after the nested level render.");
        assertTrue(renderer.contains("minecraft.setCameraEntity(restoredCameraEntity"), "The player camera entity should be restored.");
        assertTrue(renderer.contains("minecraft.gameRenderer.getMainCamera().setup"), "The main camera pose should be restored after the offscreen drone render.");
        assertTrue(renderer.contains("RenderSystem.restoreProjectionMatrix()"), "The GUI projection should be restored.");
        assertTrue(renderer.contains("RenderSystem.disableScissor()"), "Scissor state should be cleared after the video viewport render.");
        assertTrue(renderer.contains("RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)"), "Shader color should be reset.");
        assertTrue(context.contains("beginReadOnlyCameraIntercept"));
        assertTrue(context.contains("endReadOnlyCameraIntercept"));
    }
}
