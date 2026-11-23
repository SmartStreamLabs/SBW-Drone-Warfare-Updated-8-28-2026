package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record NetBulkPlacementConfirmMessage(
        ResourceLocation itemId,
        BlockPos pos1,
        BlockPos pos2,
        Direction.Axis planeAxis,
        Direction facing,
        ResourceLocation dimension,
        UUID sessionId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NetBulkPlacementConfirmMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "net_bulk_placement_confirm"));

    public static void encode(NetBulkPlacementConfirmMessage message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.itemId);
        buffer.writeBlockPos(message.pos1);
        buffer.writeBlockPos(message.pos2);
        buffer.writeEnum(message.planeAxis);
        buffer.writeEnum(message.facing);
        buffer.writeResourceLocation(message.dimension);
        buffer.writeUUID(message.sessionId);
    }

    public static NetBulkPlacementConfirmMessage decode(FriendlyByteBuf buffer) {
        return new NetBulkPlacementConfirmMessage(
                buffer.readResourceLocation(),
                buffer.readBlockPos(),
                buffer.readBlockPos(),
                buffer.readEnum(Direction.Axis.class),
                buffer.readEnum(Direction.class),
                buffer.readResourceLocation(),
                buffer.readUUID()
        );
    }

    public static void handle(NetBulkPlacementConfirmMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                NetBulkPlacementServer.handleConfirm(sender, message);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
