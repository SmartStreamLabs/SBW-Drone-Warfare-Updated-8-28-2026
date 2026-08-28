package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateJammerWhitelistMessage(BlockPos blockPos, String gamertag, boolean remove) {
    public static void encode(UpdateJammerWhitelistMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeUtf(message.gamertag);
        buffer.writeBoolean(message.remove);
    }

    public static UpdateJammerWhitelistMessage decode(FriendlyByteBuf buffer) {
        return new UpdateJammerWhitelistMessage(buffer.readBlockPos(), buffer.readUtf(64), buffer.readBoolean());
    }

    public static void handle(UpdateJammerWhitelistMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level().getBlockEntity(message.blockPos) instanceof ConfigurableJammerBlockEntity blockEntity)) {
                return;
            }
            if (!player.blockPosition().closerThan(message.blockPos, 12.0D) || !blockEntity.canConfigure(player)) {
                return;
            }

            String trimmed = message.gamertag == null ? "" : message.gamertag.trim();
            boolean changed = message.remove ? blockEntity.removeWhitelistedGamertag(trimmed) : blockEntity.addWhitelistedGamertag(trimmed);
            if (!changed) {
                player.displayClientMessage(Component.translatable("message.sbwdroneconfig.jammer_whitelist_invalid"), true);
                return;
            }

            player.displayClientMessage(
                    Component.translatable(
                            message.remove
                                    ? "message.sbwdroneconfig.jammer_whitelist_removed"
                                    : "message.sbwdroneconfig.jammer_whitelist_added",
                            trimmed
                    ),
                    true
            );
            AddonNetwork.openJammerConfig(player, OpenJammerConfigMessage.fromBlockEntity(message.blockPos, blockEntity));
        });
        context.setPacketHandled(true);
    }
}
