package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record FiberCableCutRequestMessage(
        int droneEntityId,
        UUID sessionId,
        int claimedRevision,
        int claimedSegmentIndex,
        double hitX,
        double hitY,
        double hitZ
) {
    public FiberCableCutRequestMessage(int droneEntityId, UUID sessionId, int claimedRevision, int claimedSegmentIndex, Vec3 hitPosition) {
        this(
                droneEntityId,
                sessionId,
                claimedRevision,
                claimedSegmentIndex,
                hitPosition == null ? Double.NaN : hitPosition.x,
                hitPosition == null ? Double.NaN : hitPosition.y,
                hitPosition == null ? Double.NaN : hitPosition.z
        );
    }

    public static void encode(FiberCableCutRequestMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.droneEntityId);
        buffer.writeUUID(message.sessionId);
        buffer.writeVarInt(message.claimedRevision);
        buffer.writeVarInt(message.claimedSegmentIndex);
        buffer.writeDouble(message.hitX);
        buffer.writeDouble(message.hitY);
        buffer.writeDouble(message.hitZ);
    }

    public static FiberCableCutRequestMessage decode(FriendlyByteBuf buffer) {
        return new FiberCableCutRequestMessage(
                buffer.readVarInt(),
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void handle(FiberCableCutRequestMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            FiberCableCutSystem.handleCutRequest(sender, message);
        });
        context.setPacketHandled(true);
    }

    Vec3 claimedHitPosition() {
        return new Vec3(hitX, hitY, hitZ);
    }
}
