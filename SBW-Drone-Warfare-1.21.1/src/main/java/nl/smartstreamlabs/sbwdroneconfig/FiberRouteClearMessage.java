package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record FiberRouteClearMessage(
        UUID droneId,
        UUID sessionId,
        int revision
) {
    public static void encode(FiberRouteClearMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeUUID(message.sessionId);
        buffer.writeVarInt(message.revision);
    }

    public static FiberRouteClearMessage decode(FriendlyByteBuf buffer) {
        return new FiberRouteClearMessage(buffer.readUUID(), buffer.readUUID(), buffer.readVarInt());
    }

    public static void handle(FiberRouteClearMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DroneFiberOpticClient.handleRouteClear(message);
        contextSupplier.get().setPacketHandled(true);
    }
}
