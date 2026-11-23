package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record FiberCableCutEffectMessage(
        int droneEntityId,
        UUID sessionId,
        double x,
        double y,
        double z
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FiberCableCutEffectMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "fiber_cable_cut_effect"));

    public static void encode(FiberCableCutEffectMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.droneEntityId);
        buffer.writeUUID(message.sessionId);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    public static FiberCableCutEffectMessage decode(FriendlyByteBuf buffer) {
        return new FiberCableCutEffectMessage(
                buffer.readVarInt(),
                buffer.readUUID(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void handle(FiberCableCutEffectMessage message, IPayloadContext context) {
        context.enqueueWork(() -> DroneFiberOpticClient.handleCutEffect(message));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
