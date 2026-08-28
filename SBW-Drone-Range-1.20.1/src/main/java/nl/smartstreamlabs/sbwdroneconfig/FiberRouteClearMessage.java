package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record FiberRouteClearMessage(
        UUID droneId,
        UUID sessionId,
        int revision
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FiberRouteClearMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "fiber_route_clear")
    );

    public static void encode(FiberRouteClearMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.droneId);
        buffer.writeUUID(message.sessionId);
        buffer.writeVarInt(message.revision);
    }

    public static FiberRouteClearMessage decode(FriendlyByteBuf buffer) {
        return new FiberRouteClearMessage(buffer.readUUID(), buffer.readUUID(), buffer.readVarInt());
    }

    public static void handle(FiberRouteClearMessage message, IPayloadContext context) {
        context.enqueueWork(() -> DroneFiberOpticClient.handleRouteClear(message));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
