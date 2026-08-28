package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateJammerRangeMessage(BlockPos blockPos, int range) {
    public static void encode(UpdateJammerRangeMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeVarInt(message.range);
    }

    public static UpdateJammerRangeMessage decode(FriendlyByteBuf buffer) {
        return new UpdateJammerRangeMessage(buffer.readBlockPos(), buffer.readVarInt());
    }

    public static void handle(UpdateJammerRangeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level().getBlockEntity(message.blockPos) instanceof ConfigurableJammerBlockEntity blockEntity)) {
                return;
            }
            if (!player.blockPosition().closerThan(message.blockPos, 12.0D) || !blockEntity.canConfigure(player)) {
                return;
            }

            int clampedRange = Math.max(blockEntity.getMinJammerRange(), Math.min(blockEntity.getMaxJammerRange(), message.range));
            blockEntity.setJammerRange(clampedRange);
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.jammer_range_updated", clampedRange), true);
        });
        context.setPacketHandled(true);
    }
}
