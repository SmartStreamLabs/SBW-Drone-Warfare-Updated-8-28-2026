package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public final class VehicleAntiDroneNetSyncSystem {
    private static final java.util.Map<ServerPlayer,java.util.Map<Integer,Integer>> HEALTH_HASH=new java.util.WeakHashMap<>();
    private static final int SYNC_INTERVAL_TICKS = 10;
    private static final double SYNC_RANGE = 192.0D;
    private static final java.util.Map<ServerPlayer,Set<Integer>> KNOWN = new java.util.WeakHashMap<>();

    private VehicleAntiDroneNetSyncSystem() {
    }

    @SubscribeEvent
    public static void onServerLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.level.isClientSide()
                || !(event.level instanceof ServerLevel serverLevel)
                || !AddonConfig.vehicleAntiDroneNetEnabled()) {
            return;
        }

        Vc01Damage.cleanup(serverLevel);
        VehicleAntiDroneNetCollisionSystem.endTick(serverLevel);
        if (serverLevel.getGameTime() % SYNC_INTERVAL_TICKS == 0L) syncVehiclesNearPlayers(serverLevel);
    }

    public static void startTracking(ServerPlayer player,Entity target){var hashes=HEALTH_HASH.get(player);if(hashes!=null)hashes.remove(target.getId());if(SbwCompat.hasVehicleAntiDroneNetUpgrade(target)&&Vc01Geometry.profile(target)!=null)AddonNetwork.sendVehicleAntiDroneNetState(player,new VehicleAntiDroneNetStateMessage(target.getId(),true,Vc01Panels.packet(target)));}
    private static void syncVehiclesNearPlayers(ServerLevel serverLevel) {
        Set<Integer> syncedEntityIds = new HashSet<>();
        for (ServerPlayer player : serverLevel.players()) {
            AABB searchBox = player.getBoundingBox().inflate(SYNC_RANGE);
            var view=DroneClientChunkSyncManager.trackingPosition(player);
            java.util.List<AABB> regions=new java.util.ArrayList<>();regions.add(searchBox);if(view.distanceToSqr(player.position())>64)regions.add(AABB.ofSize(view,SYNC_RANGE*2,SYNC_RANGE*2,SYNC_RANGE*2));
            for(AABB region:regions)for (Entity vehicle : serverLevel.getEntities((Entity) null, region, SbwCompat::isVehicleNetHost)) {
                if (syncedEntityIds.add(vehicle.getId())) {
                    syncVehicleToNearbyPlayers(serverLevel, vehicle);
                }
            }
        }
    }

    private static void syncVehicleToNearbyPlayers(ServerLevel serverLevel, Entity vehicle) {
        VehicleAntiDroneNetCollisionSystem.track(vehicle);
        VehicleAntiDroneNetStateMessage message = new VehicleAntiDroneNetStateMessage(vehicle.getId(), SbwCompat.hasVehicleAntiDroneNetUpgrade(vehicle), SbwCompat.hasVehicleAntiDroneNetUpgrade(vehicle)&&Vc01Geometry.profile(vehicle)!=null?Vc01Panels.packet(vehicle):new net.minecraft.nbt.CompoundTag());
        double maxDistanceSqr = SYNC_RANGE * SYNC_RANGE;
        for (ServerPlayer player : serverLevel.players()) {
            if (Math.min(player.distanceToSqr(vehicle),DroneClientChunkSyncManager.trackingPosition(player).distanceToSqr(vehicle.position())) <= maxDistanceSqr) {
                Set<Integer> known = KNOWN.computeIfAbsent(player, p -> new HashSet<>());
                known.removeIf(id -> serverLevel.getEntity(id) == null);
                if (message.active()) {
                    known.add(vehicle.getId());
                    var hashes=HEALTH_HASH.computeIfAbsent(player,p->new java.util.HashMap<>());if(hashes.size()>512)hashes.clear();Integer oldHash=hashes.put(vehicle.getId(),message.panels().hashCode());
                    AddonNetwork.sendVehicleAntiDroneNetState(player,serverLevel.getGameTime()%40==0||oldHash==null||oldHash!=message.panels().hashCode()?message:new VehicleAntiDroneNetStateMessage(message.entityId(),true));
                } else if (known.remove(vehicle.getId())) {
                    var hashes=HEALTH_HASH.get(player);if(hashes!=null)hashes.remove(vehicle.getId());
                    AddonNetwork.sendVehicleAntiDroneNetState(player, message);
                }
            }
        }
    }
}
