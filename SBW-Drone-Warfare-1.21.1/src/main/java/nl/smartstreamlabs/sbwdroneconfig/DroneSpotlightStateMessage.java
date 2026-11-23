package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record DroneSpotlightStateMessage(UUID droneId, boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DroneSpotlightStateMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_spotlight_state"));

    public static void encode(DroneSpotlightStateMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeBoolean(message.active);
    }

    public static DroneSpotlightStateMessage decode(FriendlyByteBuf buffer) {
        return new DroneSpotlightStateMessage(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(DroneSpotlightStateMessage message, IPayloadContext context) {
        DroneSpotlightClient.handleSpotlightState(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
