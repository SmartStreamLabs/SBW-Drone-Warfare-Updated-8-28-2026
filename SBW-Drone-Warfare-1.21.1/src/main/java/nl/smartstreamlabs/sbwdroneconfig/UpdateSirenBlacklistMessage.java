package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateSirenBlacklistMessage(BlockPos blockPos, String gamertag, boolean remove) {
    public static void encode(UpdateSirenBlacklistMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeUtf(message.gamertag);
        buffer.writeBoolean(message.remove);
    }

    public static UpdateSirenBlacklistMessage decode(FriendlyByteBuf buffer) {
        return new UpdateSirenBlacklistMessage(buffer.readBlockPos(), buffer.readUtf(64), buffer.readBoolean());
    }

    public static void handle(UpdateSirenBlacklistMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level().getBlockEntity(message.blockPos) instanceof DroneDetectionSirenBlockEntity blockEntity)) {
                return;
            }
            if (!player.blockPosition().closerThan(message.blockPos, 12.0D) || !blockEntity.canConfigure(player)) {
                return;
            }

            String trimmed = message.gamertag == null ? "" : message.gamertag.trim();
            boolean changed = message.remove ? blockEntity.removeBlacklistedGamertag(trimmed) : blockEntity.addBlacklistedGamertag(trimmed);
            if (!changed) {
                player.displayClientMessage(Component.translatable("message.sbwdroneconfig.siren_blacklist_invalid"), true);
                return;
            }

            if (player.serverLevel() != null) {
                blockEntity.refreshDetectionState(player.serverLevel());
            }

            player.displayClientMessage(
                    Component.translatable(
                            message.remove
                                    ? "message.sbwdroneconfig.siren_blacklist_removed"
                                    : "message.sbwdroneconfig.siren_blacklist_added",
                            trimmed
                    ),
                    true
            );
            AddonNetwork.openSirenConfig(player, new OpenSirenConfigMessage(message.blockPos, blockEntity.getBlacklistedGamertags()));
        });
        context.setPacketHandled(true);
    }
}
