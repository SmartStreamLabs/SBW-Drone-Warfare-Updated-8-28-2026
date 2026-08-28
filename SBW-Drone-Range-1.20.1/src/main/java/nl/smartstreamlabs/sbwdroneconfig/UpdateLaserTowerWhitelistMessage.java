package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateLaserTowerWhitelistMessage(int entityId, String gamertag, boolean remove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateLaserTowerWhitelistMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "update_laser_tower_whitelist"));

    public static void encode(UpdateLaserTowerWhitelistMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeUtf(message.gamertag, 64);
        buffer.writeBoolean(message.remove);
    }

    public static UpdateLaserTowerWhitelistMessage decode(FriendlyByteBuf buffer) {
        return new UpdateLaserTowerWhitelistMessage(buffer.readVarInt(), buffer.readUtf(64), buffer.readBoolean());
    }

    public static void handle(UpdateLaserTowerWhitelistMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        Entity entity = player.serverLevel().getEntity(message.entityId());
        if (!LaserTowerDroneDefenseSystem.canConfigure(player, entity)) {
            return;
        }

        boolean changed = message.remove()
                ? LaserTowerDroneDefenseSystem.removeWhitelistedGamertag(entity, message.gamertag())
                : LaserTowerDroneDefenseSystem.addWhitelistedGamertag(entity, message.gamertag());
        if (changed) {
            player.displayClientMessage(Component.translatable(
                    message.remove()
                            ? "message.sbwdroneconfig.laser_tower_whitelist_removed"
                            : "message.sbwdroneconfig.laser_tower_whitelist_added",
                    message.gamertag()), true);
            LaserTowerDroneDefenseSystem.openWhitelist(player, entity);
        } else {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.laser_tower_whitelist_invalid"), true);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
