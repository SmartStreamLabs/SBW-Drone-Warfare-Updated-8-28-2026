package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableRenderMathTest {
    @Test
    void prefersCapturedLaunchAnchorOverMovedControllerPosition() {
        Vec3 movedControllerPosition = new Vec3(24.0D, 91.0D, -7.0D);
        Vec3 capturedLaunchAnchor = new Vec3(10.5D, 64.0D, 10.5D);

        Vec3 storedAnchor = FiberOpticCableRenderMath.resolveStoredAnchor(movedControllerPosition, capturedLaunchAnchor);

        assertEquals(capturedLaunchAnchor.x, storedAnchor.x, 1.0E-6D);
        assertEquals(capturedLaunchAnchor.y, storedAnchor.y, 1.0E-6D);
        assertEquals(capturedLaunchAnchor.z, storedAnchor.z, 1.0E-6D);
    }

    @Test
    void fallsBackToControllerFeetWhenNoCapturedLaunchAnchorExists() {
        Vec3 controllerFeet = new Vec3(5.25D, 66.0D, -2.75D);

        Vec3 storedAnchor = FiberOpticCableRenderMath.resolveStoredAnchor(controllerFeet, null);

        assertEquals(controllerFeet.x, storedAnchor.x, 1.0E-6D);
        assertEquals(controllerFeet.y, storedAnchor.y, 1.0E-6D);
        assertEquals(controllerFeet.z, storedAnchor.z, 1.0E-6D);
    }

    @Test
    void renderEndpointsAttachToGroundAndDroneRearBottomInsteadOfCenter() {
        Vec3 storedAnchor = new Vec3(10.5D, 64.0D, 10.5D);
        Vec3 dronePosition = new Vec3(22.0D, 70.0D, 30.0D);

        Vec3 renderStart = FiberOpticCableRenderMath.toRenderAnchor(storedAnchor);
        Vec3 renderEnd = FiberOpticCableRenderMath.toRenderEnd(dronePosition, 0.0F, 0.9D, 1.4D);

        assertTrue(renderStart.y > storedAnchor.y, "The cable should rise only slightly above the stored ground anchor.");
        assertTrue(renderStart.y < storedAnchor.y + 0.3D, "The ground offset should stay subtle.");
        assertTrue(renderEnd.z < dronePosition.z, "Yaw 0 should place the cable at the drone's rear, not the nose.");
        assertTrue(renderEnd.y < dronePosition.y + (1.4D * 0.5D), "The cable should attach low on the drone body, not at the camera center.");
    }

    @Test
    void segmentedCurveSagsSlightlyWithoutDippingTooFarBelowGround() {
        Vec3 renderStart = new Vec3(0.0D, 64.12D, 0.0D);
        Vec3 renderEnd = new Vec3(18.0D, 67.5D, 2.0D);

        Vec3[] points = FiberOpticCableRenderMath.buildCableCurvePoints(renderStart, renderEnd, 10);

        assertEquals(11, points.length, "Ten segments should generate eleven bezier points.");
        assertEquals(renderStart.x, points[0].x, 1.0E-6D);
        assertEquals(renderStart.y, points[0].y, 1.0E-6D);
        assertEquals(renderStart.z, points[0].z, 1.0E-6D);
        assertEquals(renderEnd.x, points[10].x, 1.0E-6D);
        assertEquals(renderEnd.y, points[10].y, 1.0E-6D);
        assertEquals(renderEnd.z, points[10].z, 1.0E-6D);

        double straightMidY = (renderStart.y + renderEnd.y) * 0.5D;
        double minimumCurveY = Double.POSITIVE_INFINITY;
        for (Vec3 point : points) {
            minimumCurveY = Math.min(minimumCurveY, point.y);
        }

        assertTrue(minimumCurveY < straightMidY, "The cable should sag below a perfectly straight midpoint.");
        assertTrue(minimumCurveY >= Math.min(renderStart.y, renderEnd.y) - 0.35D, "The sag should stay subtle and not sink far below the ground anchor.");
    }

    @Test
    void routeCurveHandlesEmptyAnchorOnlyRepeatedVerticalAndMalformedInputs() {
        Vec3 dynamicEnd = new Vec3(4.0D, 65.0D, 4.0D);
        Vec3[] emptyRoute = FiberOpticCableRenderMath.buildRouteCurvePoints(List.of(), dynamicEnd, 32, true);

        assertEquals(1, emptyRoute.length);
        assertEquals(dynamicEnd.x, emptyRoute[0].x, 1.0E-6D);
        assertEquals(dynamicEnd.y, emptyRoute[0].y, 1.0E-6D);
        assertEquals(dynamicEnd.z, emptyRoute[0].z, 1.0E-6D);

        Vec3 anchor = new Vec3(0.0D, 64.12D, 0.0D);
        Vec3 verticalEnd = new Vec3(0.0D, 74.0D, 0.0D);
        List<Vec3> route = new ArrayList<>();
        route.add(anchor);
        route.add(anchor);
        route.add(new Vec3(Double.NaN, 64.0D, 0.0D));
        route.add(new Vec3(Double.POSITIVE_INFINITY, 64.0D, 0.0D));
        route.add(new Vec3(0.0D, 70.0D, 0.0D));

        Vec3[] points = FiberOpticCableRenderMath.buildRouteCurvePoints(route, verticalEnd, 64, true);

        assertTrue(points.length <= 64);
        assertEquals(anchor.x, points[0].x, 1.0E-6D);
        assertEquals(anchor.y, points[0].y, 1.0E-6D);
        assertEquals(anchor.z, points[0].z, 1.0E-6D);
        assertEquals(verticalEnd.x, points[points.length - 1].x, 1.0E-6D);
        assertEquals(verticalEnd.y, points[points.length - 1].y, 1.0E-6D);
        assertEquals(verticalEnd.z, points[points.length - 1].z, 1.0E-6D);
        assertAllFinite(points);
    }

    @Test
    void routeCurveNeverExceedsRequestedSampleBudget() {
        List<Vec3> route = new ArrayList<>();
        for (int index = 0; index < 512; index++) {
            route.add(new Vec3(index, 64.0D, index % 2 == 0 ? 0.0D : 4.0D));
        }

        assertTrue(FiberOpticCableRenderMath.buildRouteCurvePoints(route, new Vec3(512.0D, 64.0D, 0.0D), 96, true).length <= 96);
        assertTrue(FiberOpticCableRenderMath.buildRouteCurvePoints(route, new Vec3(512.0D, 64.0D, 0.0D), 64, true).length <= 64);
        assertTrue(FiberOpticCableRenderMath.buildRouteCurvePoints(route, new Vec3(512.0D, 64.0D, 0.0D), 32, true).length <= 32);
    }

    @Test
    void clampedCatmullRomCornersDoNotOvershootRouteBounds() {
        List<Vec3> route = List.of(
                new Vec3(0.0D, 64.0D, 0.0D),
                new Vec3(10.0D, 64.0D, 0.0D),
                new Vec3(10.0D, 64.0D, 10.0D),
                new Vec3(20.0D, 64.0D, 10.0D)
        );

        Vec3[] points = FiberOpticCableRenderMath.buildRouteCurvePoints(route, new Vec3(20.0D, 64.0D, 20.0D), 96, true);

        assertTrue(points.length > route.size());
        for (Vec3 point : points) {
            assertTrue(FiberOpticCableRenderMath.isFinite(point));
            assertTrue(point.x >= -0.36D && point.x <= 20.36D, "Curve x should stay close to the route corridor.");
            assertTrue(point.z >= -0.36D && point.z <= 20.36D, "Curve z should stay close to the route corridor.");
            assertTrue(point.y >= 63.0D && point.y <= 64.36D, "Curve y should not loop or spike around flat corners.");
        }
    }

    @Test
    void malformedCableEndpointsReturnNoRenderableSegments() {
        assertEquals(0, FiberOpticCableRenderMath.buildCableCurvePoints(new Vec3(Double.NaN, 64.0D, 0.0D), Vec3.ZERO, 8).length);
        assertEquals(0, FiberOpticCableRenderMath.buildRouteCurvePoints(List.of(Vec3.ZERO), new Vec3(Double.NEGATIVE_INFINITY, 64.0D, 0.0D), 8, true).length);
        assertFalse(FiberOpticCableRenderMath.isFinite(new Vec3(Double.NaN, 0.0D, 0.0D)));
    }

    private static void assertAllFinite(Vec3[] points) {
        for (Vec3 point : points) {
            assertTrue(FiberOpticCableRenderMath.isFinite(point));
        }
    }
}
