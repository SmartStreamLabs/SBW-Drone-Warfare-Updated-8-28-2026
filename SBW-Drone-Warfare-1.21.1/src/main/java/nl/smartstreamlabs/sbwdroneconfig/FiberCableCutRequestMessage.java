package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record FiberCableCutRequestMessage(
        int droneEntityId,
        UUID sessionId,
        int claimedRevision,
        int claimedSegmentIndex,
        double hitX,
        double hitY,
        double hitZ
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FiberCableCutRequestMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "fiber_cable_cut_request"));

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

    public static void handle(FiberCableCutRequestMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                FiberCableCutSystem.handleCutRequest(sender, message);
            }
        });
    }

    Vec3 claimedHitPosition() {
        return new Vec3(hitX, hitY, hitZ);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
