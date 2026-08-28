package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record CubedFpvInputMessage(UUID droneUuid, short keys, double mouseX, double mouseY, int sequence) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CubedFpvInputMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "cubed_fpv_input"));

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

    public static void handle(CubedFpvInputMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender) || message.droneUuid == null) {
            return;
        }

        Entity drone = sender.serverLevel().getEntity(message.droneUuid);
        if (drone instanceof GlideBombEntity
                || !(drone instanceof CubedFpvDroneEntity)
                || !SbwCompat.isUsingLinkedMonitorForDrone(sender, drone)) {
            return;
        }

        CubedFpvInputState.receive(sender, drone, message.keys, message.mouseX, message.mouseY, message.sequence);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
