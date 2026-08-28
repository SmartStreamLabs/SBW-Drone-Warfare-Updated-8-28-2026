package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VehicleAntiDroneNetStateMessage(int entityId, boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VehicleAntiDroneNetStateMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "vehicle_anti_drone_net_state"));

    public static void encode(VehicleAntiDroneNetStateMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeBoolean(message.active);
    }

    public static VehicleAntiDroneNetStateMessage decode(FriendlyByteBuf buffer) {
        return new VehicleAntiDroneNetStateMessage(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(VehicleAntiDroneNetStateMessage message, IPayloadContext context) {
        VehicleAntiDroneNetClient.handleVehicleNetState(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
