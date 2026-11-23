package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.*;
import java.util.*;

/** Supplementary leases only: ordinary remote views use vanilla's throttled player tickets. */
public final class ChunkTicketManager {
    private static final TicketType<ChunkPos> TYPE=TicketType.create("sbwdroneconfig:flight",Comparator.comparingLong(ChunkPos::toLong));
    private record Owner(UUID id, boolean body) {}
    private static final Map<ServerLevel,DroneChunkLeases<Owner>> LEVELS=new LinkedHashMap<>();
    private record Area(int x,int z,int radius,Set<Long> chunks) {}
    private static final Map<Entity,Area> AREAS=new WeakHashMap<>();
    private static long tick;
    private ChunkTicketManager() {}
    public static void registerValidationCallback() {
        // Previous releases persisted camera tickets. No camera is valid across a restart.
        ForgeChunkManager.setForcedChunkLoadingCallback(SbwDroneRangeConfig.MOD_ID,(level,tickets) -> {
            for(UUID owner:tickets.getEntityTickets().keySet()) tickets.removeAllTickets(owner);
        });
    }
    private static DroneChunkLeases<Owner> leases(ServerLevel level) {
        return LEVELS.computeIfAbsent(level,l -> new DroneChunkLeases<>(4096,256));
    }
    public static void touchBody(ServerPlayer player) {
        leases(player.serverLevel()).touch(new Owner(player.getUUID(),true),Set.of(player.chunkPosition().toLong()),tick);
    }
    public static void releaseBody(ServerPlayer player) {
        LEVELS.values().forEach(pool -> pool.release(new Owner(player.getUUID(),true)));
    }
    public static void touchDrone(Entity drone,int radius) {
        if(!(drone.level() instanceof ServerLevel level) || !AddonConfig.enableDroneChunkLoading() || (!SbwCompat.isDrone(drone) && !(drone instanceof VatagEntity) && !(drone instanceof MedicalTrailerEntity))) return;
        if(MissionManager.tagged(drone))return;
        if(drone.getPersistentData().getBoolean(MissionManager.NO_TICKETS)){
            var controller=SbwCompat.getDroneControllerPlayer(drone);
            if(controller==null || !SbwCompat.isUsingLinkedMonitorForDrone(controller,drone))return;
            drone.getPersistentData().remove(MissionManager.NO_TICKETS);
        }
        ChunkPos center=drone.chunkPosition();
        radius=Math.max(0,Math.min(4,radius));
        Area area=AREAS.get(drone);
        if(area==null || area.x!=center.x || area.z!=center.z || area.radius!=radius) {
            area=new Area(center.x,center.z,radius,DroneChunkGeometry.square(center.x,center.z,radius));
            AREAS.put(drone,area);
        }
        leases(level).touch(new Owner(drone.getUUID(),false),area.chunks,tick);
    }
    public static void touchAuxiliary(ServerLevel level,UUID id,Set<Long> chunks){if(AddonConfig.enableDroneChunkLoading())leases(level).touch(new Owner(id,false),chunks,tick);}
    public static void releaseDrone(Entity drone){Owner owner=new Owner(drone.getUUID(),false);LEVELS.values().forEach(pool->pool.release(owner));AREAS.remove(drone);}
    public static void onDroneBaseTick(Entity entity) {
        if(!entity.level().isClientSide() && AddonConfig.enableDroneChunkLoading() && SbwCompat.isDrone(entity)
            && entity.isAlive() && !entity.onGround() && entity.getDeltaMovement().lengthSqr()>0.0025)
            touchDrone(entity,entity instanceof Id01Entity?1:AddonConfig.droneChunkLoadRadius());
    }
    private static DroneChunkLeases.Backend backend(ServerLevel level) {
        return new DroneChunkLeases.Backend() {
            public void add(long value) { ChunkPos p=new ChunkPos(value); level.getChunkSource().addRegionTicket(TYPE,p,2,p,true); }
            public void remove(long value) { ChunkPos p=new ChunkPos(value); level.getChunkSource().removeRegionTicket(TYPE,p,2,p,true); }
            public boolean ready(long value) { ChunkPos p=new ChunkPos(value); return level.getChunkSource().getChunkNow(p.x,p.z)!=null; }
        };
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void tick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END) return;
        tick++;
        // Global budgets are shared across dimensions; no per-dimension multiplication.
        int starts=AddonConfig.DRONE_EXTRA_TICKETS_PER_TICK_VALUE.get();
        int pendingLimit=AddonConfig.DRONE_EXTRA_PENDING_CHUNKS_VALUE.get();
        for(var entry:LEVELS.entrySet()) {
            int otherPending=LEVELS.entrySet().stream().filter(e->e.getKey()!=entry.getKey())
                .mapToInt(e->e.getValue().pending(backend(e.getKey()))).sum();
            int capacity=Math.max(0,AddonConfig.DRONE_EXTRA_TICKET_LIMIT_VALUE.get()-tickets());
            starts-=entry.getValue().tick(tick,Math.min(starts,capacity),Math.max(0,pendingLimit-otherPending),backend(entry.getKey()));
        }
        LEVELS.entrySet().removeIf(e->e.getValue().owners()==0 && e.getValue().tickets()==0);
    }
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if(event.getLevel().isClientSide() || (!SbwCompat.isDrone(event.getEntity()) && !(event.getEntity() instanceof VatagEntity))) return;
        Owner owner=new Owner(event.getEntity().getUUID(),false);
        LEVELS.values().forEach(pool -> pool.release(owner));
        AREAS.remove(event.getEntity());
    }
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event) {
        if(event.getLevel() instanceof ServerLevel level) {
            DroneChunkLeases<Owner> pool=LEVELS.remove(level);
            if(pool!=null) pool.clear(backend(level));
        }
    }
    @SubscribeEvent public static void stop(ServerStoppingEvent event) {
        LEVELS.forEach((level,pool)->pool.clear(backend(level))); LEVELS.clear(); AREAS.clear(); tick=0;
    }
    public static int tickets() { return LEVELS.values().stream().mapToInt(DroneChunkLeases::tickets).sum(); }
}
