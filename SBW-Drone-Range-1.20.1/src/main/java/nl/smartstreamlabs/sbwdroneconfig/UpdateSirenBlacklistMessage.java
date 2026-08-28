package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateSirenBlacklistMessage(BlockPos blockPos, String gamertag, boolean remove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateSirenBlacklistMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "update_siren_blacklist"));

    public static void encode(UpdateSirenBlacklistMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeUtf(message.gamertag);
        buffer.writeBoolean(message.remove);
    }

    public static UpdateSirenBlacklistMessage decode(FriendlyByteBuf buffer) {
        return new UpdateSirenBlacklistMessage(buffer.readBlockPos(), buffer.readUtf(64), buffer.readBoolean());
    }

    public static void handle(UpdateSirenBlacklistMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.level().getBlockEntity(message.blockPos) instanceof DroneDetectionSirenBlockEntity blockEntity)) {
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
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
