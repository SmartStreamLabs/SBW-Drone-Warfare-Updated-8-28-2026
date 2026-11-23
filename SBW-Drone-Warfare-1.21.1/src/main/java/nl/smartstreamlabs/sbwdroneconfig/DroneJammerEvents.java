package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class DroneJammerEvents {
    private DroneJammerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PortableFpvDetectorServer.tickHeldDetector(player);
        }
    }

    public static boolean isHoldingActiveJammer(net.minecraft.world.entity.player.Player player) {
        return DroneJammerItem.isActiveJammerHeld(player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PortableFpvDetectorServer.clear(player);
        }
    }
}
