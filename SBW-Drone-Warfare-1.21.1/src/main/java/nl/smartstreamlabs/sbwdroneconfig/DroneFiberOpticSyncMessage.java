package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

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
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DroneFiberOpticSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_fiber_optic_sync"));

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

    public static void handle(DroneFiberOpticSyncMessage message, IPayloadContext context) {
        DroneFiberOpticClient.handleSync(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
