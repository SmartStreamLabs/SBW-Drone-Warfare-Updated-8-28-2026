package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record DroneJamSyncMessage(UUID droneId, float jamProgress, boolean hardJammed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DroneJamSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_jam_sync"));

    public static void encode(DroneJamSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeFloat(message.jamProgress);
        buffer.writeBoolean(message.hardJammed);
    }

    public static DroneJamSyncMessage decode(FriendlyByteBuf buffer) {
        return new DroneJamSyncMessage(
                buffer.readUUID(),
                buffer.readFloat(),
                buffer.readBoolean()
        );
    }

    public static void handle(DroneJamSyncMessage message, IPayloadContext context) {
        DroneJamOverlayClient.handleSync(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
