package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record AbandonedFiberCableCutRequestMessage(
        UUID cableId,
        int claimedSegmentIndex,
        double hitX,
        double hitY,
        double hitZ
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AbandonedFiberCableCutRequestMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "abandoned_fiber_cable_cut_request"));

    public AbandonedFiberCableCutRequestMessage(UUID cableId, int claimedSegmentIndex, Vec3 hitPosition) {
        this(
                cableId,
                claimedSegmentIndex,
                hitPosition == null ? Double.NaN : hitPosition.x,
                hitPosition == null ? Double.NaN : hitPosition.y,
                hitPosition == null ? Double.NaN : hitPosition.z
        );
    }

    public static void encode(AbandonedFiberCableCutRequestMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.cableId);
        buffer.writeVarInt(message.claimedSegmentIndex);
        buffer.writeDouble(message.hitX);
        buffer.writeDouble(message.hitY);
        buffer.writeDouble(message.hitZ);
    }

    public static AbandonedFiberCableCutRequestMessage decode(FriendlyByteBuf buffer) {
        return new AbandonedFiberCableCutRequestMessage(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void handle(AbandonedFiberCableCutRequestMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                AbandonedFiberCableManager.handleCutRequest(sender, message);
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
