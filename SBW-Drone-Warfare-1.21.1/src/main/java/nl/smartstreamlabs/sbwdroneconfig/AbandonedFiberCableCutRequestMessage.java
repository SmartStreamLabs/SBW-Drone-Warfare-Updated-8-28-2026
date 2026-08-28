package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record AbandonedFiberCableCutRequestMessage(
        UUID cableId,
        int claimedSegmentIndex,
        double hitX,
        double hitY,
        double hitZ
) {
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

    public static void handle(AbandonedFiberCableCutRequestMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            AbandonedFiberCableManager.handleCutRequest(sender, message);
        });
        context.setPacketHandled(true);
    }

    Vec3 claimedHitPosition() {
        return new Vec3(hitX, hitY, hitZ);
    }
}
