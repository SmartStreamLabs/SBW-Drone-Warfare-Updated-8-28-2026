package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record PortableFpvDetectorSelectMessage(UUID targetId) {
    public static void encode(PortableFpvDetectorSelectMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.targetId() != null);
        if (message.targetId() != null) {
            buffer.writeUUID(message.targetId());
        }
    }

    public static PortableFpvDetectorSelectMessage decode(FriendlyByteBuf buffer) {
        return new PortableFpvDetectorSelectMessage(buffer.readBoolean() ? buffer.readUUID() : null);
    }

    public static void handle(PortableFpvDetectorSelectMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PortableFpvDetectorServer.selectTarget(player, message.targetId());
            }
        });
        context.setPacketHandled(true);
    }
}
