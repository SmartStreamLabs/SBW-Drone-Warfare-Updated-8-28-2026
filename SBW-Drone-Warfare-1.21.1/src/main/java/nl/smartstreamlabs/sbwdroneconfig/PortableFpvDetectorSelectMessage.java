package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record PortableFpvDetectorSelectMessage(UUID targetId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PortableFpvDetectorSelectMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "portable_fpv_detector_select"));

    public static void encode(PortableFpvDetectorSelectMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.targetId() != null);
        if (message.targetId() != null) {
            buffer.writeUUID(message.targetId());
        }
    }

    public static PortableFpvDetectorSelectMessage decode(FriendlyByteBuf buffer) {
        return new PortableFpvDetectorSelectMessage(buffer.readBoolean() ? buffer.readUUID() : null);
    }

    public static void handle(PortableFpvDetectorSelectMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PortableFpvDetectorServer.selectTarget(player, message.targetId());
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
