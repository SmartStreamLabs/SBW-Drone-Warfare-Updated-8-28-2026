package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;

/** Validated origins for vanilla's complete tracking pipeline. No direct packets or blocking loads. */
public final class DroneClientChunkSyncManager {
    private record View(ServerPlayer player, Entity drone) {}
    private static final Map<UUID,View> VIEWS=new HashMap<>();
    private static final Map<UUID,SectionPos> MOVED=new HashMap<>();
    private DroneClientChunkSyncManager() {}
    public static void touch(ServerPlayer player, Entity drone, int ignoredRadius) {
        if(!valid(player,drone)) return;
        VIEWS.put(player.getUUID(),new View(player,drone));
        ChunkTicketManager.touchBody(player);
        moveIfNeeded(player);
    }
    private static boolean valid(ServerPlayer player, Entity drone) {
        return AddonConfig.enableDroneRemoteChunkTracking()
            && player.isAlive() && !player.hasDisconnected() && drone.isAlive() && !drone.isRemoved()
            && player.level()==drone.level() && ((drone instanceof Rd01Entity && Rd01Sessions.controlling(player,drone)) || (drone instanceof Id01Entity && Id01Sessions.controlling(player,drone)) || (drone instanceof VatagEntity && VatagSessions.controlling(player,drone)) || (SbwCompat.isDrone(drone) && SbwCompat.hasEquippedFpvGoggles(player) && SbwCompat.isUsingLinkedMonitorForDrone(player,drone)));
    }
    public static Entity controlledDrone(ServerPlayer player) { View view=VIEWS.get(player.getUUID());return view!=null&&view.player==player&&valid(player,view.drone)?view.drone:null; }
    public static Vec3 trackingPosition(ServerPlayer player) {
        View view=VIEWS.get(player.getUUID());
        return view!=null && view.player==player && view.drone.level()==player.level()
            ? view.drone.position() : player.position();
    }
    public static SectionPos trackingSection(ServerPlayer player) {
        Vec3 p=trackingPosition(player);
        return SectionPos.of(DroneChunkGeometry.chunk(p.x),DroneChunkGeometry.chunk(p.y),DroneChunkGeometry.chunk(p.z));
    }
    public static boolean active(ServerPlayer player) { return VIEWS.containsKey(player.getUUID()); }
    public static int sessions() { return VIEWS.size(); }
    private static void moveIfNeeded(ServerPlayer player) {
        SectionPos section=trackingSection(player);
        if(!section.equals(MOVED.put(player.getUUID(),section))) player.serverLevel().getChunkSource().move(player);
    }
    public static void release(ServerPlayer player, boolean restore) {
        if(VIEWS.remove(player.getUUID())==null) return;
        MOVED.remove(player.getUUID());
        ChunkTicketManager.releaseBody(player);
        if(restore && !player.hasDisconnected()) player.serverLevel().getChunkSource().move(player);
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END) return;
        for(View view:List.copyOf(VIEWS.values())) {
            if(!valid(view.player,view.drone)) release(view.player,true);
            else { ChunkTicketManager.touchBody(view.player); moveIfNeeded(view.player); }
        }
    }
    private static void stopMonitors(ServerPlayer player) {
        for(var stack:player.getInventory().items) SbwCompat.stopUsingMonitor(stack);
        SbwCompat.stopUsingMonitor(player.getOffhandItem());
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        // Using is connection state, not a camera session that may resume from saved item data.
        if(event.getEntity() instanceof ServerPlayer p) { stopMonitors(p); release(p,false); }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer p) { stopMonitors(p); release(p,false); }
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.getEntity() instanceof ServerPlayer p) { stopMonitors(p); release(p,false); }
    }
    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer p) { stopMonitors(p); release(p,true); }
    }
    @SubscribeEvent public static void stop(ServerStoppingEvent event) { VIEWS.clear(); MOVED.clear(); }
}
