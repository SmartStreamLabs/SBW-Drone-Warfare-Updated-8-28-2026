package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record NetBulkPlacementConfirmMessage(
        ResourceLocation itemId,
        BlockPos pos1,
        BlockPos pos2,
        Direction.Axis planeAxis,
        Direction facing,
        ResourceLocation dimension,
        UUID sessionId
) {
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

    public static void handle(NetBulkPlacementConfirmMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                NetBulkPlacementServer.handleConfirm(sender, message);
            }
        });
        context.setPacketHandled(true);
    }
}
