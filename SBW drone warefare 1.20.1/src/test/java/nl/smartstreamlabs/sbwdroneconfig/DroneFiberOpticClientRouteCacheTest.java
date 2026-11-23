package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneFiberOpticClientRouteCacheTest {
    private static final UUID DRONE_ID = UUID.fromString("52c55093-a2b3-4efa-a26c-923919364681");
    private static final UUID OTHER_DRONE_ID = UUID.fromString("36c0d4c1-3216-444c-8f98-271f7506aab6");
    private static final UUID SESSION_ID = UUID.fromString("60d12a71-753e-44fd-b5f9-43ebfdcbba4d");
    private static final UUID OTHER_SESSION_ID = UUID.fromString("68674a93-53c7-42aa-9ce0-910c047fcf19");

    @Test
    void snapshotStartsClientRouteAndMatchingAppendAdvancesRevision() {
        Map<UUID, DroneFiberOpticClient.FiberRouteClientState> cache = new HashMap<>();

        assertTrue(DroneFiberOpticClient.applyRouteSnapshot(cache, DRONE_ID, 17, SESSION_ID, 1, points(point(0.0D, 64.0D, 0.0D)), 10L));
        assertTrue(DroneFiberOpticClient.applyRouteAppend(cache, DRONE_ID, 17, SESSION_ID, 1, 2, points(point(3.0D, 64.0D, 0.0D)), 11L));

        DroneFiberOpticClient.FiberRouteClientState state = cache.get(DRONE_ID);
        assertEquals(2, state.revision());
        assertEquals(2, state.points().size());
        assertEquals(3.0D, state.points().get(1).x, 1.0E-6D);
        assertTrue(state.active());
    }

    @Test
    void staleDuplicateAndOutOfOrderAppendsAreRejected() {
        Map<UUID, DroneFiberOpticClient.FiberRouteClientState> cache = new HashMap<>();
        DroneFiberOpticClient.applyRouteSnapshot(cache, DRONE_ID, 17, SESSION_ID, 3, points(point(0.0D, 64.0D, 0.0D)), 10L);

        assertFalse(DroneFiberOpticClient.applyRouteAppend(cache, DRONE_ID, 17, SESSION_ID, 2, 3, points(point(3.0D, 64.0D, 0.0D)), 11L));
        assertFalse(DroneFiberOpticClient.applyRouteAppend(cache, DRONE_ID, 17, SESSION_ID, 1, 2, points(point(6.0D, 64.0D, 0.0D)), 12L));
        assertFalse(DroneFiberOpticClient.applyRouteAppend(cache, DRONE_ID, 17, SESSION_ID, 4, 5, points(point(9.0D, 64.0D, 0.0D)), 13L));

        DroneFiberOpticClient.FiberRouteClientState state = cache.get(DRONE_ID);
        assertEquals(3, state.revision());
        assertEquals(1, state.points().size());
    }

    @Test
    void wrongSessionOrReusedEntityIdCannotAttachToCurrentRoute() {
        Map<UUID, DroneFiberOpticClient.FiberRouteClientState> cache = new HashMap<>();
        DroneFiberOpticClient.applyRouteSnapshot(cache, DRONE_ID, 17, SESSION_ID, 1, points(point(0.0D, 64.0D, 0.0D)), 10L);

        assertFalse(DroneFiberOpticClient.applyRouteAppend(cache, DRONE_ID, 17, OTHER_SESSION_ID, 1, 2, points(point(3.0D, 64.0D, 0.0D)), 11L));
        assertFalse(DroneFiberOpticClient.applyRouteAppend(cache, DRONE_ID, 18, SESSION_ID, 1, 2, points(point(3.0D, 64.0D, 0.0D)), 12L));
        assertFalse(DroneFiberOpticClient.applyRouteAppend(cache, OTHER_DRONE_ID, 17, SESSION_ID, 1, 2, points(point(3.0D, 64.0D, 0.0D)), 13L));

        assertNull(cache.get(OTHER_DRONE_ID));
        assertEquals(1, cache.get(DRONE_ID).revision());
    }

    @Test
    void clearOnlyRemovesTheCurrentSessionAtCurrentOrNewerRevision() {
        Map<UUID, DroneFiberOpticClient.FiberRouteClientState> cache = new HashMap<>();
        DroneFiberOpticClient.applyRouteSnapshot(cache, DRONE_ID, 17, SESSION_ID, 4, points(point(0.0D, 64.0D, 0.0D)), 10L);

        assertFalse(DroneFiberOpticClient.applyRouteClear(cache, DRONE_ID, OTHER_SESSION_ID, 99, "stale_session"));
        assertFalse(DroneFiberOpticClient.applyRouteClear(cache, DRONE_ID, SESSION_ID, 3, "stale_revision"));
        assertTrue(cache.containsKey(DRONE_ID));

        assertTrue(DroneFiberOpticClient.applyRouteClear(cache, DRONE_ID, SESSION_ID, 4, "current_clear"));
        assertFalse(cache.containsKey(DRONE_ID));
    }

    @Test
    void cacheClearCoversWorldUnloadAndDimensionChange() {
        Map<UUID, DroneFiberOpticClient.FiberRouteClientState> cache = new HashMap<>();
        DroneFiberOpticClient.applyRouteSnapshot(cache, DRONE_ID, 17, SESSION_ID, 1, points(point(0.0D, 64.0D, 0.0D)), 10L);
        DroneFiberOpticClient.applyRouteSnapshot(cache, OTHER_DRONE_ID, 18, OTHER_SESSION_ID, 1, points(point(10.0D, 64.0D, 10.0D)), 10L);

        DroneFiberOpticClient.clearRouteCacheForTesting(cache, "dimension_change");

        assertTrue(cache.isEmpty());
    }

    @Test
    void malformedSnapshotAndAppendPointsAreSanitized() {
        Map<UUID, DroneFiberOpticClient.FiberRouteClientState> cache = new HashMap<>();
        List<FiberCableRouteState.RoutePoint> snapshotPoints = points(
                point(0.0D, 64.0D, 0.0D),
                point(Double.NaN, 64.0D, 0.0D)
        );

        assertTrue(DroneFiberOpticClient.applyRouteSnapshot(cache, DRONE_ID, 17, SESSION_ID, 1, snapshotPoints, 10L));
        assertEquals(1, cache.get(DRONE_ID).points().size());

        assertFalse(DroneFiberOpticClient.applyRouteAppend(cache, DRONE_ID, 17, SESSION_ID, 1, 2, points(point(Double.POSITIVE_INFINITY, 64.0D, 0.0D)), 11L));
        assertEquals(1, cache.get(DRONE_ID).revision());
    }

    private static FiberCableRouteState.RoutePoint point(double x, double y, double z) {
        return new FiberCableRouteState.RoutePoint(x, y, z);
    }

    private static List<FiberCableRouteState.RoutePoint> points(FiberCableRouteState.RoutePoint... points) {
        return List.of(points);
    }
}
