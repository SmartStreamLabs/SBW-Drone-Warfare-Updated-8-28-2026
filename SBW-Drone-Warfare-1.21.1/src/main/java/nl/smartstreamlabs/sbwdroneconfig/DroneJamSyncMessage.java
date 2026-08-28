package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record DroneJamSyncMessage(UUID droneId, float jamProgress, boolean hardJammed) {
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

    public static void handle(DroneJamSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DroneJamOverlayClient.handleSync(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
