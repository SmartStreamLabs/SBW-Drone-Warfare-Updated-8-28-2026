package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.Locale;

public final class DronePayloadSystem {
    private static final String TAG_LAST_MORTAR_PAYLOAD_DROP_TICK = "sbwdroneconfigLastMortarPayloadDropTick";

    private DronePayloadSystem() {
    }

    public static void dropMortarPayload(ServerPlayer player) {
        Entity drone = SbwCompat.findActiveLinkedDrone(player);
        if (drone == null || !(drone.level() instanceof ServerLevel serverLevel) || !drone.isAlive()) {
            sendStatus(player, "message.sbwdroneconfig.mortar_payload_no_drone", ChatFormatting.RED);
            return;
        }

        long gameTime = serverLevel.getGameTime();
        long lastDropTick = player.getPersistentData().getLong(TAG_LAST_MORTAR_PAYLOAD_DROP_TICK);
        int cooldownTicks = AddonConfig.antiVehicleMortarPayloadCooldownTicks();
        if (cooldownTicks > 0 && gameTime - lastDropTick < cooldownTicks) {
            sendStatus(player, "message.sbwdroneconfig.mortar_payload_cooldown", ChatFormatting.YELLOW);
            return;
        }

        if (!consumePayloadFromDroneInventory(drone)) {
            sendStatus(player, "message.sbwdroneconfig.mortar_payload_missing", ChatFormatting.RED);
            return;
        }

        AntiVehicleMortarPayloadEntity payload = new AntiVehicleMortarPayloadEntity(AddonEntities.ANTI_VEHICLE_MORTAR_PAYLOAD.get(), serverLevel);
        Vec3 droneLook = drone.getLookAngle();
        if (droneLook.lengthSqr() < 1.0E-4D) {
            droneLook = Vec3.directionFromRotation(drone.getXRot(), drone.getYRot());
        }
        Vec3 spawnPos = getMortarDropPosition(drone).add(droneLook.normalize().scale(0.12D));
        payload.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, drone.getYRot(), 70.0F);
        payload.setOwnerUuid(player.getUUID());
        payload.setSourceDroneUuid(drone.getUUID());
        payload.releaseFromDrone(drone.getDeltaMovement().scale(0.25D).add(droneLook.normalize().scale(0.08D)).add(0.0D, -0.45D, 0.0D));
        serverLevel.addFreshEntity(payload);

        player.getPersistentData().putLong(TAG_LAST_MORTAR_PAYLOAD_DROP_TICK, gameTime);
        sendStatus(player, "message.sbwdroneconfig.mortar_payload_dropped", ChatFormatting.GREEN);
    }

    public static Vec3 getMortarDropPosition(Entity drone) {
        Vec3 look = Vec3.directionFromRotation(0.0F, drone.getYRot());
        double yOffset = drone instanceof LucasDroneEntity ? -0.65D : -0.35D;
        double forwardOffset = drone instanceof LucasDroneEntity ? 0.2D : 0.0D;
        return drone.position().add(0.0D, yOffset, 0.0D).add(look.normalize().scale(forwardOffset));
    }

    private static boolean consumePayloadFromDroneInventory(Entity drone) {
        DroneInventoryContainer container = new DroneInventoryContainer(drone);
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(AddonItems.ANTI_VEHICLE_MORTAR_PAYLOAD.get())) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    container.setItem(slot, ItemStack.EMPTY);
                } else {
                    container.setChanged();
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isVehicleLike(Entity entity) {
        if (!canBeVehicleTarget(entity)) {
            return false;
        }

        String combined = combinedEntityIdAndClass(entity);
        boolean looksLikeVehicle = combined.contains("superbwarfare")
                && (combined.contains("vehicle")
                || combined.contains("tank")
                || combined.contains("apc")
                || combined.contains("mrap")
                || combined.contains("truck")
                || combined.contains("car")
                || combined.contains("plane")
                || combined.contains("aircraft")
                || combined.contains("helicopter")
                || combined.contains("artillery")
                || combined.contains("cannon")
                || combined.contains("boat")
                || combined.contains("lav"));
        return looksLikeVehicle || hasVehicleHealthApi(entity);
    }

    public static boolean hasVehicleHealthApi(Entity entity) {
        if (!canBeVehicleTarget(entity) || !combinedEntityIdAndClass(entity).contains("superbwarfare")) {
            return false;
        }
        return findNoArgNumberMethod(entity.getClass(), "getHealth", "getHp", "getHP", "getVehicleHealth", "getEntityHealth") != null
                && findNumericSetterMethod(entity.getClass(), "setHealth", "setHp", "setHP", "setVehicleHealth", "setEntityHealth") != null;
    }

    public static boolean applyVehicleDirectDamage(Entity entity, double damage) {
        if (!hasVehicleHealthApi(entity) || damage <= 0.0D) {
            return false;
        }

        Method getter = findNoArgNumberMethod(entity.getClass(), "getHealth", "getHp", "getHP", "getVehicleHealth", "getEntityHealth");
        Method setter = findNumericSetterMethod(entity.getClass(), "setHealth", "setHp", "setHP", "setVehicleHealth", "setEntityHealth");
        if (getter == null || setter == null) {
            return false;
        }

        try {
            Object value = getter.invoke(entity);
            if (!(value instanceof Number number)) {
                return false;
            }
            invokeNumericSetter(entity, setter, Math.max(0.0D, number.doubleValue() - damage));
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean canBeVehicleTarget(Entity entity) {
        return entity != null && !(entity instanceof Player) && !(entity instanceof LivingEntity) && !SbwCompat.isDrone(entity);
    }

    private static String combinedEntityIdAndClass(Entity entity) {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String id = typeId == null ? "" : typeId.toString().toLowerCase(Locale.ROOT);
        String className = entity.getClass().getName().toLowerCase(Locale.ROOT);
        return id + " " + className;
    }

    private static Method findNoArgNumberMethod(Class<?> type, String... names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && isNamed(method, names) && isNumericType(method.getReturnType())) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private static Method findNumericSetterMethod(Class<?> type, String... names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 1 && isNamed(method, names) && isNumericType(method.getParameterTypes()[0])) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean isNamed(Method method, String... names) {
        for (String name : names) {
            if (method.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNumericType(Class<?> type) {
        return type == float.class
                || type == double.class
                || type == int.class
                || type == long.class
                || type == Float.class
                || type == Double.class
                || type == Integer.class
                || type == Long.class
                || Number.class.isAssignableFrom(type);
    }

    private static void invokeNumericSetter(Entity entity, Method setter, double value) throws ReflectiveOperationException {
        Class<?> type = setter.getParameterTypes()[0];
        if (type == float.class || type == Float.class) {
            setter.invoke(entity, (float) value);
        } else if (type == double.class || type == Double.class) {
            setter.invoke(entity, value);
        } else if (type == int.class || type == Integer.class) {
            setter.invoke(entity, (int) Math.ceil(value));
        } else if (type == long.class || type == Long.class) {
            setter.invoke(entity, (long) Math.ceil(value));
        }
    }

    private static void sendStatus(ServerPlayer player, String translationKey, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(translationKey).withStyle(color), true);
    }
}
