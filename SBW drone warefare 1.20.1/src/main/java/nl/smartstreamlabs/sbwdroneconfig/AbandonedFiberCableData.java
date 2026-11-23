package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AbandonedFiberCableData extends SavedData {
    private static final String DATA_NAME = SbwDroneRangeConfig.MOD_ID + "_abandoned_fiber_cables";
    private static final String TAG_CABLES = "cables";
    private static final String TAG_CABLE_ID = "cableId";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_OWNER_ID = "ownerId";
    private static final String TAG_CREATED_GAME_TIME = "createdGameTime";
    private static final String TAG_POINTS = "points";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final int HARD_POINT_LIMIT = 2049;

    private final Map<UUID, CableRecord> cables = new LinkedHashMap<>();

    public static AbandonedFiberCableData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                AbandonedFiberCableData::load,
                AbandonedFiberCableData::new,
                DATA_NAME
        );
    }

    private static AbandonedFiberCableData load(CompoundTag tag) {
        AbandonedFiberCableData data = new AbandonedFiberCableData();
        if (tag == null || !tag.contains(TAG_CABLES, Tag.TAG_LIST)) {
            return data;
        }

        ListTag list = tag.getList(TAG_CABLES, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CableRecord record = readRecord(list.getCompound(index));
            if (record != null) {
                data.cables.put(record.cableId(), record);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (CableRecord record : cables.values()) {
            list.add(writeRecord(record));
        }
        tag.put(TAG_CABLES, list);
        return tag;
    }

    CableRecord addCable(String dimension, UUID ownerId, List<Vec3> points, long createdGameTime) {
        List<Vec3> sanitized = sanitizePoints(points);
        if (dimension == null || dimension.isBlank() || sanitized.size() < 2) {
            return null;
        }

        CableRecord record = new CableRecord(UUID.randomUUID(), dimension, ownerId, createdGameTime, sanitized);
        cables.put(record.cableId(), record);
        setDirty();
        return record;
    }

    Collection<CableRecord> records() {
        return List.copyOf(cables.values());
    }

    CableRecord cable(UUID cableId) {
        return cableId == null ? null : cables.get(cableId);
    }

    CableRecord removeCable(UUID cableId) {
        if (cableId == null) {
            return null;
        }

        CableRecord removed = cables.remove(cableId);
        if (removed != null) {
            setDirty();
        }
        return removed;
    }

    List<CableRecord> recordsNear(Vec3 position, double maxDistance) {
        if (!FiberCableHitTest.isFinite(position)) {
            return List.of();
        }

        double maxDistanceSqr = Math.max(0.0D, maxDistance) * Math.max(0.0D, maxDistance);
        List<CableRecord> nearby = new ArrayList<>();
        for (CableRecord record : cables.values()) {
            if (isNear(record, position, maxDistanceSqr)) {
                nearby.add(record);
            }
        }
        return nearby;
    }

    static boolean isNear(CableRecord record, Vec3 position, double maxDistanceSqr) {
        if (record == null || record.points().isEmpty() || !FiberCableHitTest.isFinite(position)) {
            return false;
        }

        for (int index = 0; index < record.points().size(); index++) {
            Vec3 start = record.points().get(index);
            Vec3 end = index + 1 < record.points().size() ? record.points().get(index + 1) : start;
            Vec3 midpoint = start.lerp(end, 0.5D);
            if (position.distanceToSqr(start) <= maxDistanceSqr
                    || position.distanceToSqr(end) <= maxDistanceSqr
                    || position.distanceToSqr(midpoint) <= maxDistanceSqr) {
                return true;
            }
        }
        return false;
    }

    private static List<Vec3> sanitizePoints(List<Vec3> points) {
        if (points == null || points.isEmpty()) {
            return List.of();
        }

        List<Vec3> sanitized = new ArrayList<>();
        int limit = Math.min(HARD_POINT_LIMIT, Math.max(2, AddonConfig.fiberRouteMaximumPoints() + 1));
        for (Vec3 point : points) {
            if (!FiberCableHitTest.isFinite(point)) {
                continue;
            }
            if (sanitized.isEmpty() || !sanitized.get(sanitized.size() - 1).closerThan(point, 0.02D)) {
                sanitized.add(point);
            }
            if (sanitized.size() >= limit) {
                break;
            }
        }
        return sanitized.size() >= 2 ? List.copyOf(sanitized) : List.of();
    }

    private static CableRecord readRecord(CompoundTag tag) {
        if (tag == null || !tag.hasUUID(TAG_CABLE_ID) || !tag.contains(TAG_POINTS, Tag.TAG_LIST)) {
            return null;
        }

        List<Vec3> points = readPoints(tag.getList(TAG_POINTS, Tag.TAG_COMPOUND));
        if (points.size() < 2) {
            return null;
        }

        UUID ownerId = tag.hasUUID(TAG_OWNER_ID) ? tag.getUUID(TAG_OWNER_ID) : null;
        return new CableRecord(
                tag.getUUID(TAG_CABLE_ID),
                tag.getString(TAG_DIMENSION),
                ownerId,
                tag.getLong(TAG_CREATED_GAME_TIME),
                points
        );
    }

    private static CompoundTag writeRecord(CableRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(TAG_CABLE_ID, record.cableId());
        tag.putString(TAG_DIMENSION, record.dimension());
        if (record.ownerId() != null) {
            tag.putUUID(TAG_OWNER_ID, record.ownerId());
        }
        tag.putLong(TAG_CREATED_GAME_TIME, record.createdGameTime());
        tag.put(TAG_POINTS, writePoints(record.points()));
        return tag;
    }

    private static List<Vec3> readPoints(ListTag list) {
        List<Vec3> points = new ArrayList<>();
        int count = Math.min(HARD_POINT_LIMIT, list.size());
        for (int index = 0; index < count; index++) {
            CompoundTag tag = list.getCompound(index);
            Vec3 point = new Vec3(tag.getDouble(TAG_X), tag.getDouble(TAG_Y), tag.getDouble(TAG_Z));
            if (FiberCableHitTest.isFinite(point)) {
                points.add(point);
            }
        }
        return sanitizePoints(points);
    }

    private static ListTag writePoints(List<Vec3> points) {
        ListTag list = new ListTag();
        for (Vec3 point : sanitizePoints(points)) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble(TAG_X, point.x);
            tag.putDouble(TAG_Y, point.y);
            tag.putDouble(TAG_Z, point.z);
            list.add(tag);
        }
        return list;
    }

    public record CableRecord(UUID cableId, String dimension, UUID ownerId, long createdGameTime, List<Vec3> points) {
        public CableRecord {
            points = List.copyOf(points);
        }
    }
}
