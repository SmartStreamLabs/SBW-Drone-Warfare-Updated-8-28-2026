package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;

@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class PilotMapServer {
    private static final Set<UUID> hidden = new HashSet<>();
    private static final Map<UUID,ServerPlayer> joining = new HashMap<>();

    /** Read the existing validated session, not a held item or a client GUI flag. */
    public static boolean controlling(ServerPlayer player) {
        if (!player.isAlive() || player.hasDisconnected() || player.isSpectator()) return false;
        Entity drone = DroneClientChunkSyncManager.controlledDrone(player);
        if (drone == null) drone = PlayerDroneAnchorManager.controlledDrone(player);
        return drone != null && !(drone instanceof Id01Entity) && !(drone instanceof Fl01Entity)
                && !MissionManager.owns(drone);
    }

    private static Set<UUID> current(MinecraftServer server) {
        Set<UUID> result = new HashSet<>();
        if (PilotMapConfig.HIDE.get()) for (var player : server.getPlayerList().getPlayers())
            if (controlling(player)) result.add(player.getUUID());
        return result;
    }

    private static void broadcast(MinecraftServer server, boolean hide, Collection<UUID> ids) {
        var list = new ArrayList<>(ids);
        for (int from = 0; from < list.size(); from += PilotMapPacket.LIMIT) {
            var packet = new PilotMapPacket(false, hide, List.copyOf(list.subList(from, Math.min(from + PilotMapPacket.LIMIT, list.size()))));
            for (var player : server.getPlayerList().getPlayers()) AddonNetwork.pilotMap(player, packet);
        }
    }

    private static void refresh(MinecraftServer server) {
        var next = current(server);
        var added = new HashSet<>(next); added.removeAll(hidden);
        var removed = new HashSet<>(hidden); removed.removeAll(next);
        hidden.clear(); hidden.addAll(next);
        broadcast(server, false, removed);
        broadcast(server, true, added);
    }

    private static void snapshot(ServerPlayer player) {
        var list = new ArrayList<>(hidden);
        if (list.isEmpty()) AddonNetwork.pilotMap(player, new PilotMapPacket(true, true, List.of()));
        else for (int from = 0; from < list.size(); from += PilotMapPacket.LIMIT)
            AddonNetwork.pilotMap(player, new PilotMapPacket(from == 0, true, List.copyOf(list.subList(from, Math.min(from + PilotMapPacket.LIMIT, list.size())))));
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) { refresh(server); for(var entry:List.copyOf(joining.entrySet())) {var p=entry.getValue();if(p.hasDisconnected()){joining.remove(entry.getKey());continue;}if(p.tickCount>=2){snapshot(p);joining.remove(entry.getKey());}} }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { refresh(player.server); snapshot(player); joining.put(player.getUUID(),player); }
    }

    private static void reveal(ServerPlayer player) {
        joining.remove(player.getUUID());
        if (hidden.remove(player.getUUID())) broadcast(player.server, false, List.of(player.getUUID()));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) reveal(player);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { reveal(player); snapshot(player); }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) reveal(player);
    }
    @SubscribeEvent public static void stop(ServerStoppingEvent event) { hidden.clear(); joining.clear(); }
    private PilotMapServer() {}
}
