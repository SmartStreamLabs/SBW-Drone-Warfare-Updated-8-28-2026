package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class FiberCableRouteState {
    static final UUID EMPTY_SESSION = new UUID(0L, 0L);

    static final String TAG_ROUTE_ACTIVE = "sbwdroneconfigFiberRouteActive";
    static final String TAG_ROUTE_SESSION_ID = "sbwdroneconfigFiberRouteSessionId";
    static final String TAG_ROUTE_REVISION = "sbwdroneconfigFiberRouteRevision";
    static final String TAG_ROUTE_POINTS = "sbwdroneconfigFiberRoutePoints";

    private static final String TAG_POINT_X = "x";
    private static final String TAG_POINT_Y = "y";
    private static final String TAG_POINT_Z = "z";
    private static final double MIN_POINT_DISTANCE_SQR = 0.05D * 0.05D;
    private static final double COLLINEAR_MAX_DEVIATION = 0.20D;
    private static final double COLLINEAR_MAX_ANGLE_DEGREES = 5.0D;
    private static final int NEWEST_POINT_GUARD = 64;

    private FiberCableRouteState() {
    }

    static RouteSyncUpdate initialize(Entity drone, Vec3 renderAnchor) {
        if (drone == null) {
            return RouteSyncUpdate.none();
        }
        RouteSyncUpdate update = initialize(drone.getPersistentData(), UUID.randomUUID(), renderAnchor, drone.getId());
        return update;
    }

    static RouteSyncUpdate initialize(CompoundTag data, UUID sessionId, Vec3 renderAnchor) {
        return initialize(data, sessionId, renderAnchor, -1);
    }

    private static RouteSyncUpdate initialize(CompoundTag data, UUID sessionId, Vec3 renderAnchor, int entityId) {
        RoutePoint anchor = RoutePoint.fromVec3OrNull(renderAnchor);
        if (data == null || anchor == null || !AddonConfig.fiberRouteHistoryEnabled()) {
            clearTags(data);
            return RouteSyncUpdate.none();
        }

        UUID nextSessionId = sessionId == null || EMPTY_SESSION.equals(sessionId) ? UUID.randomUUID() : sessionId;
        List<RoutePoint> points = new ArrayList<>();
        points.add(anchor);

        data.putBoolean(TAG_ROUTE_ACTIVE, true);
        data.putUUID(TAG_ROUTE_SESSION_ID, nextSessionId);
        data.putInt(TAG_ROUTE_REVISION, 1);
        writePoints(data, points);
        logRouteEvent("SESSION_START", entityId, nextSessionId, 1, points.size(), "anchor");

        return RouteSyncUpdate.snapshot(nextSessionId, 1, points, "session_start");
    }

    static RouteSyncUpdate ensureInitialized(Entity drone, Vec3 renderAnchor) {
        if (drone == null) {
            return RouteSyncUpdate.none();
        }
        return ensureInitialized(drone.getPersistentData(), renderAnchor, drone.getId());
    }

    static RouteSyncUpdate ensureInitialized(CompoundTag data, Vec3 renderAnchor) {
        return ensureInitialized(data, renderAnchor, -1);
    }

    private static RouteSyncUpdate ensureInitialized(CompoundTag data, Vec3 renderAnchor, int entityId) {
        if (data == null || !AddonConfig.fiberRouteHistoryEnabled()) {
            clearTags(data);
            return RouteSyncUpdate.none();
        }
        if (!data.getBoolean(TAG_ROUTE_ACTIVE) || !data.hasUUID(TAG_ROUTE_SESSION_ID)) {
            return initialize(data, UUID.randomUUID(), renderAnchor, entityId);
        }

        List<RoutePoint> points = readPoints(data);
        if (points.isEmpty()) {
            RoutePoint anchor = RoutePoint.fromVec3OrNull(renderAnchor);
            if (anchor == null) {
                return RouteSyncUpdate.none();
            }
            points.add(anchor);
            int nextRevision = data.getInt(TAG_ROUTE_REVISION) + 1;
            data.putInt(TAG_ROUTE_REVISION, nextRevision);
            writePoints(data, points);
            logRouteEvent("SNAPSHOT_SENT", entityId, sessionId(data), nextRevision, points.size(), "repair_empty_route");
            return RouteSyncUpdate.snapshot(sessionId(data), nextRevision, points, "repair_empty_route");
        }

        return RouteSyncUpdate.none();
    }

    static RouteSyncUpdate appendIfNeeded(Entity drone, Vec3 spoolPoint) {
        if (drone == null) {
            return RouteSyncUpdate.none();
        }
        return appendIfNeeded(drone.getPersistentData(), spoolPoint, drone.getId());
    }

    static RouteSyncUpdate appendIfNeeded(CompoundTag data, Vec3 spoolPoint) {
        return appendIfNeeded(data, spoolPoint, -1);
    }

    private static RouteSyncUpdate appendIfNeeded(CompoundTag data, Vec3 spoolPoint, int entityId) {
        RoutePoint newPoint = RoutePoint.fromVec3OrNull(spoolPoint);
        if (data == null || newPoint == null || !AddonConfig.fiberRouteHistoryEnabled()) {
            return RouteSyncUpdate.none();
        }
        if (!data.getBoolean(TAG_ROUTE_ACTIVE) || !data.hasUUID(TAG_ROUTE_SESSION_ID)) {
            return RouteSyncUpdate.none();
        }

        List<RoutePoint> points = readPoints(data);
        if (points.isEmpty()) {
            return RouteSyncUpdate.none();
        }

        RoutePoint lastPoint = points.get(points.size() - 1);
        if (lastPoint.distanceToSqr(newPoint) < MIN_POINT_DISTANCE_SQR) {
            return RouteSyncUpdate.none();
        }

        AppendDecision decision = appendDecision(points, newPoint);
        if (!decision.shouldAppend()) {
            return RouteSyncUpdate.none();
        }

        int previousRevision = revision(data);
        boolean modifiedExistingPoint = replaceLastIfLocallyCollinear(points, newPoint);
        if (!modifiedExistingPoint) {
            points.add(newPoint);
        }

        boolean compacted = enforceMaximumPoints(points);
        int nextRevision = previousRevision + 1;
        data.putInt(TAG_ROUTE_REVISION, nextRevision);
        writePoints(data, points);

        if (modifiedExistingPoint) {
            logRouteEvent("ROUTE_SIMPLIFIED", entityId, sessionId(data), nextRevision, points.size(), decision.reason());
        }
        if (compacted) {
            logRouteEvent("ROUTE_COMPACTED", entityId, sessionId(data), nextRevision, points.size(), decision.reason());
        }
        logRouteEvent("ROUTE_APPEND", entityId, sessionId(data), nextRevision, points.size(), decision.reason());

        if (modifiedExistingPoint || compacted) {
            return RouteSyncUpdate.snapshot(sessionId(data), nextRevision, points, decision.reason());
        }

        return RouteSyncUpdate.append(sessionId(data), previousRevision, nextRevision, List.of(newPoint), decision.reason());
    }

    static SnapshotData snapshot(Entity drone) {
        if (drone == null) {
            return null;
        }
        return snapshot(drone.getPersistentData());
    }

    static SnapshotData snapshot(CompoundTag data) {
        if (data == null || !AddonConfig.fiberRouteHistoryEnabled()) {
            return null;
        }
        if (!data.getBoolean(TAG_ROUTE_ACTIVE) || !data.hasUUID(TAG_ROUTE_SESSION_ID)) {
            return null;
        }

        return new SnapshotData(sessionId(data), revision(data), readPoints(data), data.getBoolean(TAG_ROUTE_ACTIVE));
    }

    static ClearData clear(Entity drone) {
        if (drone == null) {
            return new ClearData(EMPTY_SESSION, 0, false);
        }
        ClearData clearData = clear(drone.getPersistentData(), drone.getId());
        return clearData;
    }

    static ClearData clear(CompoundTag data) {
        return clear(data, -1);
    }

    private static ClearData clear(CompoundTag data, int entityId) {
        if (data == null) {
            return new ClearData(EMPTY_SESSION, 0, false);
        }

        boolean wasActive = isActive(data);
        UUID sessionId = sessionId(data);
        int revision = revision(data) + 1;
        clearTags(data);
        if (wasActive) {
            logRouteEvent("ROUTE_CLEAR", entityId, sessionId, revision, 0, "clear_session");
        }
        return new ClearData(sessionId, revision, wasActive);
    }

    static boolean isActive(Entity drone) {
        return drone != null && isActive(drone.getPersistentData());
    }

    static boolean isActive(CompoundTag data) {
        return data != null && data.getBoolean(TAG_ROUTE_ACTIVE) && data.hasUUID(TAG_ROUTE_SESSION_ID);
    }

    static UUID sessionId(Entity drone) {
        return drone == null ? EMPTY_SESSION : sessionId(drone.getPersistentData());
    }

    static UUID sessionId(CompoundTag data) {
        return data != null && data.hasUUID(TAG_ROUTE_SESSION_ID) ? data.getUUID(TAG_ROUTE_SESSION_ID) : EMPTY_SESSION;
    }

    static int revision(Entity drone) {
        return drone == null ? 0 : revision(drone.getPersistentData());
    }

    static int revision(CompoundTag data) {
        return data == null ? 0 : data.getInt(TAG_ROUTE_REVISION);
    }

    static List<RoutePoint> readPoints(Entity drone) {
        if (drone == null) {
            return List.of();
        }
        return readPoints(drone.getPersistentData());
    }

    static List<RoutePoint> readPoints(CompoundTag data) {
        int maximumPoints = Math.max(1, AddonConfig.fiberRouteMaximumPoints());
        List<RoutePoint> points = new ArrayList<>();
        if (data == null || !data.contains(TAG_ROUTE_POINTS, Tag.TAG_LIST)) {
            return points;
        }

        ListTag list = data.getList(TAG_ROUTE_POINTS, Tag.TAG_COMPOUND);
        int count = Math.min(maximumPoints, list.size());
        for (int index = 0; index < count; index++) {
            RoutePoint point = RoutePoint.fromTagOrNull(list.getCompound(index));
            if (point != null) {
                points.add(point);
            }
        }
        return points;
    }

    static void writePoints(CompoundTag data, List<RoutePoint> points) {
        int maximumPoints = Math.max(1, AddonConfig.fiberRouteMaximumPoints());
        ListTag list = new ListTag();
        if (points != null) {
            int count = Math.min(maximumPoints, points.size());
            for (int index = 0; index < count; index++) {
                RoutePoint point = points.get(index);
                if (point != null && point.isFinite()) {
                    list.add(point.save());
                }
            }
        }
        data.put(TAG_ROUTE_POINTS, list);
    }

    private static AppendDecision appendDecision(List<RoutePoint> points, RoutePoint newPoint) {
        RoutePoint lastPoint = points.get(points.size() - 1);
        boolean spacingReached = lastPoint.distanceTo(newPoint) >= AddonConfig.fiberRoutePointSpacing();
        boolean turnReached = shouldCommitTurn(points, newPoint);
        if (spacingReached) {
            return new AppendDecision(true, turnReached ? "spacing_and_turn" : "spacing");
        }
        if (turnReached) {
            return new AppendDecision(true, "turn");
        }
        return new AppendDecision(false, "none");
    }

    private static boolean shouldCommitTurn(List<RoutePoint> points, RoutePoint newPoint) {
        if (points.size() < 2) {
            return false;
        }

        RoutePoint previousPrevious = points.get(points.size() - 2);
        RoutePoint previous = points.get(points.size() - 1);
        if (previous.distanceTo(newPoint) < AddonConfig.fiberRouteMinimumTurnDistance()) {
            return false;
        }

        double angle = angleBetween(previous.subtract(previousPrevious), newPoint.subtract(previous));
        return angle >= AddonConfig.fiberRouteTurnAngleThreshold();
    }

    private static boolean replaceLastIfLocallyCollinear(List<RoutePoint> points, RoutePoint newPoint) {
        if (points.size() < 2 || points.get(0).equals(points.get(points.size() - 1))) {
            return false;
        }

        RoutePoint previousPrevious = points.get(points.size() - 2);
        RoutePoint previous = points.get(points.size() - 1);
        if (!isNearlyCollinear(previousPrevious, previous, newPoint)) {
            return false;
        }

        points.set(points.size() - 1, newPoint);
        return true;
    }

    private static boolean enforceMaximumPoints(List<RoutePoint> points) {
        int maximumPoints = Math.max(2, AddonConfig.fiberRouteMaximumPoints());
        if (points.size() <= maximumPoints) {
            return false;
        }

        boolean compacted = false;
        int newestGuard = Math.min(NEWEST_POINT_GUARD, Math.max(2, maximumPoints / 4));
        while (points.size() > maximumPoints && removeOlderCollinearPoint(points, newestGuard)) {
            compacted = true;
        }

        while (points.size() > maximumPoints && removeLowestTurnOlderPoint(points, newestGuard)) {
            compacted = true;
        }

        while (points.size() > maximumPoints && points.size() > 2) {
            points.remove(1);
            compacted = true;
        }

        return compacted;
    }

    private static boolean removeOlderCollinearPoint(List<RoutePoint> points, int newestGuard) {
        int exclusiveEnd = Math.max(2, points.size() - newestGuard);
        for (int index = 1; index < exclusiveEnd - 1; index++) {
            if (isNearlyCollinear(points.get(index - 1), points.get(index), points.get(index + 1))) {
                points.remove(index);
                return true;
            }
        }
        return false;
    }

    private static boolean removeLowestTurnOlderPoint(List<RoutePoint> points, int newestGuard) {
        int exclusiveEnd = Math.max(2, points.size() - newestGuard);
        int bestIndex = -1;
        double bestAngle = Double.MAX_VALUE;
        for (int index = 1; index < exclusiveEnd - 1; index++) {
            double angle = angleBetween(
                    points.get(index).subtract(points.get(index - 1)),
                    points.get(index + 1).subtract(points.get(index))
            );
            if (angle < bestAngle) {
                bestAngle = angle;
                bestIndex = index;
            }
        }

        if (bestIndex <= 0) {
            return false;
        }

        points.remove(bestIndex);
        return true;
    }

    private static boolean isNearlyCollinear(RoutePoint first, RoutePoint middle, RoutePoint last) {
        Vec3 whole = last.subtract(first);
        Vec3 firstLeg = middle.subtract(first);
        Vec3 secondLeg = last.subtract(middle);
        if (whole.lengthSqr() <= 1.0E-8D || firstLeg.lengthSqr() <= 1.0E-8D || secondLeg.lengthSqr() <= 1.0E-8D) {
            return false;
        }

        double deviation = firstLeg.cross(whole).length() / whole.length();
        double angle = angleBetween(firstLeg, secondLeg);
        return Double.isFinite(deviation) && deviation <= COLLINEAR_MAX_DEVIATION && angle <= COLLINEAR_MAX_ANGLE_DEGREES;
    }

    private static double angleBetween(Vec3 first, Vec3 second) {
        double lengthProduct = first.length() * second.length();
        if (!Double.isFinite(lengthProduct) || lengthProduct <= 1.0E-8D) {
            return 0.0D;
        }

        double dot = first.dot(second) / lengthProduct;
        double clamped = Math.max(-1.0D, Math.min(1.0D, dot));
        return Math.toDegrees(Math.acos(clamped));
    }

    private static void clearTags(CompoundTag data) {
        if (data == null) {
            return;
        }

        data.remove(TAG_ROUTE_ACTIVE);
        data.remove(TAG_ROUTE_SESSION_ID);
        data.remove(TAG_ROUTE_REVISION);
        data.remove(TAG_ROUTE_POINTS);
    }

    private static void logRouteEvent(String event, int entityId, UUID sessionId, int revision, int pointCount, String detail) {
        if (!AddonConfig.fiberRouteDebugLogging()) {
            return;
        }

        SbwDroneRangeConfig.LOGGER.info(
                "Fiber route {} droneEntityId={} session={} revision={} points={} detail={}",
                event,
                entityId,
                sessionId,
                revision,
                pointCount,
                detail
        );
    }

    record RoutePoint(double x, double y, double z) {
        static RoutePoint from(Vec3 point) {
            return new RoutePoint(point.x, point.y, point.z);
        }

        static RoutePoint fromVec3OrNull(Vec3 point) {
            if (point == null || !Double.isFinite(point.x) || !Double.isFinite(point.y) || !Double.isFinite(point.z)) {
                return null;
            }
            return from(point);
        }

        static RoutePoint fromTagOrNull(CompoundTag tag) {
            if (tag == null) {
                return null;
            }
            RoutePoint point = new RoutePoint(tag.getDouble(TAG_POINT_X), tag.getDouble(TAG_POINT_Y), tag.getDouble(TAG_POINT_Z));
            return point.isFinite() ? point : null;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble(TAG_POINT_X, x);
            tag.putDouble(TAG_POINT_Y, y);
            tag.putDouble(TAG_POINT_Z, z);
            return tag;
        }

        Vec3 toVec3() {
            return new Vec3(x, y, z);
        }

        boolean isFinite() {
            return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
        }

        double distanceTo(RoutePoint other) {
            return Math.sqrt(distanceToSqr(other));
        }

        double distanceToSqr(RoutePoint other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return (dx * dx) + (dy * dy) + (dz * dz);
        }

        Vec3 subtract(RoutePoint other) {
            return new Vec3(x - other.x, y - other.y, z - other.z);
        }
    }

    record SnapshotData(UUID sessionId, int revision, List<RoutePoint> points, boolean active) {
        SnapshotData {
            points = List.copyOf(points);
        }
    }

    record ClearData(UUID sessionId, int revision, boolean wasActive) {
    }

    record RouteSyncUpdate(Kind kind, UUID sessionId, int previousRevision, int revision, List<RoutePoint> points, String reason) {
        static RouteSyncUpdate none() {
            return new RouteSyncUpdate(Kind.NONE, EMPTY_SESSION, 0, 0, List.of(), "none");
        }

        static RouteSyncUpdate snapshot(UUID sessionId, int revision, List<RoutePoint> points, String reason) {
            return new RouteSyncUpdate(Kind.SNAPSHOT, sessionId, revision - 1, revision, List.copyOf(points), reason);
        }

        static RouteSyncUpdate append(UUID sessionId, int previousRevision, int revision, List<RoutePoint> points, String reason) {
            return new RouteSyncUpdate(Kind.APPEND, sessionId, previousRevision, revision, List.copyOf(points), reason);
        }
    }

    private record AppendDecision(boolean shouldAppend, String reason) {
    }

    enum Kind {
        NONE,
        SNAPSHOT,
        APPEND
    }
}
