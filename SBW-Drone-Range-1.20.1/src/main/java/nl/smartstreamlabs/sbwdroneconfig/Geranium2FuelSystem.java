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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public final class Geranium2FuelSystem {
    private static final String TAG_LOW_WARNING_SENT = "sbwdroneconfigGeranium2LowFuelWarningSent";
    private static final String TAG_EMPTY_WARNING_SENT = "sbwdroneconfigGeranium2EmptyFuelWarningSent";

    private Geranium2FuelSystem() {
    }

    public static InteractionResult refuel(Geranium2Entity drone, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(AddonItems.GASOLINE_JERRYCAN.get())) {
            return InteractionResult.PASS;
        }

        if (drone.level().isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        if (!drone.onGround()) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.geranium_2_refuel_ground_only"), true);
            return InteractionResult.sidedSuccess(false);
        }

        int currentFuel = Math.round(drone.getGeraniumFuel());
        int maxFuel = AddonConfig.lucasMaxFuel();
        int missingFuel = Math.max(0, maxFuel - currentFuel);
        if (missingFuel <= 0) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.geranium_2_fuel_full"), true);
            return InteractionResult.sidedSuccess(false);
        }

        int transferredFuel = Math.min(AddonConfig.gasolineJerrycanFuelAmount(), missingFuel);
        drone.setGeraniumFuel(currentFuel + transferredFuel);
        drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
        drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);

        if (!player.getAbilities().instabuild) {
            replaceConsumedJerrycan(player, hand, stack);
        }

        player.displayClientMessage(
                Component.translatable(
                        "message.sbwdroneconfig.geranium_2_refueled",
                        transferredFuel,
                        Math.round(drone.getGeraniumFuel()),
                        maxFuel
                ).withStyle(ChatFormatting.GREEN),
                true
        );
        return InteractionResult.sidedSuccess(false);
    }

    public static void beforeGeraniumTravel(Geranium2Entity drone) {
        if (drone == null || drone.level().isClientSide()) {
            return;
        }

        clampFuelToConfig(drone);
        ServerPlayer controller = getController(drone);
        boolean activelyControlled = controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, drone);
        boolean engineActive = activelyControlled || (!drone.onGround() && drone.getDeltaMovement().lengthSqr() > 1.0E-4D);

        if (engineActive && drone.getGeraniumFuel() > 0.0F) {
            double drain = drone.onGround() ? AddonConfig.lucasFuelDrainIdle() : AddonConfig.lucasFuelDrainFlying();
            if (activelyControlled && (drone.upInputDown() || drone.hasGlideBombTakeoffAssist())) {
                drain += AddonConfig.lucasFuelDrainClimb();
            }
            drainFuel(drone, drain);
        }

        boolean emptyFuel = isOutOfFuel(drone);
        boolean lowFuel = isLowFuel(drone);
        if (activelyControlled) {
            if (lowFuel && !drone.getPersistentData().getBoolean(TAG_LOW_WARNING_SENT)) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.geranium_2_fuel_low").withStyle(ChatFormatting.GOLD), true);
                drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, true);
            } else if (!lowFuel) {
                drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);
            }

            if (emptyFuel && !drone.getPersistentData().getBoolean(TAG_EMPTY_WARNING_SENT)) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.geranium_2_fuel_empty").withStyle(ChatFormatting.RED), true);
                drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, true);
            } else if (!emptyFuel) {
                drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
            }
        } else {
            drone.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);
            drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
        }

        if (emptyFuel) {
            forceFuelEmptyIdle(drone);
        }
    }

    public static void forceFuelEmptyIdle(Geranium2Entity drone) {
        if (drone == null) {
            return;
        }

        drone.setGlideBombThrottle(0.0F);
        drone.setGlideBombAirspeed(0.0F);
        drone.setGlideBombStalling(false);
        drone.endGlideBombTakeoffAssist();
        drone.getEntityData().set(VehicleEntity.POWER, 0.0F);

        Vec3 motion = drone.getDeltaMovement();
        if (drone.onGround()) {
            drone.setDeltaMovement(Vec3.ZERO);
        } else {
            drone.setDeltaMovement(motion.x * 0.72D, Math.max(motion.y - 0.055D, -0.72D), motion.z * 0.72D);
        }
        drone.hurtMarked = true;
    }

    public static boolean canUsePoweredFlight(Geranium2Entity drone) {
        return drone != null && drone.getGeraniumFuel() > 0.0F;
    }

    public static boolean isOutOfFuel(Geranium2Entity drone) {
        return drone != null && drone.getGeraniumFuel() <= 0.0F;
    }

    private static void drainFuel(Geranium2Entity drone, double amount) {
        if (amount <= 0.0D) {
            return;
        }
        drone.setGeraniumFuel((float) Math.max(0.0D, drone.getGeraniumFuel() - amount));
    }

    private static void clampFuelToConfig(Geranium2Entity drone) {
        drone.setGeraniumFuel(Mth.clamp(drone.getGeraniumFuel(), 0.0F, (float) AddonConfig.lucasMaxFuel()));
    }

    private static boolean isLowFuel(Geranium2Entity drone) {
        int maxFuel = AddonConfig.lucasMaxFuel();
        int threshold = AddonConfig.lucasLowFuelWarningPercent();
        return maxFuel > 0
                && threshold > 0
                && drone.getGeraniumFuel() > 0.0F
                && drone.getGeraniumFuelPercent() <= threshold;
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

    private static ServerPlayer getController(Geranium2Entity drone) {
        Entity controller = drone.getController();
        return controller instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }
}
