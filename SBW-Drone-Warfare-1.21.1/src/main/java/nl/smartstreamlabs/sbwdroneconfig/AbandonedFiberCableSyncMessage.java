package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AbandonedFiberCableSyncMessage(
        boolean replaceAll,
        List<AbandonedFiberCableData.CableRecord> cables
) implements CustomPacketPayload {
    private static final int HARD_CABLE_LIMIT = 512;
    private static final int HARD_POINT_LIMIT = 2049;
    public static final CustomPacketPayload.Type<AbandonedFiberCableSyncMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "abandoned_fiber_cable_sync")
    );

    public AbandonedFiberCableSyncMessage {
        cables = List.copyOf(cables);
    }

    public static void encode(AbandonedFiberCableSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.replaceAll);
        buffer.writeVarInt(message.cables.size());
        for (AbandonedFiberCableData.CableRecord cable : message.cables) {
            buffer.writeUUID(cable.cableId());
            buffer.writeUtf(cable.dimension());
            buffer.writeBoolean(cable.ownerId() != null);
            if (cable.ownerId() != null) {
                buffer.writeUUID(cable.ownerId());
            }
            buffer.writeLong(cable.createdGameTime());
            buffer.writeVarInt(cable.points().size());
            for (Vec3 point : cable.points()) {
                buffer.writeDouble(point.x);
                buffer.writeDouble(point.y);
                buffer.writeDouble(point.z);
            }
        }
    }

    public static AbandonedFiberCableSyncMessage decode(FriendlyByteBuf buffer) {
        boolean replaceAll = buffer.readBoolean();
        int encodedCableCount = Math.max(0, buffer.readVarInt());
        int retainedCableCount = Math.min(HARD_CABLE_LIMIT, encodedCableCount);
        List<AbandonedFiberCableData.CableRecord> cables = new ArrayList<>(retainedCableCount);
        for (int cableIndex = 0; cableIndex < encodedCableCount; cableIndex++) {
            UUID cableId = buffer.readUUID();
            String dimension = buffer.readUtf();
            UUID ownerId = buffer.readBoolean() ? buffer.readUUID() : null;
            long createdGameTime = buffer.readLong();
            int encodedPointCount = Math.max(0, buffer.readVarInt());
            int retainedPointCount = Math.min(HARD_POINT_LIMIT, encodedPointCount);
            List<Vec3> points = new ArrayList<>(retainedPointCount);
            for (int pointIndex = 0; pointIndex < encodedPointCount; pointIndex++) {
                Vec3 point = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
                if (pointIndex < retainedPointCount && FiberCableHitTest.isFinite(point)) {
                    points.add(point);
                }
            }
            if (cableIndex < retainedCableCount && points.size() >= 2) {
                cables.add(new AbandonedFiberCableData.CableRecord(cableId, dimension, ownerId, createdGameTime, points));
            }
        }
        return new AbandonedFiberCableSyncMessage(replaceAll, cables);
    }

    public static void handle(AbandonedFiberCableSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> AbandonedFiberCableClient.handleSync(message));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
