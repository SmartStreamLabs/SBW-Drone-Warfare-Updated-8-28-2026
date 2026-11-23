package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record DroneFiberOpticSyncMessage(
        UUID droneId,
        int droneEntityId,
        DroneLinkMode linkMode,
        boolean fiberSessionActive,
        double cableLength,
        int maxCableLength,
        float cableTension,
        int spoolPercent,
        int segmentCount,
        double anchorX,
        double anchorY,
        double anchorZ
) {
    public static void encode(DroneFiberOpticSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeVarInt(message.droneEntityId);
        buffer.writeEnum(message.linkMode);
        buffer.writeBoolean(message.fiberSessionActive);
        buffer.writeDouble(message.cableLength);
        buffer.writeVarInt(message.maxCableLength);
        buffer.writeFloat(message.cableTension);
        buffer.writeVarInt(message.spoolPercent);
        buffer.writeVarInt(message.segmentCount);
        buffer.writeDouble(message.anchorX);
        buffer.writeDouble(message.anchorY);
        buffer.writeDouble(message.anchorZ);
    }

    public static DroneFiberOpticSyncMessage decode(FriendlyByteBuf buffer) {
        return new DroneFiberOpticSyncMessage(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readEnum(DroneLinkMode.class),
                buffer.readBoolean(),
                buffer.readDouble(),
                buffer.readVarInt(),
                buffer.readFloat(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void handle(DroneFiberOpticSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DroneFiberOpticClient.handleSync(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
