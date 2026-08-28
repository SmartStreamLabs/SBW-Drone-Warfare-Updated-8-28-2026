package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateLaserTowerWhitelistMessage(int entityId, String gamertag, boolean remove) {
    public static void encode(UpdateLaserTowerWhitelistMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeUtf(message.gamertag, 64);
        buffer.writeBoolean(message.remove);
    }

    public static UpdateLaserTowerWhitelistMessage decode(FriendlyByteBuf buffer) {
        return new UpdateLaserTowerWhitelistMessage(buffer.readVarInt(), buffer.readUtf(64), buffer.readBoolean());
    }

    public static void handle(UpdateLaserTowerWhitelistMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            Entity entity = player.level().getEntity(message.entityId());
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
        });
        context.setPacketHandled(true);
    }
}
