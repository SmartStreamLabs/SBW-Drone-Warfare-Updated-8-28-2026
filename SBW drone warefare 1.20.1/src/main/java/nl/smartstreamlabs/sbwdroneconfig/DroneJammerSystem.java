package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class DroneJammerSystem {
    private static final String TAG_LAST_WARNING_TICK = "sbwdroneconfigLastJammerWarningTick";

    private DroneJammerSystem() {
    }

    public static void beforeDroneTravel(Entity drone) {
        if (drone == null || !SbwCompat.isDrone(drone) || drone.level().isClientSide()) {
            return;
        }
        if (FiberOpticLinkSystem.isImmuneToNormalJammers(drone)) {
            return;
        }

        float progress = DroneJamStateManager.getJamProgress(drone);
        if (progress < AddonConfig.controlInterferenceThreshold()) {
            return;
        }

        long gameTime = drone.level().getGameTime();
        float severity = normalizeToFull(progress, AddonConfig.controlInterferenceThreshold(), 1.0D);
        int resetEvery = Math.max(2, 7 - Math.round(severity * 5.0F));
        if (Math.floorMod(gameTime + drone.getId(), resetEvery) == 0) {
            SbwCompat.resetDroneInput(drone);
        }

        if (!drone.onGround()) {
            int descendEvery = Math.max(2, 11 - Math.round(severity * 8.0F));
            if (Math.floorMod(gameTime + (drone.getId() * 3L), descendEvery) == 0) {
                drone.getPersistentData().putBoolean("down", true);
            }
        }
    }

    public static boolean isJammed(Entity drone) {
        if (FiberOpticLinkSystem.isImmuneToNormalJammers(drone)) {
            return false;
        }
        return DroneJamStateManager.hasInterference(drone);
    }

    public static DroneJammerMode getJammerMode(Entity drone) {
        if (DroneJamStateManager.isHardJammed(drone)) {
            return DroneJammerMode.HARD;
        }
        return DroneJamStateManager.hasInterference(drone) ? DroneJammerMode.SOFT : null;
    }

    public static void handleControllerFeedback(ServerPlayer player, Entity drone) {
        if (player == null || drone == null || !isJammed(drone)) {
            return;
        }

        float progress = DroneJamStateManager.getJamProgress(drone);
        if (progress < AddonConfig.controlInterferenceThreshold()) {
            return;
        }

        long currentTick = player.serverLevel().getGameTime();
        var data = drone.getPersistentData();
        long lastWarningTick = data.getLong(TAG_LAST_WARNING_TICK);
        if (currentTick - lastWarningTick < 20L) {
            return;
        }

        boolean hardLockIncoming = progress >= AddonConfig.hardJamThreshold();
        player.displayClientMessage(
                Component.translatable(
                        hardLockIncoming
                                ? "message.sbwdroneconfig.drone_jammed_hard"
                                : "message.sbwdroneconfig.drone_jammed_soft"
                ).withStyle(hardLockIncoming ? ChatFormatting.RED : ChatFormatting.YELLOW),
                true
        );
        data.putLong(TAG_LAST_WARNING_TICK, currentTick);
    }

    private static float normalizeToFull(float progress, double floor, double ceiling) {
        if (ceiling <= floor) {
            return progress >= ceiling ? 1.0F : 0.0F;
        }

        float normalized = (float) ((progress - floor) / (ceiling - floor));
        return DroneJamProgressMath.clampProgress(normalized);
    }
}
