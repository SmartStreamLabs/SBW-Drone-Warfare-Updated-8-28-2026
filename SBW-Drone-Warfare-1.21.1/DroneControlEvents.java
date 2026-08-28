package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public final class DroneControlEvents {
    private DroneControlEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID) || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        ItemStack stack = SbwCompat.getLinkedMonitor(player);
        if (!SbwCompat.isLinkedMonitor(stack)) {
            PlayerDroneAnchorManager.release(player, true);
            return;
        }

        if (player.tickCount % 40 == 0) {
            DroneTrackingRangeOverride.applyConfiguredRange();
        }

        Entity drone = SbwCompat.findLinkedDrone(player, stack);
        if (drone == null) {
            PlayerDroneAnchorManager.release(player, true);
            if (SbwCompat.isUsingLinkedMonitor(stack)) {
                SbwCompat.stopUsingMonitor(stack);
                player.displayClientMessage(
                        Component.translatable("message.sbwdroneconfig.drone_missing").withStyle(ChatFormatting.RED),
                        true
                );
            }
            return;
        }

        SbwCompat.ensureDroneOwner(drone, player);

        boolean usingMonitor = SbwCompat.isUsingLinkedMonitor(stack);
        if (usingMonitor && drone instanceof Geranium2Entity geraniumDrone && Geranium2FuelSystem.isOutOfFuel(geraniumDrone)) {
            SbwCompat.stopUsingMonitor(stack);
            SbwCompat.resetDroneInput(drone);
            Geranium2FuelSystem.forceFuelEmptyIdle(geraniumDrone);
            PlayerDroneAnchorManager.release(player, true);
            player.displayClientMessage(
                    Component.translatable("message.sbwdroneconfig.geranium_2_fuel_empty").withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        DroneLinkMode linkMode = FiberOpticLinkSystem.getLinkMode(drone);
        boolean anchorEnabled = AddonConfig.enableDronePlayerAnchor();
        boolean anchorActive = usingMonitor && anchorEnabled;

        if (usingMonitor && AddonConfig.enableBatterySystem()) {
            DroneBatterySystem.tickForActiveController(player, drone);
            DroneBatterySystem.syncForController(player, drone);
        }
        if (usingMonitor && AddonConfig.enableFiberOpticMode()) {
            FiberOpticLinkSystem.syncForController(player, drone);
        }

        if (usingMonitor && AddonConfig.enableDroneJammer()) {
            DroneJamStateManager.markControlled(player, drone);
            DroneJammerSystem.handleControllerFeedback(player, drone);
        }

        if (anchorActive) {
            PlayerDroneAnchorManager.touch(player, drone);
        } else {
            PlayerDroneAnchorManager.release(player, true);
        }

        if (!anchorActive && linkMode == DroneLinkMode.WIRELESS) {
            double distance = SbwCompat.distanceToLinkedDrone(player, drone);
            int effectiveRange = DroneWeatherEffects.getEffectiveControlRange(player, drone, AddonConfig.droneMaxRange());
            if (distance > effectiveRange) {
                if (SbwCompat.isUsingLinkedMonitor(stack)) {
                    SbwCompat.stopUsingMonitor(stack);
                    SbwCompat.resetDroneInput(drone);
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.sbwdroneconfig.drone_signal_lost",
                                    effectiveRange
                            ).withStyle(ChatFormatting.RED),
                            true
                    );
                }
                return;
            }
        }

        if (AddonConfig.enableDroneChunkLoading() && usingMonitor) {
            int chunkRadius = AddonConfig.droneChunkLoadRadius();
            ChunkTicketManager.touchDrone(drone, chunkRadius);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        Entity target = event.getTarget();
        if (target != null && SbwCompat.isDrone(target) && AddonConfig.enableFiberOpticMode()) {
            FiberOpticLinkSystem.syncForTrackingPlayer(player, target);
        }
    }

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        Entity target = event.getTarget();
        if (target != null && SbwCompat.isDrone(target) && AddonConfig.enableFiberOpticMode()) {
            FiberOpticLinkSystem.clearForTrackingPlayer(player, target);
        }
    }
}
