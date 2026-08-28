package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Water contact should break the FPV control link instead of going through the
 * heavier crash explosion route. This keeps the existing drone systems intact
 * while changing only the water-touch behavior.
 */
public final class DroneWaterDisconnectSystem {
    public static final String TAG_WATER_DISCONNECTED = "sbwdroneconfigWaterDisconnected";
    public static final String TAG_SKIP_WATER_DESTROY = "sbwdroneconfigSkipWaterDestroy";

    private DroneWaterDisconnectSystem() {
    }

    public static void onDroneBaseTick(Entity drone) {
        if (drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }

        if (!isTouchingWater(drone)) {
            drone.getPersistentData().putBoolean(TAG_WATER_DISCONNECTED, false);
            drone.getPersistentData().putBoolean(TAG_SKIP_WATER_DESTROY, false);
            return;
        }

        if (drone.getPersistentData().getBoolean(TAG_WATER_DISCONNECTED)) {
            return;
        }

        drone.getPersistentData().putBoolean(TAG_WATER_DISCONNECTED, true);
        drone.getPersistentData().putBoolean(TAG_SKIP_WATER_DESTROY, true);

        if (drone.level() instanceof ServerLevel serverLevel) {
            disconnectController(serverLevel, drone);
        }

        DroneSpotlightSystem.setSpotlightActive(drone, false, null);
        SbwCompat.resetDroneInput(drone);
        drone.setDeltaMovement(Vec3.ZERO);
        drone.hurtMarked = true;
    }

    public static boolean interceptWaterDestroy(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !SbwCompat.isDrone(drone) || !isTouchingWater(drone)) {
            return false;
        }

        return handleWaterContact(level, drone);
    }

    public static boolean shouldSkipDestroy(Entity drone) {
        return drone != null && drone.getPersistentData().getBoolean(TAG_SKIP_WATER_DESTROY);
    }

    /**
     * SBW applies water crash damage during the drone tick before the old tail
     * hook ran. Expose the disconnect path so mixins can intercept those water
     * branches early, before the drone enters the destroy/explosion route.
     */
    public static boolean handleWaterContact(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !SbwCompat.isDrone(drone) || !isTouchingWater(drone)) {
            return false;
        }

        forceControlDisconnect(level, drone);
        drone.getPersistentData().putBoolean(TAG_WATER_DISCONNECTED, true);
        drone.getPersistentData().putBoolean(TAG_SKIP_WATER_DESTROY, true);
        return true;
    }

    public static boolean forceControlDisconnect(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !SbwCompat.isDrone(drone)) {
            return false;
        }

        disconnectController(level, drone);
        DroneSpotlightSystem.setSpotlightActive(drone, false, null);
        SbwCompat.resetDroneInput(drone);
        drone.setDeltaMovement(Vec3.ZERO);
        drone.hurtMarked = true;
        return true;
    }

    public static boolean handleConfirmedWaterBranch(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !SbwCompat.isDrone(drone)) {
            return false;
        }

        forceControlDisconnect(level, drone);
        drone.getPersistentData().putBoolean(TAG_WATER_DISCONNECTED, true);
        drone.getPersistentData().putBoolean(TAG_SKIP_WATER_DESTROY, true);
        return forceDisconnect(level, drone, true);
    }

    public static boolean forceDisconnect(ServerLevel level, Entity drone, boolean skipDestroy) {
        if (!forceControlDisconnect(level, drone)) {
            return false;
        }
        drone.getPersistentData().putBoolean(TAG_WATER_DISCONNECTED, true);
        drone.getPersistentData().putBoolean(TAG_SKIP_WATER_DESTROY, skipDestroy);
        return true;
    }

    private static boolean isTouchingWater(Entity drone) {
        // Rain should not count as a water crash. Only real water / bubbles should
        // trigger the disconnect-instead-of-explosion path.
        if (drone.isInWaterOrBubble() || drone.isUnderWater()) {
            return true;
        }

        Level level = drone.level();
        if (level == null) {
            return false;
        }

        AABB waterCheckBox = drone.getBoundingBox()
                .inflate(0.25D, 0.15D, 0.25D)
                .expandTowards(0.0D, -0.45D, 0.0D);
        BlockPos min = BlockPos.containing(waterCheckBox.minX, waterCheckBox.minY, waterCheckBox.minZ);
        BlockPos max = BlockPos.containing(waterCheckBox.maxX, waterCheckBox.maxY, waterCheckBox.maxZ);

        for (BlockPos scanPos : BlockPos.betweenClosed(min, max)) {
            if (level.getFluidState(scanPos).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    private static void disconnectController(ServerLevel level, Entity drone) {
        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        if (controllerUuid != null) {
            ServerPlayer controller = level.getServer().getPlayerList().getPlayer(controllerUuid);
            if (controller != null) {
                PlayerDroneAnchorManager.release(controller, true);
            }
        }

        SbwCompat.disconnectControllersForDrone(level, drone);
    }
}
