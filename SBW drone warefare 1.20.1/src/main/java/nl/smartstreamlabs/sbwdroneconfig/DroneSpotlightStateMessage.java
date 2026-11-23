package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record DroneSpotlightStateMessage(UUID droneId, boolean active) {
    public static void encode(DroneSpotlightStateMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeBoolean(message.active);
    }

    public static DroneSpotlightStateMessage decode(FriendlyByteBuf buffer) {
        return new DroneSpotlightStateMessage(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(DroneSpotlightStateMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DroneSpotlightClient.handleSpotlightState(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
