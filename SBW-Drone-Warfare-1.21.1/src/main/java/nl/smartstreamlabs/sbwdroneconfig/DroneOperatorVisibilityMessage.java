package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record DroneOperatorVisibilityMessage(UUID playerId, boolean hidden) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DroneOperatorVisibilityMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_operator_visibility"));

    public static void encode(DroneOperatorVisibilityMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerId);
        buffer.writeBoolean(message.hidden);
    }

    public static DroneOperatorVisibilityMessage decode(FriendlyByteBuf buffer) {
        return new DroneOperatorVisibilityMessage(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(DroneOperatorVisibilityMessage message, IPayloadContext context) {
        DroneThermalVisionClient.handleDroneOperatorVisibility(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
