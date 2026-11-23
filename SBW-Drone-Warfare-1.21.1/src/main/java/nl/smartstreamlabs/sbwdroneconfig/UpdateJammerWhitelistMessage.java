package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateJammerWhitelistMessage(BlockPos blockPos, String gamertag, boolean remove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateJammerWhitelistMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "update_jammer_whitelist"));

    public static void encode(UpdateJammerWhitelistMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeUtf(message.gamertag);
        buffer.writeBoolean(message.remove);
    }

    public static UpdateJammerWhitelistMessage decode(FriendlyByteBuf buffer) {
        return new UpdateJammerWhitelistMessage(buffer.readBlockPos(), buffer.readUtf(64), buffer.readBoolean());
    }

    public static void handle(UpdateJammerWhitelistMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.level().getBlockEntity(message.blockPos) instanceof ConfigurableJammerBlockEntity blockEntity)) {
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
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
