package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticSpoolVisualSourceTest {
    @Test
    void cubedFpvRendererAddsConditionalSpoolModuleWithoutControls() throws Exception {
        String renderer = readMainSource("CubedFpvDroneRenderer.java");

        assertTrue(renderer.contains("renderFiberSpoolModule"));
        assertTrue(renderer.contains("renderOctagonalModuleBodyZ"));
        assertTrue(renderer.contains("renderOctagonalDiskZ"));
        assertTrue(renderer.contains("CubedFpvFiberSpoolGeometry.HOUSING_SEGMENTS"));
        assertTrue(renderer.contains("FiberSpoolHousingModel.render"));
        assertTrue(renderer.contains("DroneFiberOpticClient.hasFiberSpoolModule"));
        assertTrue(renderer.contains("CubedFpvFiberSpoolGeometry.CABLE_EXIT_LOCAL"));
        assertFalse(renderer.contains("renderOctagonalModuleBodyX"));
        assertFalse(renderer.contains("renderSpoolDetailLines"));
        assertFalse(renderer.contains("CENTER_SPOOL"));
        assertFalse(renderer.contains("SPOOL_GROOVE_COLOR"));
        assertFalse(renderer.contains("setMouseMoveSpeedX"));
        assertFalse(renderer.contains("setPower("));
    }

    @Test
    void spoolGeometryDefinesNamedPartAndCableExitPoint() throws Exception {
        String geometry = readMainSource("CubedFpvFiberSpoolGeometry.java");

        assertTrue(geometry.contains("fiber_spool_module"));
        assertTrue(geometry.contains("enclosed_spool_housing"));
        assertTrue(geometry.contains("left_spool_cap"));
        assertTrue(geometry.contains("right_spool_cap"));
        assertTrue(geometry.contains("center_hub_left"));
        assertTrue(geometry.contains("center_hub_right"));
        assertTrue(geometry.contains("protective_frame_bottom"));
        assertTrue(geometry.contains("rear_cable_outlet"));
        assertTrue(geometry.contains("orange_connector"));
        assertTrue(geometry.contains("fiber_cable_exit"));
        assertTrue(geometry.contains("POD_CENTER_X = 0.000D"));
        assertTrue(geometry.contains("MAIN_HOUSING_MIN_Z"));
        assertTrue(geometry.contains("new Vec3(-0.057D, -0.033D, 0.000D)"));
        assertTrue(geometry.contains("new Vec3(-0.089D, -0.045D, 0.000D)"));
        assertTrue(geometry.contains("CABLE_EXIT_LOCAL"));
        assertTrue(geometry.contains("droneVisualScale()"));
        assertTrue(geometry.contains("renderYawDegrees"));
        assertTrue(geometry.contains("renderBodyPitchDegrees"));
        assertTrue(geometry.contains("renderRollDegrees"));
        assertFalse(geometry.contains("CENTER_SPOOL"));
        assertFalse(geometry.contains("SPOOL_SEGMENTS"));
        assertFalse(geometry.contains("RECESSED_HUB"));
        assertFalse(geometry.contains("HOUSING_MIN ="));
        assertFalse(geometry.contains("LEFT_SIDE_PLATE_MIN"));
        assertFalse(geometry.contains("RIGHT_SIDE_PLATE_MIN"));
    }

    @Test
    void visibleCableOriginUsesTheSpoolOutletGeometry() throws Exception {
        String math = readMainSource("FiberOpticCableRenderMath.java");

        assertTrue(math.contains("CubedFpvFiberSpoolGeometry.cableExitWorldPosition"));
        assertFalse(math.contains("CUBED_FPV_SPOOL_LOCAL_OFFSET"));
    }

    @Test
    void fiberVisualSyncIsSeparateFromActiveCableRendering() throws Exception {
        String client = readMainSource("DroneFiberOpticClient.java");
        String linkSystem = readMainSource("FiberOpticLinkSystem.java");
        String inventory = readMainSource("DroneInventoryContainer.java");

        assertTrue(client.contains("VISUAL_STATES"));
        assertTrue(client.contains("FiberVisualState"));
        assertTrue(client.contains("hasFiberSpoolModule"));
        assertTrue(linkSystem.contains("syncVisualStateForTrackingPlayers"));
        assertTrue(inventory.contains("syncVisualStateForTrackingPlayers(drone)"));
    }

    private static String readMainSource(String fileName) throws Exception {
        return Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", fileName));
    }
}
