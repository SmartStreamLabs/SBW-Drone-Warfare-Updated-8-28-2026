package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record LucasFuelSyncMessage(UUID droneId, int currentFuel, int maxFuel, boolean emptyFuel, boolean lowFuel) {
    public static void encode(LucasFuelSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeVarInt(message.currentFuel);
        buffer.writeVarInt(message.maxFuel);
        buffer.writeBoolean(message.emptyFuel);
        buffer.writeBoolean(message.lowFuel);
    }

    public static LucasFuelSyncMessage decode(FriendlyByteBuf buffer) {
        return new LucasFuelSyncMessage(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public static void handle(LucasFuelSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        LucasFuelClient.handleSync(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
