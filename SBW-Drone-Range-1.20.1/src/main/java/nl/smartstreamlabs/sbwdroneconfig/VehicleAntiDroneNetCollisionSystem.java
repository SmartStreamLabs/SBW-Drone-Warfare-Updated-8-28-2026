package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class VehicleAntiDroneNetCollisionSystem {
    private static final double MAX_SCAN_RADIUS = 48.0D;

    private VehicleAntiDroneNetCollisionSystem() {
    }

    public static boolean tryJamVehicleNet(ServerLevel serverLevel, Entity drone) {
        if (serverLevel == null
                || drone == null
                || !AddonConfig.vehicleAntiDroneNetEnabled()
                || !AddonConfig.vehicleAntiDroneNetStopsDrones()
                || !SbwCompat.isDrone(drone)) {
            return false;
        }

        Vec3 current = drone.position();
        Vec3 previous = current.subtract(drone.getDeltaMovement());
        AABB searchBox = drone.getBoundingBox().inflate(MAX_SCAN_RADIUS);
        List<Entity> vehicles = serverLevel.getEntities((Entity) null, searchBox, SbwCompat::hasVehicleAntiDroneNetUpgrade);

        for (Entity vehicle : vehicles) {
            VehicleAntiDroneNetGeometry.NetBox netBox = VehicleAntiDroneNetGeometry.createBox(
                    vehicle,
                    AddonConfig.vehicleAntiDroneNetHorizontalPadding(),
                    AddonConfig.vehicleAntiDroneNetTopPadding()
            );
            double vehicleNetRadius = VehicleAntiDroneNetGeometry.searchRadius(netBox);
            if (vehicle.distanceToSqr(drone) > vehicleNetRadius * vehicleNetRadius) {
                continue;
            }

            VehicleAntiDroneNetGeometry.Basis basis = VehicleAntiDroneNetGeometry.basis(vehicle, 1.0F);
            VehicleAntiDroneNetGeometry.LocalPoint previousLocal = basis.toLocal(previous);
            VehicleAntiDroneNetGeometry.LocalPoint currentLocal = basis.toLocal(current);
            double droneRadius = Math.max(0.12D, Math.max(drone.getBbWidth(), drone.getBbHeight()) * 0.5D);
            if (VehicleAntiDroneNetGeometry.intersectsSweptShell(netBox, previousLocal, currentLocal, droneRadius)) {
                return AntiDroneNetEffectSystem.jamDrone(
                        serverLevel,
                        drone,
                        current,
                        "vehicle " + vehicle.getStringUUID()
                );
            }
        }

        return false;
    }
}
