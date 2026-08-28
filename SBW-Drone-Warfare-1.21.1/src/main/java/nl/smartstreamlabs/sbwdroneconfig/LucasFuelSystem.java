package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public final class LucasFuelSystem {
    private static final String TAG_LOW_WARNING_SENT = "sbwdroneconfigLucasLowFuelWarningSent";
    private static final String TAG_EMPTY_WARNING_SENT = "sbwdroneconfigLucasEmptyFuelWarningSent";
    private static final String TAG_LAST_SYNC_FUEL = "sbwdroneconfigLucasLastSyncFuel";
    private static final String TAG_LAST_SYNC_LOW = "sbwdroneconfigLucasLastSyncLow";
    private static final String TAG_LAST_SYNC_EMPTY = "sbwdroneconfigLucasLastSyncEmpty";

    private LucasFuelSystem() {
    }

    public static InteractionResult refuel(LucasDroneEntity drone, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(AddonItems.GASOLINE_JERRYCAN.get())) {
            return InteractionResult.PASS;
        }

        if (drone.level().isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        if (!drone.onGround()) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.lucas_refuel_ground_only"), true);
            return InteractionResult.sidedSuccess(false);
        }

        int currentFuel = Math.round(drone.getLucasFuel());
        int maxFuel = AddonConfig.lucasMaxFuel();
        int missingFuel = Math.max(0, maxFuel - currentFuel);
        if (missingFuel <= 0) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.lucas_fuel_full"), true);
            return InteractionResult.sidedSuccess(false);
        }

        int transferredFuel = Math.min(AddonConfig.gasolineJerrycanFuelAmount(), missingFuel);
        drone.setLucasFuel(currentFuel + transferredFuel);
        drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
        drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);

        if (!player.getAbilities().instabuild) {
            replaceConsumedJerrycan(player, hand, stack);
        }

        player.displayClientMessage(
                Component.translatable(
                        "message.sbwdroneconfig.lucas_refueled",
                        transferredFuel,
                        Math.round(drone.getLucasFuel()),
                        maxFuel
                ).withStyle(ChatFormatting.GREEN),
                true
        );

        syncIfNeeded(drone, player instanceof ServerPlayer serverPlayer ? serverPlayer : null, true);
        return InteractionResult.sidedSuccess(false);
    }

    public static void beforeLucasTravel(LucasDroneEntity drone) {
        if (drone == null || drone.level().isClientSide()) {
            return;
        }

        clampFuelToConfig(drone);
        ServerPlayer controller = getController(drone);
        boolean activelyControlled = controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, drone);
        boolean engineActive = activelyControlled || (!drone.onGround() && drone.getDeltaMovement().lengthSqr() > 1.0E-4D);

        if (engineActive && drone.getLucasFuel() > 0.0F) {
            double drain = drone.onGround() ? AddonConfig.lucasFuelDrainIdle() : AddonConfig.lucasFuelDrainFlying();
            if (activelyControlled && (drone.upInputDown() || drone.hasLucasTakeoffAssist())) {
                drain += AddonConfig.lucasFuelDrainClimb();
            }
            drainFuel(drone, drain);
        }

        boolean emptyFuel = isOutOfFuel(drone);
        boolean lowFuel = isLowFuel(drone);
        if (activelyControlled) {
            if (lowFuel && !drone.getPersistentData().getBoolean(TAG_LOW_WARNING_SENT)) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.lucas_fuel_low").withStyle(ChatFormatting.GOLD), true);
                drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, true);
            } else if (!lowFuel) {
                drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);
            }

            if (emptyFuel && !drone.getPersistentData().getBoolean(TAG_EMPTY_WARNING_SENT)) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.lucas_fuel_empty").withStyle(ChatFormatting.RED), true);
                drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, true);
            } else if (!emptyFuel) {
                drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
            }
        } else {
            drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);
            drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
        }

        if (emptyFuel && !AddonConfig.lucasCanFlyWithoutFuel()) {
            drone.setLucasThrottle(0.0F);
            drone.endLucasTakeoffAssist();
            drone.getEntityData().set(VehicleEntity.POWER, 0.0F);
        }

        syncIfNeeded(drone, controller, false);
    }

    public static boolean canUsePoweredFlight(LucasDroneEntity drone) {
        return drone != null && (AddonConfig.lucasCanFlyWithoutFuel() || drone.getLucasFuel() > 0.0F);
    }

    public static boolean isOutOfFuel(LucasDroneEntity drone) {
        return drone != null && !AddonConfig.lucasCanFlyWithoutFuel() && drone.getLucasFuel() <= 0.0F;
    }

    private static void drainFuel(LucasDroneEntity drone, double amount) {
        if (amount <= 0.0D) {
            return;
        }
        drone.setLucasFuel((float) Math.max(0.0D, drone.getLucasFuel() - amount));
    }

    private static void clampFuelToConfig(LucasDroneEntity drone) {
        drone.setLucasFuel(Mth.clamp(drone.getLucasFuel(), 0.0F, (float) AddonConfig.lucasMaxFuel()));
    }

    private static boolean isLowFuel(LucasDroneEntity drone) {
        int maxFuel = AddonConfig.lucasMaxFuel();
        int threshold = AddonConfig.lucasLowFuelWarningPercent();
        return maxFuel > 0
                && threshold > 0
                && drone.getLucasFuel() > 0.0F
                && drone.getLucasFuelPercent() <= threshold;
    }

    private static void replaceConsumedJerrycan(Player player, InteractionHand hand, ItemStack stack) {
        ItemStack emptyJerrycan = new ItemStack(AddonItems.EMPTY_JERRYCAN.get());
        if (stack.getCount() == 1) {
            player.setItemInHand(hand, emptyJerrycan);
            return;
        }

        stack.shrink(1);
        ItemHandlerHelper.giveItemToPlayer(player, emptyJerrycan);
    }

    private static ServerPlayer getController(LucasDroneEntity drone) {
        Entity controller = drone.getController();
        return controller instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    private static void syncIfNeeded(LucasDroneEntity drone, ServerPlayer controller, boolean force) {
        if (controller == null) {
            return;
        }

        int currentFuel = Math.round(drone.getLucasFuel());
        int maxFuel = AddonConfig.lucasMaxFuel();
        boolean lowFuel = isLowFuel(drone);
        boolean emptyFuel = isOutOfFuel(drone);
        var data = drone.getPersistentData();

        if (!force
                && data.getInt(TAG_LAST_SYNC_FUEL) == currentFuel
                && data.getBoolean(TAG_LAST_SYNC_LOW) == lowFuel
                && data.getBoolean(TAG_LAST_SYNC_EMPTY) == emptyFuel
                && drone.tickCount % 10 != 0) {
            return;
        }

        data.putInt(TAG_LAST_SYNC_FUEL, currentFuel);
        data.putBoolean(TAG_LAST_SYNC_LOW, lowFuel);
        data.putBoolean(TAG_LAST_SYNC_EMPTY, emptyFuel);
        AddonNetwork.sendLucasFuel(controller, new LucasFuelSyncMessage(drone.getUUID(), currentFuel, maxFuel, emptyFuel, lowFuel));
    }
}
