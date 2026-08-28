package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class DroneSpeedSystem {
    private static final double DRONE_THRUST_SCALE = 0.017D;

    private DroneSpeedSystem() {
    }

    public static void afterDroneTravel(Entity entity) {
        if (!(entity instanceof DroneEntity drone) || !drone.isAlive()) {
            return;
        }

        double configuredMultiplier = AddonConfig.droneSpeedMultiplier();
        if (configuredMultiplier <= DroneSpeedConfigLimits.MIN_DRONE_SPEED_MULTIPLIER) {
            return;
        }

        if (!hasAnyFlightInput(drone)) {
            return;
        }

        double boostMultiplier = configuredMultiplier - 1.0D;
        Vec3 extraMotion = Vec3.ZERO;

        Vector3f direction = drone.getRightDirection().mul(drone.getEntityData().get(VehicleEntity.DELTA_ROT));
        extraMotion = extraMotion.add(new Vec3(direction.x, direction.y, direction.z).scale(DRONE_THRUST_SCALE * boostMultiplier));

        Vector3f directionZ = drone.getForwardDirection().mul(-drone.getEntityData().get(DroneEntity.DELTA_X_ROT));
        extraMotion = extraMotion.add(new Vec3(directionZ.x, directionZ.y, directionZ.z).scale(DRONE_THRUST_SCALE * boostMultiplier));

        if (drone.upInputDown() || drone.downInputDown()) {
            extraMotion = extraMotion.add(0.0D, drone.getEntityData().get(VehicleEntity.POWER) * 0.6D * boostMultiplier, 0.0D);
        }

        if (extraMotion.lengthSqr() > 0.0D) {
            drone.setDeltaMovement(drone.getDeltaMovement().add(extraMotion));
        }
    }

    private static boolean hasAnyFlightInput(DroneEntity drone) {
        return drone.forwardInputDown()
                || drone.backInputDown()
                || drone.leftInputDown()
                || drone.rightInputDown()
                || drone.upInputDown()
                || drone.downInputDown();
    }
}
