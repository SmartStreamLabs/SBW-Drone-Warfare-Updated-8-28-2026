package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record DroneEnergySyncMessage(UUID droneId, int currentEnergy, int maxEnergy, boolean outOfEnergy, boolean lowEnergy) {
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

    public static void handle(DroneEnergySyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DroneEnergyClient.handleSync(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
