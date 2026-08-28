package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record DroneEnergySyncMessage(UUID droneId, int currentEnergy, int maxEnergy, boolean outOfEnergy, boolean lowEnergy) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DroneEnergySyncMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_energy_sync"));

    public static void encode(DroneEnergySyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeVarInt(message.currentEnergy);
        buffer.writeVarInt(message.maxEnergy);
        buffer.writeBoolean(message.outOfEnergy);
        buffer.writeBoolean(message.lowEnergy);
    }

    public static DroneEnergySyncMessage decode(FriendlyByteBuf buffer) {
        return new DroneEnergySyncMessage(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public static void handle(DroneEnergySyncMessage message, IPayloadContext context) {
        DroneEnergyClient.handleSync(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
