package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class LaserTowerWrenchEvents {
    private LaserTowerWrenchEvents() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!LaserTowerDroneDefenseSystem.isLaserTower(event.getTarget())
                || !LaserTowerDroneDefenseSystem.isLaserTowerWrench(player.getItemInHand(event.getHand()))) {
            return;
        }

        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            LaserTowerDroneDefenseSystem.openWhitelist(serverPlayer, event.getTarget());
        }

        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
        event.setCanceled(true);
    }
}
