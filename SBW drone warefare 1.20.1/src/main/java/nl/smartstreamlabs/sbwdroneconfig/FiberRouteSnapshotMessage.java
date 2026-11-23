package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record FiberRouteSnapshotMessage(
        UUID droneId,
        int droneEntityId,
        UUID sessionId,
        int revision,
        List<FiberCableRouteState.RoutePoint> points
) {
    private static final int HARD_POINT_LIMIT = 2048;

    public FiberRouteSnapshotMessage {
        points = List.copyOf(points);
    }

    public static void encode(FiberRouteSnapshotMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeVarInt(message.droneEntityId);
        buffer.writeUUID(message.sessionId);
        buffer.writeVarInt(message.revision);
        buffer.writeVarInt(message.points.size());
        for (FiberCableRouteState.RoutePoint point : message.points) {
            buffer.writeDouble(point.x());
            buffer.writeDouble(point.y());
            buffer.writeDouble(point.z());
        }
    }

    public static FiberRouteSnapshotMessage decode(FriendlyByteBuf buffer) {
        UUID droneId = buffer.readUUID();
        int droneEntityId = buffer.readVarInt();
        UUID sessionId = buffer.readUUID();
        int revision = buffer.readVarInt();
        int encodedCount = Math.max(0, buffer.readVarInt());
        int retainedCount = Math.min(HARD_POINT_LIMIT, encodedCount);
        List<FiberCableRouteState.RoutePoint> points = new ArrayList<>(retainedCount);
        for (int index = 0; index < encodedCount; index++) {
            FiberCableRouteState.RoutePoint point = new FiberCableRouteState.RoutePoint(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            if (index < retainedCount && point.isFinite()) {
                points.add(point);
            }
        }
        return new FiberRouteSnapshotMessage(droneId, droneEntityId, sessionId, revision, points);
    }

    public static void handle(FiberRouteSnapshotMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DroneFiberOpticClient.handleRouteSnapshot(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
