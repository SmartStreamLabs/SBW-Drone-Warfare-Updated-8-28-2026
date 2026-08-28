package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberCableRouteStateTest {
    private static final Vec3 ANCHOR = new Vec3(0.0D, 64.12D, 0.0D);

    @Test
    void straightMovementKeepsSparseRouteAndDoesNotRecordEveryTick() {
        CompoundTag data = initializedRoute();
        int ticks = 0;

        for (double x = 0.10D; x <= 30.0D; x += 0.25D) {
            FiberCableRouteState.appendIfNeeded(data, new Vec3(x, ANCHOR.y, 0.0D));
            ticks++;
        }

        List<FiberCableRouteState.RoutePoint> points = FiberCableRouteState.readPoints(data);
        FiberCableRouteState.RoutePoint lastPoint = points.get(points.size() - 1);
        assertTrue(points.size() <= 3, "Straight flight should collapse to anchor plus the latest endpoint.");
        assertEquals(ANCHOR.x, points.get(0).x(), 1.0E-6D);
        assertTrue(30.0D - lastPoint.x() <= AddonConfig.fiberRoutePointSpacing(), "The latest sparse route point may lag by at most one spacing step.");
        assertTrue(FiberCableRouteState.revision(data) < ticks / 3, "Route revisions must stay sparse and not tick-rate driven.");
    }

    @Test
    void hoveringAndJitterDoNotAppendRoutePoints() {
        CompoundTag data = initializedRoute();
        FiberCableRouteState.RouteSyncUpdate firstAppend = FiberCableRouteState.appendIfNeeded(data, new Vec3(2.6D, ANCHOR.y, 0.0D));
        int revisionAfterAppend = FiberCableRouteState.revision(data);
        int pointsAfterAppend = FiberCableRouteState.readPoints(data).size();

        assertNotEquals(FiberCableRouteState.Kind.NONE, firstAppend.kind());
        for (int tick = 0; tick < 200; tick++) {
            double jitter = (tick % 2 == 0) ? 0.01D : -0.01D;
            FiberCableRouteState.RouteSyncUpdate update = FiberCableRouteState.appendIfNeeded(data, new Vec3(2.61D + jitter, ANCHOR.y, 0.01D));
            assertEquals(FiberCableRouteState.Kind.NONE, update.kind());
        }

        assertEquals(revisionAfterAppend, FiberCableRouteState.revision(data));
        assertEquals(pointsAfterAppend, FiberCableRouteState.readPoints(data).size());
    }

    @Test
    void spacingThresholdControlsNormalAppends() {
        CompoundTag data = initializedRoute();

        FiberCableRouteState.RouteSyncUpdate shortMove = FiberCableRouteState.appendIfNeeded(data, new Vec3(2.49D, ANCHOR.y, 0.0D));
        FiberCableRouteState.RouteSyncUpdate spacedMove = FiberCableRouteState.appendIfNeeded(data, new Vec3(2.51D, ANCHOR.y, 0.0D));

        assertEquals(FiberCableRouteState.Kind.NONE, shortMove.kind());
        assertNotEquals(FiberCableRouteState.Kind.NONE, spacedMove.kind());
        assertEquals(2, FiberCableRouteState.readPoints(data).size());
    }

    @Test
    void turnThresholdCommitsCornerBeforeSpacingThreshold() {
        CompoundTag data = initializedRoute();
        FiberCableRouteState.appendIfNeeded(data, new Vec3(2.6D, ANCHOR.y, 0.0D));

        FiberCableRouteState.RouteSyncUpdate turn = FiberCableRouteState.appendIfNeeded(data, new Vec3(3.6D, ANCHOR.y, 1.2D));

        assertEquals(FiberCableRouteState.Kind.APPEND, turn.kind());
        assertEquals("turn", turn.reason());
        assertEquals(3, FiberCableRouteState.readPoints(data).size());
    }

    @Test
    void locallyCollinearMovementReplacesTheMiddlePoint() {
        CompoundTag data = initializedRoute();
        FiberCableRouteState.appendIfNeeded(data, new Vec3(2.6D, ANCHOR.y, 0.0D));

        FiberCableRouteState.RouteSyncUpdate simplified = FiberCableRouteState.appendIfNeeded(data, new Vec3(5.2D, ANCHOR.y, 0.02D));

        List<FiberCableRouteState.RoutePoint> points = FiberCableRouteState.readPoints(data);
        assertEquals(FiberCableRouteState.Kind.SNAPSHOT, simplified.kind());
        assertEquals(2, points.size(), "A straight local run should keep the anchor and newest point only.");
        assertEquals(5.2D, points.get(1).x(), 1.0E-6D);
        assertEquals(0.02D, points.get(1).z(), 1.0E-6D);
    }

    @Test
    void sharpTurnsArePreserved() {
        CompoundTag data = initializedRoute();

        FiberCableRouteState.appendIfNeeded(data, new Vec3(3.0D, ANCHOR.y, 0.0D));
        FiberCableRouteState.appendIfNeeded(data, new Vec3(3.0D, ANCHOR.y, 3.0D));
        FiberCableRouteState.appendIfNeeded(data, new Vec3(6.0D, ANCHOR.y, 3.0D));

        List<FiberCableRouteState.RoutePoint> points = FiberCableRouteState.readPoints(data);
        assertEquals(4, points.size());
        assertEquals(3.0D, points.get(1).x(), 1.0E-6D);
        assertEquals(3.0D, points.get(2).z(), 1.0E-6D);
        assertEquals(6.0D, points.get(3).x(), 1.0E-6D);
    }

    @Test
    void maximumPointBudgetPreservesAnchorAndNewestPoint() {
        CompoundTag data = initializedRoute();
        Vec3 newestPoint = ANCHOR;

        for (int index = 1; index <= 700; index++) {
            newestPoint = new Vec3(index * 2.6D, ANCHOR.y, index % 2 == 0 ? 0.0D : 2.6D);
            FiberCableRouteState.appendIfNeeded(data, newestPoint);
        }

        List<FiberCableRouteState.RoutePoint> points = FiberCableRouteState.readPoints(data);
        FiberCableRouteState.RoutePoint first = points.get(0);
        FiberCableRouteState.RoutePoint last = points.get(points.size() - 1);

        assertTrue(points.size() <= AddonConfig.fiberRouteMaximumPoints());
        assertEquals(ANCHOR.x, first.x(), 1.0E-6D);
        assertEquals(ANCHOR.y, first.y(), 1.0E-6D);
        assertEquals(ANCHOR.z, first.z(), 1.0E-6D);
        assertEquals(newestPoint.x, last.x(), 1.0E-6D);
        assertEquals(newestPoint.z, last.z(), 1.0E-6D);
    }

    @Test
    void routeNbtRoundTripsAndMalformedPointsAreIgnored() {
        CompoundTag data = initializedRoute();
        UUID sessionId = FiberCableRouteState.sessionId(data);
        FiberCableRouteState.appendIfNeeded(data, new Vec3(3.0D, ANCHOR.y, 0.0D));
        FiberCableRouteState.appendIfNeeded(data, new Vec3(3.0D, ANCHOR.y, 3.0D));

        CompoundTag copy = data.copy();
        FiberCableRouteState.SnapshotData snapshot = FiberCableRouteState.snapshot(copy);

        assertTrue(snapshot.active());
        assertEquals(sessionId, snapshot.sessionId());
        assertEquals(FiberCableRouteState.revision(data), snapshot.revision());
        assertEquals(FiberCableRouteState.readPoints(data), snapshot.points());

        CompoundTag malformed = new CompoundTag();
        ListTag oversizedList = new ListTag();
        for (int index = 0; index < 600; index++) {
            CompoundTag point = new CompoundTag();
            point.putDouble("x", index % 50 == 0 ? Double.NaN : index);
            point.putDouble("y", 64.0D);
            point.putDouble("z", index);
            oversizedList.add(point);
        }
        malformed.put(FiberCableRouteState.TAG_ROUTE_POINTS, oversizedList);

        List<FiberCableRouteState.RoutePoint> sanitized = FiberCableRouteState.readPoints(malformed);
        assertTrue(sanitized.size() <= AddonConfig.fiberRouteMaximumPoints());
        assertTrue(sanitized.stream().allMatch(FiberCableRouteState.RoutePoint::isFinite));
    }

    @Test
    void clearRemovesRouteTagsAndCarriesSessionRevisionForClients() {
        CompoundTag data = initializedRoute();
        UUID sessionId = FiberCableRouteState.sessionId(data);
        int previousRevision = FiberCableRouteState.revision(data);

        FiberCableRouteState.ClearData clearData = FiberCableRouteState.clear(data);

        assertTrue(clearData.wasActive());
        assertEquals(sessionId, clearData.sessionId());
        assertEquals(previousRevision + 1, clearData.revision());
        assertFalse(FiberCableRouteState.isActive(data));
        assertFalse(data.contains(FiberCableRouteState.TAG_ROUTE_ACTIVE));
        assertFalse(data.contains(FiberCableRouteState.TAG_ROUTE_SESSION_ID));
        assertFalse(data.contains(FiberCableRouteState.TAG_ROUTE_REVISION));
        assertFalse(data.contains(FiberCableRouteState.TAG_ROUTE_POINTS));
    }

    private static CompoundTag initializedRoute() {
        CompoundTag data = new CompoundTag();
        FiberCableRouteState.RouteSyncUpdate update = FiberCableRouteState.initialize(
                data,
                UUID.fromString("89e6376d-e6a7-4d27-b944-2948086e2f5b"),
                ANCHOR
        );

        assertEquals(FiberCableRouteState.Kind.SNAPSHOT, update.kind());
        return data;
    }
}
