package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class DroneTrackingHooks {
    private static final int TRACKING_BUFFER_BLOCKS = 64;

    private DroneTrackingHooks() {
    }

    public static boolean shouldKeepDroneTrackedTo(ServerPlayer player, Entity trackedEntity) {
        return getForcedTrackingRangeBlocks(player, trackedEntity) > 0;
    }

    public static int getForcedTrackingRangeBlocks(ServerPlayer player, Entity trackedEntity) {
        if (!isTrackableDroneForPlayer(player, trackedEntity)) {
            return 0;
        }

        if (SbwCompat.isUsingLinkedMonitorForDrone(player, trackedEntity)) {
            return getDesiredTrackingRangeBlocks();
        }

        if (isWithinTrackingRange(player, trackedEntity, getNearbyPlayerTrackingRangeBlocks())) {
            return getNearbyPlayerTrackingRangeBlocks();
        }

        return 0;
    }

    public static int getDesiredTrackingRangeBlocks() {
        return AddonConfig.droneMaxRange() + TRACKING_BUFFER_BLOCKS;
    }

    public static int getNearbyPlayerTrackingRangeBlocks() {
        return Math.max(TRACKING_BUFFER_BLOCKS, Math.min(getDesiredTrackingRangeBlocks(), AddonConfig.droneVisualTrackingRange()));
    }

    private static boolean isTrackableDroneForPlayer(ServerPlayer player, Entity trackedEntity) {
        if (player == null || trackedEntity == null || !trackedEntity.isAlive() || trackedEntity.isRemoved()) {
            return false;
        }

        if (player.level() != trackedEntity.level()) {
            return false;
        }

        return SbwCompat.isDrone(trackedEntity);
    }

    private static boolean isWithinTrackingRange(ServerPlayer player, Entity trackedEntity, int rangeBlocks) {
        double range = rangeBlocks;
        return player.distanceToSqr(trackedEntity) <= range * range;
    }

    public static boolean shouldSuppressClientMonitorDisconnect(ItemStack stack, Level level, Entity holder, boolean selected) {
        if (stack.isEmpty() || level == null || holder == null || !level.isClientSide() || !selected) {
            return false;
        }

        if (!SbwCompat.isUsingLinkedMonitor(stack) || !AddonStackTags.get(stack).contains(SbwCompat.TAG_LINKED_DRONE)) {
            return false;
        }

        return holder instanceof Player;
    }
}
