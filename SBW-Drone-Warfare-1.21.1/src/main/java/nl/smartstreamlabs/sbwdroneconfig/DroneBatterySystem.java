package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class DroneBatterySystem {
    private static final String TAG_CURRENT_ENERGY = "sbwdroneconfigCurrentEnergy";
    private static final String TAG_MAX_ENERGY = "sbwdroneconfigMaxEnergy";
    private static final String TAG_OUT_OF_ENERGY = "sbwdroneconfigOutOfEnergy";
    private static final String TAG_LOW_WARNING_SENT = "sbwdroneconfigLowWarningSent";
    private static final String TAG_EMPTY_WARNING_SENT = "sbwdroneconfigEmptyWarningSent";
    private static final String TAG_LAST_SYNC_ENERGY = "sbwdroneconfigLastSyncEnergy";
    private static final String TAG_LAST_SYNC_LOW = "sbwdroneconfigLastSyncLow";
    private static final String TAG_LAST_SYNC_EMPTY = "sbwdroneconfigLastSyncEmpty";
    private static final String TAG_LAST_ENERGY_PROCESS_TICK = "sbwdroneconfigLastEnergyProcessTick";

    private DroneBatterySystem() {
    }

    public static void onDroneBaseTick(Entity entity) {if(PlatformInventory.editingDrone(entity))return;if(entity instanceof Gad01Entity||entity instanceof Fl01Entity)return;
        if(Dc01Docking.docked(entity)||entity instanceof Hx2Entity h&&h.mount()!=null)return;
        if (!AddonConfig.enableBatterySystem() || entity == null || entity.level().isClientSide() || !SbwCompat.isDrone(entity)) {
            return;
        }

        long gameTime = entity.level().getGameTime();
        if (entity.getPersistentData().getLong(TAG_LAST_ENERGY_PROCESS_TICK) == gameTime) {
            return;
        }
        entity.getPersistentData().putLong(TAG_LAST_ENERGY_PROCESS_TICK, gameTime);

        initializeEnergy(entity);
        rechargeFromInventoryBatteries(entity);

        int currentEnergy = getCurrentEnergy(entity);
        boolean active = isActivelyUsingEnergy(entity);
        int energyUse = entity instanceof Hx2Entity h?(h.motor()?Hx2Config.USE.get():0):active ? AddonConfig.energyUsePerTick() : AddonConfig.idleEnergyUsePerTick();
        energyUse += DroneWeatherEffects.getAdditionalEnergyUse(entity, active);
        energyUse = FiberOpticLinkSystem.applyExtraBatteryDrain(entity, energyUse, active);

        if (energyUse > 0 && currentEnergy > 0) {
            currentEnergy = Math.max(0, currentEnergy - energyUse);
            setCurrentEnergy(entity, currentEnergy);
        }

        boolean outOfEnergy = currentEnergy <= 0;
        setOutOfEnergy(entity, outOfEnergy);

        ServerPlayer controller = getController(entity);
        boolean lowEnergy = currentEnergy > 0 && currentEnergy <= AddonConfig.lowEnergyThreshold() && AddonConfig.lowEnergyThreshold() > 0;

        if (controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, entity)) {
            if (lowEnergy && !entity.getPersistentData().getBoolean(TAG_LOW_WARNING_SENT)) {
                controller.displayClientMessage(
                        Component.translatable("message.sbwdroneconfig.drone_low_energy", currentEnergy, getMaxEnergy(entity))
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
                entity.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, true);
            } else if (!lowEnergy) {
                entity.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);
            }

            if (outOfEnergy && !entity.getPersistentData().getBoolean(TAG_EMPTY_WARNING_SENT)) {
                controller.displayClientMessage(
                        Component.translatable("message.sbwdroneconfig.drone_energy_empty").withStyle(ChatFormatting.RED),
                        true
                );
                entity.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, true);
                SbwCompat.stopUsingLinkedMonitorForDrone(controller, entity);
                SbwCompat.resetDroneInput(entity);
            } else if (!outOfEnergy) {
                entity.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
            }
        } else {
            entity.getPersistentData().putBoolean(TAG_LOW_WARNING_SENT, false);
            entity.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
        }

        syncIfNeeded(entity, controller, currentEnergy, lowEnergy, outOfEnergy);
    }

    public static void tickForActiveController(ServerPlayer player, Entity drone) {
        if (!AddonConfig.enableBatterySystem() || player == null || drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }
        onDroneBaseTick(drone);
    }

    public static void beforeDroneTravel(Entity entity) {
        if (!AddonConfig.enableBatterySystem() || entity == null || entity.level().isClientSide() || !SbwCompat.isDrone(entity)) {
            return;
        }

        initializeEnergy(entity);
        if (!isOutOfEnergy(entity)) {
            return;
        }

        invokeBooleanSetter(entity, "setLeftInputDown", false);
        invokeBooleanSetter(entity, "setRightInputDown", false);
        invokeBooleanSetter(entity, "setForwardInputDown", false);
        invokeBooleanSetter(entity, "setBackInputDown", false);
        invokeBooleanSetter(entity, "setUpInputDown", false);
        invokeBooleanSetter(entity, "setFireInputDown", false);
        invokeBooleanSetter(entity, "setDecoyInputDown", false);
        invokeBooleanSetter(entity, "setSprintInputDown", false);
        invokeFloatSetter(entity, "setMouseMoveSpeedX", 0.0F);
        invokeFloatSetter(entity, "setMouseMoveSpeedY", 0.0F);

        if (entity.onGround()) {
            invokeBooleanSetter(entity, "setDownInputDown", false);
            invokeFloatSetter(entity, "setPower", 0.0F);
            return;
        }

        invokeBooleanSetter(entity, "setDownInputDown", true);
        if (AddonConfig.returnHomeOnEmpty()) {
            Entity controller = getControllerEntity(entity);
            if (controller != null) {
                Vec3 toController = controller.position().subtract(entity.position());
                Vec3 horizontal = new Vec3(toController.x, 0.0D, toController.z);
                if (horizontal.lengthSqr() > 1.0E-4D) {
                    Vec3 nudged = entity.getDeltaMovement().add(horizontal.normalize().scale(0.015D));
                    entity.setDeltaMovement(nudged.x, Math.min(nudged.y, -0.02D), nudged.z);
                }
            }
        }
    }

    public static void syncForController(ServerPlayer player, Entity drone) {
        if (!AddonConfig.enableBatterySystem() || player == null || drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }

        initializeEnergy(drone);
        int currentEnergy = getCurrentEnergy(drone);
        boolean outOfEnergy = isOutOfEnergy(drone);
        boolean lowEnergy = currentEnergy > 0 && currentEnergy <= AddonConfig.lowEnergyThreshold() && AddonConfig.lowEnergyThreshold() > 0;
        syncIfNeeded(drone, player, currentEnergy, lowEnergy, outOfEnergy);
    }

    public static int currentEnergy(Entity drone) {if(drone instanceof Sd01Entity d)return d.getEnergy();if(drone instanceof Gad01Entity g)return g.getEnergy();
        if (drone == null || !SbwCompat.isDrone(drone)) {
            return 0;
        }
        initializeEnergy(drone);
        return getCurrentEnergy(drone);
    }

    public static boolean isOutOfEnergyState(Entity drone) {if(drone instanceof Sd01Entity d)return d.getEnergy()<=0;if(drone instanceof Gad01Entity g)return g.getEnergy()<=0;
        if (drone == null || !SbwCompat.isDrone(drone)) {
            return true;
        }
        initializeEnergy(drone);
        return isOutOfEnergy(drone);
    }

    public static int consumeEnergy(Entity drone, int amount) {
        if(drone instanceof Sd01Entity d){int n=Math.max(0,Math.min(amount,d.getEnergy()));d.consumeEnergy(n);return n;}
        if (drone == null || !SbwCompat.isDrone(drone) || amount <= 0) {
            return 0;
        }

        initializeEnergy(drone);
        int currentEnergy = getCurrentEnergy(drone);
        int consumed = Math.min(currentEnergy, amount);
        setCurrentEnergy(drone, currentEnergy - consumed);
        setOutOfEnergy(drone, getCurrentEnergy(drone) <= 0);
        return consumed;
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if(event.getTarget() instanceof Sd01Entity||event.getTarget() instanceof Gad01Entity)return;handleRecharge(event.getEntity(), event.getTarget(), event.getHand(), event);
    }

    private static void handleRecharge(Player player, Entity target, InteractionHand hand, PlayerInteractEvent.EntityInteract event) {
        if (!AddonConfig.enableBatterySystem() || target.level().isClientSide() || !SbwCompat.isDrone(target)) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!SbwCompat.isSbwBattery(stack)) {
            return;
        }

        initializeEnergy(target);
        int currentEnergy = getCurrentEnergy(target);
        int maxEnergy = getMaxEnergy(target);
        int missing = maxEnergy - currentEnergy;
        if (missing <= 0) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.drone_recharge_full"), true);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(false));
            return;
        }

        int transferred = extractEnergyFromBattery(stack, missing);

        if (transferred <= 0) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.drone_recharge_empty_battery"), true);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(false));
            return;
        }

        setCurrentEnergy(target, currentEnergy + transferred);
        setOutOfEnergy(target, false);
        target.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
        player.displayClientMessage(
                Component.translatable(
                        "message.sbwdroneconfig.drone_recharged",
                        transferred,
                        getCurrentEnergy(target),
                        getMaxEnergy(target)
                ).withStyle(ChatFormatting.GREEN),
                true
        );

        if (player instanceof ServerPlayer serverPlayer) {
            syncIfNeeded(target, serverPlayer, getCurrentEnergy(target), getCurrentEnergy(target) <= AddonConfig.lowEnergyThreshold(), false, true);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(false));
    }

    private static boolean isActivelyUsingEnergy(Entity drone) {
        if (isControlled(drone)) {
            return true;
        }

        if (!drone.onGround()) {
            return true;
        }

        if (invokeBooleanGetter(drone, "forwardInputDown") || invokeBooleanGetter(drone, "backInputDown")
                || invokeBooleanGetter(drone, "leftInputDown") || invokeBooleanGetter(drone, "rightInputDown")
                || invokeBooleanGetter(drone, "upInputDown") || invokeBooleanGetter(drone, "downInputDown")
                || invokeBooleanGetter(drone, "fireInputDown")) {
            return true;
        }

        Vec3 movement = drone.getDeltaMovement();
        return movement.horizontalDistanceSqr() > 1.0E-4D || Math.abs(movement.y) > 0.03D;
    }

    /**
     * Upgrades the existing drone battery system so the drone can recharge itself from
     * SBW battery items placed inside the drone inventory. This reuses the same energy
     * capability extraction path as manual right-click charging instead of inventing a
     * separate fake battery store.
     */
    private static void rechargeFromInventoryBatteries(Entity drone) {
        int currentEnergy = getCurrentEnergy(drone);
        int maxEnergy = getMaxEnergy(drone);
        int missing = maxEnergy - currentEnergy;
        if (missing <= 0) {
            return;
        }

        rechargeFromInventoryBatteries(drone,new DroneInventoryContainer(drone));
    }

    public static void prepareOnPlatform(Entity drone,DroneInventoryContainer inventory){
        if(!AddonConfig.enableBatterySystem()||drone instanceof Fl01Entity)return;
        initializeEnergy(drone);rechargeFromInventoryBatteries(drone,inventory);
    }

    private static void rechargeFromInventoryBatteries(Entity drone,DroneInventoryContainer container){
        int currentEnergy=getCurrentEnergy(drone),maxEnergy=getMaxEnergy(drone),missing=maxEnergy-currentEnergy;
        if(missing<=0)return;
        boolean changed = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!SbwCompat.isSbwBattery(stack)) {
                continue;
            }

            int transferred = extractEnergyFromBattery(stack, missing);

            if (transferred <= 0) {
                continue;
            }

            setCurrentEnergy(drone, currentEnergy + transferred);
            setOutOfEnergy(drone, false);
            drone.getPersistentData().putBoolean(TAG_EMPTY_WARNING_SENT, false);
            changed = true;
            break;
        }

        if (changed) {
            container.saveToDrone();
        }
    }

    private static int extractEnergyFromBattery(ItemStack stack, int missingEnergy) {
        var storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (storage == null) {
            return 0;
        }

        int available = storage.getEnergyStored();
        int requested = Math.min(Math.min(available, missingEnergy), AddonConfig.batteryTransferRate());
        return requested <= 0 ? 0 : storage.extractEnergy(requested, false);
    }

    private static boolean isControlled(Entity drone) {
        ServerPlayer controller = getController(drone);
        return controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, drone);
    }

    private static void initializeEnergy(Entity drone) {
        var data = drone.getPersistentData();
        int configuredMax = drone instanceof Hx2Entity?Hx2Config.CAPACITY.get():AddonConfig.maxEnergy();
        int storedMax = data.getInt(TAG_MAX_ENERGY);
        int currentEnergy = data.contains(TAG_CURRENT_ENERGY) ? data.getInt(TAG_CURRENT_ENERGY) : configuredMax;

        if (storedMax <= 0) {
            storedMax = configuredMax;
        }

        currentEnergy = Math.min(currentEnergy, configuredMax);
        currentEnergy = Math.max(0, currentEnergy);

        data.putInt(TAG_MAX_ENERGY, configuredMax);
        data.putInt(TAG_CURRENT_ENERGY, currentEnergy);
        data.putBoolean(TAG_OUT_OF_ENERGY, currentEnergy <= 0);
    }

    private static int getCurrentEnergy(Entity drone) {
        return drone.getPersistentData().getInt(TAG_CURRENT_ENERGY);
    }

    private static void setCurrentEnergy(Entity drone, int value) {
        drone.getPersistentData().putInt(TAG_CURRENT_ENERGY, Math.max(0, Math.min(value, getMaxEnergy(drone))));
    }

    private static int getMaxEnergy(Entity drone) {
        int max = drone.getPersistentData().getInt(TAG_MAX_ENERGY);
        return max > 0 ? max : AddonConfig.maxEnergy();
    }

    private static boolean isOutOfEnergy(Entity drone) {
        if(drone instanceof Fl01Entity)return false;
        return drone.getPersistentData().getBoolean(TAG_OUT_OF_ENERGY);
    }

    private static void setOutOfEnergy(Entity drone, boolean value) {
        drone.getPersistentData().putBoolean(TAG_OUT_OF_ENERGY, value);
    }

    private static ServerPlayer getController(Entity drone) {
        Entity controller = getControllerEntity(drone);
        return controller instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    private static Entity getControllerEntity(Entity drone) {
        try {
            Method method = drone.getClass().getMethod("getController");
            Object result = method.invoke(drone);
            return result instanceof Entity entity ? entity : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void syncIfNeeded(Entity drone, ServerPlayer controller, int currentEnergy, boolean lowEnergy, boolean outOfEnergy) {
        syncIfNeeded(drone, controller, currentEnergy, lowEnergy, outOfEnergy, false);
    }

    private static void syncIfNeeded(Entity drone, ServerPlayer controller, int currentEnergy, boolean lowEnergy, boolean outOfEnergy, boolean force) {
        if (controller == null) {
            return;
        }

        var data = drone.getPersistentData();
        if (!force
                && data.getInt(TAG_LAST_SYNC_ENERGY) == currentEnergy
                && data.getBoolean(TAG_LAST_SYNC_LOW) == lowEnergy
                && data.getBoolean(TAG_LAST_SYNC_EMPTY) == outOfEnergy
                && drone.tickCount % 10 != 0) {
            return;
        }

        data.putInt(TAG_LAST_SYNC_ENERGY, currentEnergy);
        data.putBoolean(TAG_LAST_SYNC_LOW, lowEnergy);
        data.putBoolean(TAG_LAST_SYNC_EMPTY, outOfEnergy);
        AddonNetwork.sendDroneEnergy(
                controller,
                new DroneEnergySyncMessage(drone.getUUID(), currentEnergy, getMaxEnergy(drone), outOfEnergy, lowEnergy)
        );
    }

    private static boolean invokeBooleanGetter(Entity entity, String methodName) {
        try {
            Method method = entity.getClass().getMethod(methodName);
            Object result = method.invoke(entity);
            return result instanceof Boolean value && value;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static void invokeBooleanSetter(Entity entity, String methodName, boolean value) {
        try {
            Method method = entity.getClass().getMethod(methodName, boolean.class);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void invokeFloatSetter(Entity entity, String methodName, float value) {
        try {
            Method method = entity.getClass().getMethod(methodName, float.class);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
