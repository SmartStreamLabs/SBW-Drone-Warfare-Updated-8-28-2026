package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record CubedFpvInputMessage(UUID droneUuid, short keys, double mouseX, double mouseY, int sequence) {
    public static CubedFpvInputMessage decode(FriendlyByteBuf buffer) {
        return new CubedFpvInputMessage(
                buffer.readUUID(),
                buffer.readShort(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt()
        );
    }

    public static void encode(CubedFpvInputMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneUuid);
        buffer.writeShort(message.keys);
        buffer.writeDouble(message.mouseX);
        buffer.writeDouble(message.mouseY);
        buffer.writeVarInt(message.sequence);
    }

    public static void handle(CubedFpvInputMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || message.droneUuid == null) {
                return;
            }

            Entity drone = sender.serverLevel().getEntity(message.droneUuid);
            if (drone instanceof GlideBombEntity
                    || !(drone instanceof CubedFpvDroneEntity)
                    || !SbwCompat.isUsingLinkedMonitorForDrone(sender, drone)) {
                return;
            }

            CubedFpvInputState.receive(sender, drone, message.keys, message.mouseX, message.mouseY, message.sequence);
        });
        context.setPacketHandled(true);
    }
}
