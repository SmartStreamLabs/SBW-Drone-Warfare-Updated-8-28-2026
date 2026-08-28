package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DroneJammerEvents {
    private DroneJammerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        PortableFpvDetectorServer.tickHeldDetector(player);
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
