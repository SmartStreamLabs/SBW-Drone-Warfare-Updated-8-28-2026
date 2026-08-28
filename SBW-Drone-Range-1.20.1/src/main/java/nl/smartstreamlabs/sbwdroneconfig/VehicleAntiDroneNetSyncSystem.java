package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.Set;

public final class VehicleAntiDroneNetSyncSystem {
    private static final int SYNC_INTERVAL_TICKS = 10;
    private static final double SYNC_RANGE = 192.0D;

    private VehicleAntiDroneNetSyncSystem() {
    }

    @SubscribeEvent
    public static void onServerLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()
                || !(event.getLevel() instanceof ServerLevel serverLevel)
                || !AddonConfig.vehicleAntiDroneNetEnabled()
                || serverLevel.getGameTime() % SYNC_INTERVAL_TICKS != 0L) {
            return;
        }

        syncVehiclesNearPlayers(serverLevel);
    }

    private static void syncVehiclesNearPlayers(ServerLevel serverLevel) {
        Set<Integer> syncedEntityIds = new HashSet<>();
        for (ServerPlayer player : serverLevel.players()) {
            AABB searchBox = player.getBoundingBox().inflate(SYNC_RANGE);
            for (Entity vehicle : serverLevel.getEntities((Entity) null, searchBox, SbwCompat::hasVehicleAntiDroneNetUpgrade)) {
                if (syncedEntityIds.add(vehicle.getId())) {
                    syncVehicleToNearbyPlayers(serverLevel, vehicle);
                }
            }
        }
    }

    private static void syncVehicleToNearbyPlayers(ServerLevel serverLevel, Entity vehicle) {
        VehicleAntiDroneNetStateMessage message = new VehicleAntiDroneNetStateMessage(vehicle.getId(), true);
        double maxDistanceSqr = SYNC_RANGE * SYNC_RANGE;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(vehicle) <= maxDistanceSqr) {
                AddonNetwork.sendVehicleAntiDroneNetState(player, message);
            }
        }
    }
}
