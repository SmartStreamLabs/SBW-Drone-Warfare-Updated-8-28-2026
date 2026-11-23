package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateJammerRangeMessage(BlockPos blockPos, int range) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateJammerRangeMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "update_jammer_range"));

    public static void encode(UpdateJammerRangeMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeVarInt(message.range);
    }

    public static UpdateJammerRangeMessage decode(FriendlyByteBuf buffer) {
        return new UpdateJammerRangeMessage(buffer.readBlockPos(), buffer.readVarInt());
    }

    public static void handle(UpdateJammerRangeMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.level().getBlockEntity(message.blockPos) instanceof ConfigurableJammerBlockEntity blockEntity)) {
            return;
        }
        if (!player.blockPosition().closerThan(message.blockPos, 12.0D) || !blockEntity.canConfigure(player)) {
            return;
        }

        int clampedRange = Math.max(blockEntity.getMinJammerRange(), Math.min(blockEntity.getMaxJammerRange(), message.range));
        blockEntity.setJammerRange(clampedRange);
        player.displayClientMessage(Component.translatable("message.sbwdroneconfig.jammer_range_updated", clampedRange), true);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
