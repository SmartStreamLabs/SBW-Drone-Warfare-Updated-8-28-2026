package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class DroneSpotlightSystem {
    public static final String TAG_SPOTLIGHT_ACTIVE = "sbwdroneconfigSpotlightActive";
    private static final String TAG_SPOTLIGHT_ENERGY_BUFFER = "sbwdroneconfigSpotlightEnergyBuffer";

    private DroneSpotlightSystem() {
    }

    public static void onDroneBaseTick(Entity drone) {
        if (drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }

        if (!AddonConfig.enableSpotlightModule()) {
            DroneSpotlightLightSystem.clearForDrone(drone);
            return;
        }

        boolean active = isSpotlightActive(drone);
        ServerPlayer controller = getController(drone);

        if (active && !shouldKeepSpotlightActive(drone, controller)) {
            setSpotlightActive(drone, false, controller);
            return;
        }

        if (active) {
            // Real light stays server-authoritative and follows the drone only
            // while the existing spotlight state is valid.
            DroneSpotlightLightSystem.tick(drone);
        } else {
            DroneSpotlightLightSystem.clearForDrone(drone);
        }

        if (!active || !AddonConfig.enableBatterySystem()) {
            return;
        }

        double buffer = drone.getPersistentData().getDouble(TAG_SPOTLIGHT_ENERGY_BUFFER);
        buffer += AddonConfig.spotlightEnergyCostPerSecond() / 20.0D;
        int toConsume = (int) Math.floor(buffer);
        if (toConsume > 0) {
            int consumed = DroneBatterySystem.consumeEnergy(drone, toConsume);
            buffer -= consumed;

            if (consumed < toConsume && AddonConfig.spotlightAutoOffWhenBatteryEmpty()) {
                setSpotlightActive(drone, false, controller);
                buffer = 0.0D;
            }
        }
        drone.getPersistentData().putDouble(TAG_SPOTLIGHT_ENERGY_BUFFER, Math.max(0.0D, buffer));

        if (AddonConfig.spotlightAutoOffWhenBatteryEmpty() && DroneBatterySystem.isOutOfEnergyState(drone)) {
            setSpotlightActive(drone, false, controller);
        }
    }

    public static void toggleSpotlight(ServerPlayer player) {
        if (player == null || !AddonConfig.enableSpotlightModule()) {
            return;
        }

        Entity drone = SbwCompat.findActiveLinkedDrone(player);
        if (drone == null || !SbwCompat.isDrone(drone) || !SbwCompat.isUsingLinkedMonitorForDrone(player, drone)) {
            return;
        }

        if (AddonConfig.spotlightRequiresModule() && !DroneModuleSystem.hasSpotlightModule(drone)) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.spotlight_module_required").withStyle(ChatFormatting.RED), true);
            return;
        }

        if (AddonConfig.enableBatterySystem() && AddonConfig.spotlightAutoOffWhenBatteryEmpty() && DroneBatterySystem.isOutOfEnergyState(drone)) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.spotlight_no_power").withStyle(ChatFormatting.RED), true);
            return;
        }

        setSpotlightActive(drone, !isSpotlightActive(drone), player);
    }

    public static boolean isSpotlightActive(Entity drone) {
        return drone != null && drone.getPersistentData().getBoolean(TAG_SPOTLIGHT_ACTIVE);
    }

    public static void setSpotlightActive(Entity drone, boolean active, ServerPlayer controller) {
        if (drone == null || !SbwCompat.isDrone(drone)) {
            return;
        }

        drone.getPersistentData().putBoolean(TAG_SPOTLIGHT_ACTIVE, active);
        if (!active) {
            drone.getPersistentData().putDouble(TAG_SPOTLIGHT_ENERGY_BUFFER, 0.0D);
            DroneSpotlightLightSystem.clearForDrone(drone);
        }
        syncSpotlightState(drone, active, controller);
    }

    private static boolean shouldKeepSpotlightActive(Entity drone, ServerPlayer controller) {
        if (drone == null || !drone.isAlive() || drone.isRemoved()) {
            return false;
        }

        if (AddonConfig.spotlightRequiresModule() && !DroneModuleSystem.hasSpotlightModule(drone)) {
            return false;
        }

        return !AddonConfig.spotlightAutoOffWhenBatteryEmpty() || !DroneBatterySystem.isOutOfEnergyState(drone);
    }

    private static ServerPlayer getController(Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        var owner = SbwCompat.getDroneControllerUuid(drone);
        return owner == null ? null : serverLevel.getServer().getPlayerList().getPlayer(owner);
    }

    private static void syncSpotlightState(Entity drone, boolean active, ServerPlayer controller) {
        DroneSpotlightStateMessage message = new DroneSpotlightStateMessage(drone.getUUID(), active);
        if (AddonConfig.spotlightVisibleToOtherPlayers()) {
            AddonNetwork.syncDroneSpotlightState(message);
        } else if (controller != null) {
            AddonNetwork.sendDroneSpotlightState(controller, message);
        }
    }
}
