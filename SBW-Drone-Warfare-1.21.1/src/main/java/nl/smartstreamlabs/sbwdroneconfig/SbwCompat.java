package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

public final class SbwCompat {
    public static final String TAG_LINKED = "Linked";
    public static final String TAG_USING = "Using";
    public static final String TAG_LINKED_DRONE = "LinkedDrone";
    public static final String TAG_DRONE_OWNER_UUID = "sbwdroneconfigDroneOwnerUuid";
    public static final String TAG_DRONE_OWNER_NAME = "sbwdroneconfigDroneOwnerName";
    private static final SbwDroneAccess SBW_ACCESS = SbwVersionDetector.selectAdapter();
    private static final String SBW_VEHICLE_ENTITY_CLASS = "com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity";
    private static volatile Class<?> sbwVehicleEntityClass;
    private static volatile boolean sbwVehicleEntityLookupAttempted;

    private SbwCompat() {
    }

    public static SbwDroneAccess access() {
        return SBW_ACCESS;
    }

    public static String selectedAdapterName() {
        return SBW_ACCESS.adapterName();
    }

    public static boolean isMonitor(ItemStack stack) {
        return isLegacyMonitor(stack);
    }

    public static boolean isLegacyMonitor(ItemStack stack) {
        return SBW_ACCESS.isMonitor(stack);
    }

    public static boolean isFpvGoggles(ItemStack stack) {
        return !stack.isEmpty() && stack.is(AddonItems.FPV_GOGGLES.get());
    }

    public static boolean isLinkedMonitor(ItemStack stack) {
        if (!isMonitor(stack)) {
            return false;
        }
        return AddonStackTags.readBoolean(stack, TAG_LINKED) && AddonStackTags.contains(stack, TAG_LINKED_DRONE);
    }

    public static boolean isLegacyLinkedMonitor(ItemStack stack) {
        if (!isLegacyMonitor(stack)) {
            return false;
        }
        return AddonStackTags.readBoolean(stack, TAG_LINKED) && AddonStackTags.contains(stack, TAG_LINKED_DRONE);
    }

    public static boolean isLegacyUsingMonitor(ItemStack stack) {
        return isLegacyLinkedMonitor(stack) && AddonStackTags.readBoolean(stack, TAG_USING);
    }

    public static boolean isUsingLinkedMonitor(ItemStack stack) {
        return isLinkedMonitor(stack) && AddonStackTags.readBoolean(stack, TAG_USING);
    }

    public static UUID getLinkedDroneUuid(ItemStack stack) {
        if (!isLinkedMonitor(stack)) {
            return null;
        }
        String uuidString = AddonStackTags.readString(stack, TAG_LINKED_DRONE);
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static UUID getLegacyLinkedDroneUuid(ItemStack stack) {
        if (!isLegacyLinkedMonitor(stack)) {
            return null;
        }
        String uuidString = AddonStackTags.readString(stack, TAG_LINKED_DRONE);
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static Entity findLinkedDrone(ServerPlayer player, ItemStack stack) {
        UUID uuid = getLinkedDroneUuid(stack);
        if (uuid == null) {
            return null;
        }
        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(uuid);
        if (entity != null && isDrone(entity)) {
            return entity;
        }
        return null;
    }

    public static boolean isDrone(Entity entity) {
        if(entity instanceof Fl01Entity)return true;
        if(entity instanceof DeliveryRotorPart)return false;
        return isSupportedDrone(entity);
    }

    public static boolean isSbwDrone(Entity entity) {
        return SBW_ACCESS.isSbwDrone(entity);
    }

    public static boolean isSupportedDrone(Entity entity) {
        return entity instanceof Sd01Entity || entity instanceof Gad01Entity || SBW_ACCESS.isSupportedDrone(entity);
    }

    public static boolean isSbwVehicle(Entity entity) {
        if (entity == null || isDrone(entity)) {
            return false;
        }
        Class<?> vehicleClass = getSbwVehicleEntityClass();
        return vehicleClass != null && vehicleClass.isInstance(entity);
    }

    public static boolean isVehicleAntiDroneNetUpgradeStack(ItemStack stack) {
        return !stack.isEmpty() && stack.is(AddonItems.VEHICLE_ANTI_DRONE_NET_UPGRADE.get());
    }

    public static boolean isVehicleNetHost(Entity vehicle){return vehicle instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity && !(vehicle instanceof com.atsuishio.superbwarfare.entity.vehicle.DroneEntity);}
    public static boolean hasVehicleAntiDroneNetUpgrade(Entity vehicle) {
        if (!AddonConfig.vehicleAntiDroneNetEnabled() || !isVehicleNetHost(vehicle) || !vehicle.isAlive()) {
            return false;
        }

        Object inventory=invokeNoArg(vehicle,"getInventory");
        if(inventory instanceof net.neoforged.neoforge.items.IItemHandler handler){for(int slot=0;slot<handler.getSlots();slot++)if(isVehicleAntiDroneNetUpgradeStack(handler.getStackInSlot(slot)))return true;return false;}

        int size = getVehicleContainerSize(vehicle);
        for (int slot = 0; slot < size; slot++) {
            if (isVehicleAntiDroneNetUpgradeStack(getVehicleItem(vehicle, slot))) {
                return true;
            }
        }
        return false;
    }

    public static Vec3 getVehicleRightVector(Entity vehicle, float partialTick) {
        Vec3 right = invokeVehicleVector(vehicle, "getRightVec", partialTick);
        return right != null ? right : Vec3.directionFromRotation(0.0F, vehicle.getYRot() + 90.0F);
    }

    public static Vec3 getVehicleUpVector(Entity vehicle, float partialTick) {
        Vec3 up = invokeVehicleVector(vehicle, "getUpVec", partialTick);
        return up != null ? up : new Vec3(0.0D, 1.0D, 0.0D);
    }

    public static Vec3 getVehicleForwardVector(Entity vehicle, float partialTick) {
        Vec3 forward = invokeVehicleVector(vehicle, "getForwardVec", partialTick);
        return forward != null ? forward : getVehicleForwardFallback(vehicle);
    }

    public static DroneOwnerIdentity getDroneOwner(ServerLevel level, Entity drone) {
        return SBW_ACCESS.getDroneOwner(level, drone);
    }

    public static Vec3 getDronePosition(Entity drone) {
        return SBW_ACCESS.getDronePosition(drone);
    }

    public static String getDroneDisplayName(Entity drone) {
        return SBW_ACCESS.getDroneDisplayName(drone);
    }

    public static ItemStack getActiveLinkedMonitor(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        return findMonitorStack(player, true);
    }

    public static ItemStack getLinkedMonitor(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack activeMonitor = getActiveLinkedMonitor(player);
        if (!activeMonitor.isEmpty()) {
            return activeMonitor;
        }

        return findMonitorStack(player, false);
    }

    public static ItemStack getEquippedFpvGoggles(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
        return isFpvGoggles(headStack) ? headStack : ItemStack.EMPTY;
    }

    public static boolean hasEquippedFpvGoggles(Player player) {
        return !getEquippedFpvGoggles(player).isEmpty();
    }

    private static ItemStack findMonitorStack(Player player, boolean requireUsing) {
        ItemStack mainHand = player.getMainHandItem();
        if (matchesPilotStack(mainHand, requireUsing)) {
            return mainHand;
        }

        ItemStack offhand = player.getOffhandItem();
        if (matchesPilotStack(offhand, requireUsing)) {
            return offhand;
        }

        if (!requireUsing) {
            for (ItemStack stack : player.getInventory().items) {
                if (isLegacyLinkedMonitor(stack)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean matchesPilotStack(ItemStack stack, boolean requireUsing) {
        return requireUsing ? isUsingLinkedMonitor(stack) : isLinkedMonitor(stack);
    }

    public static net.minecraft.world.item.Item resolveSbwItem(String id) {
        return SBW_ACCESS.resolveSbwItem(id);
    }

    public static EntityType<?> resolveSbwEntityType(String id) {
        return SBW_ACCESS.resolveSbwEntityType(id);
    }

    public static boolean isUsingLinkedMonitorForDrone(Player player, Entity drone) {
        if (player == null || drone == null) {
            return false;
        }
        return isUsingLinkedMonitorForDrone(getActiveLinkedMonitor(player), drone);
    }

    public static Entity findActiveLinkedDrone(ServerPlayer player) {
        if (player == null) {
            return null;
        }

        ItemStack activeMonitor = getActiveLinkedMonitor(player);
        if (activeMonitor.isEmpty()) {
            return null;
        }

        return findLinkedDrone(player, activeMonitor);
    }

    public static boolean isUsingLinkedMonitorForDrone(ItemStack stack, Entity drone) {
        if (stack.isEmpty() || drone == null) {
            return false;
        }

        UUID linkedDrone = getLinkedDroneUuid(stack);
        return isUsingLinkedMonitor(stack) && linkedDrone != null && linkedDrone.equals(drone.getUUID());
    }

    public static boolean hasLinkedMonitorForDrone(Player player, Entity drone) {
        if (player == null || drone == null) {
            return false;
        }

        return !getLinkedMonitorForDrone(player, drone).isEmpty();
    }

    public static ItemStack getLinkedMonitorForDrone(Player player, Entity drone) {
        if (player == null || drone == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (isLinkedMonitorForDrone(mainHand, drone)) {
            return mainHand;
        }

        ItemStack offhand = player.getOffhandItem();
        if (isLinkedMonitorForDrone(offhand, drone)) {
            return offhand;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (isLegacyLinkedMonitorForDrone(stack, drone)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static ServerPlayer findLinkedMonitorController(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !isDrone(drone)) {
            return null;
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (hasLinkedMonitorForDrone(player, drone)) {
                return player;
            }
        }

        return null;
    }

    public static boolean isLinkedMonitorForDrone(ItemStack stack, Entity drone) {
        if (stack.isEmpty() || drone == null) {
            return false;
        }

        UUID linkedDrone = getLinkedDroneUuid(stack);
        return linkedDrone != null && linkedDrone.equals(drone.getUUID());
    }

    public static boolean isSbwBattery(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null || !SbwDroneRangeConfig.SBW_MOD_ID.equals(key.getNamespace())) {
            return false;
        }
        String path = key.getPath();
        return "cell".equals(path)
                || "battery".equals(path)
                || "small_battery_pack".equals(path)
                || "medium_battery_pack".equals(path)
                || "large_battery_pack".equals(path);
    }

    public static double distanceToLinkedDrone(ServerPlayer player, Entity drone) {
        return player.position().distanceTo(drone.position());
    }

    public static void stopUsingMonitor(ItemStack stack) {
        if (!isMonitor(stack)) {
            return;
        }
        AddonStackTags.update(stack, tag -> tag.putBoolean(TAG_USING, false));
    }

    public static void setUsingMonitor(ItemStack stack, boolean using) {
        if (!isLinkedMonitor(stack)) {
            return;
        }
        AddonStackTags.update(stack, tag -> tag.putBoolean(TAG_USING, using));
    }

    public static void stopUsingLegacyMonitor(ItemStack stack) {
        if (!isLegacyMonitor(stack)) {
            return;
        }
        AddonStackTags.update(stack, tag -> tag.putBoolean(TAG_USING, false));
    }

    public static void linkMonitorToDrone(ItemStack stack, Entity drone, boolean using) {
        if (!isMonitor(stack) || drone == null) {
            return;
        }

        AddonStackTags.update(stack, tag -> {
            tag.putBoolean(TAG_LINKED, true);
            tag.putBoolean(TAG_USING, using);
            tag.putString(TAG_LINKED_DRONE, drone.getStringUUID());
        });
    }

    public static boolean stopUsingLinkedMonitorForDrone(Player player, Entity drone) {
        if (player == null || drone == null) {
            return false;
        }

        boolean stopped = false;
        if (isLinkedMonitorForDrone(player.getMainHandItem(), drone)) {
            stopUsingMonitor(player.getMainHandItem());
            stopped = true;
        }
        if (isLinkedMonitorForDrone(player.getOffhandItem(), drone)) {
            stopUsingMonitor(player.getOffhandItem());
            stopped = true;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (isLinkedMonitorForDrone(stack, drone)) {
                stopUsingMonitor(stack);
                stopped = true;
            }
        }

        return stopped;
    }

    public static boolean unlinkMonitorFromDrone(ItemStack stack, Player player, Entity drone) {
        if (isLinkedMonitorForDrone(stack, drone)) {
            AddonStackTags.update(stack, tag -> {
                tag.putBoolean(TAG_LINKED, false);
                tag.putBoolean(TAG_USING, false);
                tag.putString(TAG_LINKED_DRONE, "none");
                tag.remove("PosX");
                tag.remove("PosY");
                tag.remove("PosZ");
            });
            return true;
        }

        if (isLegacyLinkedMonitorForDrone(stack, drone)) {
            SBW_ACCESS.unlinkMonitor(stack, player);
            return true;
        }

        return false;
    }

    public static boolean disconnectControllersForDrone(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !isDrone(drone)) {
            return false;
        }

        boolean disconnected = false;
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            boolean playerDisconnected = disconnectPlayerFromDrone(player, drone);
            disconnected = disconnected || playerDisconnected;
        }
        return disconnected;
    }

    public static boolean disconnectPlayerFromDrone(ServerPlayer player, Entity drone) {
        if (player == null || drone == null || !isDrone(drone)) {
            return false;
        }

        boolean disconnected = false;
        disconnected = disconnectMonitorStack(player.getMainHandItem(), drone) || disconnected;
        disconnected = disconnectMonitorStack(player.getOffhandItem(), drone) || disconnected;

        for (ItemStack stack : player.getInventory().items) {
            disconnected = disconnectMonitorStack(stack, drone) || disconnected;
        }

        if (disconnected) {
            resetDroneInput(drone);
        }
        return disconnected;
    }

    private static boolean disconnectMonitorStack(ItemStack stack, Entity drone) {
        if (!isUsingLinkedMonitor(stack)) {
            return false;
        }

        UUID linkedDrone = getLinkedDroneUuid(stack);
        if (linkedDrone == null || !linkedDrone.equals(drone.getUUID())) {
            return false;
        }

        stopUsingMonitor(stack);
        return true;
    }

    private static boolean isLegacyLinkedMonitorForDrone(ItemStack stack, Entity drone) {
        if (stack.isEmpty() || drone == null) {
            return false;
        }

        UUID linkedDrone = getLegacyLinkedDroneUuid(stack);
        return linkedDrone != null && linkedDrone.equals(drone.getUUID());
    }

    public static void resetDroneInput(Entity drone) {
        CompoundTag data = drone.getPersistentData();
        data.putBoolean("left", false);
        data.putBoolean("right", false);
        data.putBoolean("forward", false);
        data.putBoolean("backward", false);
        data.putBoolean("up", false);
        data.putBoolean("down", false);
    }

    public static UUID getDroneControllerUuid(Entity drone) {
        if(drone instanceof Sd01Entity sd)return sd.operator()==null?null:sd.operator().getUUID();
        if(drone instanceof Gad01Entity g)return g.operator()==null?null:g.operator().getUUID();
        if (drone == null || !isDrone(drone)) {
            return null;
        }

        Entity controller = SbwDroneRuntimeAccess.controller(drone);
        return controller == null ? null : controller.getUUID();
    }
    public static Entity getDroneController(Entity drone) { if(drone instanceof Sd01Entity sd)return sd.operator(); return SbwDroneRuntimeAccess.controller(drone); }
    public static ItemStack getDroneCurrentItem(Entity drone) { if(drone instanceof Sd01Entity)return ItemStack.EMPTY; return SbwDroneRuntimeAccess.item(drone); }
    public static void setDroneCurrentItem(Entity drone, ItemStack stack) { if(drone instanceof Sd01Entity)return; SbwDroneRuntimeAccess.item(drone, stack); }
    public static boolean getDroneFire(Entity drone) { if(drone instanceof Sd01Entity)return false; return SbwDroneRuntimeAccess.fire(drone); }
    public static void setDroneFire(Entity drone, boolean value) { if(drone instanceof Sd01Entity)return; SbwDroneRuntimeAccess.fire(drone, value); }

    public static String getDroneCustomName(Entity drone) {
        if (drone == null || !isDrone(drone) || !drone.hasCustomName()) {
            return null;
        }

        Component customName = drone.getCustomName();
        if (customName == null) {
            return null;
        }

        String rawName = customName.getString();
        if (rawName == null) {
            return null;
        }

        String sanitized = rawName.trim();
        return sanitized.isEmpty() ? null : sanitized;
    }

    private static Class<?> getSbwVehicleEntityClass() {
        if (!sbwVehicleEntityLookupAttempted) {
            synchronized (SbwCompat.class) {
                if (!sbwVehicleEntityLookupAttempted) {
                    try {
                        sbwVehicleEntityClass = Class.forName(SBW_VEHICLE_ENTITY_CLASS);
                    } catch (ClassNotFoundException ignored) {
                        sbwVehicleEntityClass = null;
                    }
                    sbwVehicleEntityLookupAttempted = true;
                }
            }
        }
        return sbwVehicleEntityClass;
    }

    private static int getVehicleContainerSize(Entity vehicle) {
        Object inventory=invokeNoArg(vehicle,"getInventory");
        if(inventory instanceof net.neoforged.neoforge.items.IItemHandler handler)return handler.getSlots();
        if(inventory instanceof net.minecraft.world.Container container)return container.getContainerSize();
        if(vehicle instanceof net.minecraft.world.Container container)return container.getContainerSize();
        Object result = invokeNoArg(vehicle, "getContainerSize");
        return result instanceof Number number ? Math.max(0, number.intValue()) : 0;
    }

    private static ItemStack getVehicleItem(Entity vehicle, int slot) {
        Object inventory=invokeNoArg(vehicle,"getInventory");
        if(inventory instanceof net.neoforged.neoforge.items.IItemHandler handler)return slot>=0&&slot<handler.getSlots()?handler.getStackInSlot(slot):ItemStack.EMPTY;
        if(inventory instanceof net.minecraft.world.Container container)return container.getItem(slot);
        if(vehicle instanceof net.minecraft.world.Container container)return container.getItem(slot);

        try {
            Method method = vehicle.getClass().getMethod("getItem", int.class);
            Object result = method.invoke(vehicle, slot);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static Object invokeNoArg(Entity entity, String methodName) {
        try {
            Method method = entity.getClass().getMethod(methodName);
            return method.invoke(entity);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Vec3 invokeVehicleVector(Entity vehicle, String methodName, float partialTick) {
        try {
            Method method = vehicle.getClass().getMethod(methodName, float.class);
            Object result = method.invoke(vehicle, partialTick);
            return result instanceof Vec3 vector ? vector : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Vec3 getVehicleForwardFallback(Entity vehicle) {
        return Vec3.directionFromRotation(0.0F, vehicle.getYRot());
    }

    public static int getDroneAmmo(Entity drone) {
        if (drone == null || !isDrone(drone)) {
            return 0;
        }

        Integer methodAmmo = invokeIntGetter(drone, "getAmmo");
        if (methodAmmo != null) {
            return methodAmmo;
        }

        Integer droneAmmo = readSyncedIntAccessor(drone, "com.atsuishio.superbwarfare.entity.vehicle.DroneEntity", "AMMO");
        if (droneAmmo != null) {
            return droneAmmo;
        }

        Integer vehicleAmmo = readSyncedIntAccessor(drone, "com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity", "AMMO");
        return vehicleAmmo == null ? 0 : vehicleAmmo;
    }

    private static Integer invokeIntGetter(Entity entity, String methodName) {
        try {
            Method method = entity.getClass().getMethod(methodName);
            Object result = method.invoke(entity);
            return result instanceof Number number ? number.intValue() : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Integer readSyncedIntAccessor(Entity entity, String className, String fieldName) {
        try {
            Class<?> accessorOwner = Class.forName(className);
            Field field = accessorOwner.getField(fieldName);
            Object accessor = field.get(null);
            if (!(accessor instanceof EntityDataAccessor entityDataAccessor)) {
                return null;
            }

            Object value = entity.getEntityData().get(entityDataAccessor);
            return value instanceof Number number ? number.intValue() : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    public static void assignDroneOwner(Entity drone, Player player) {
        if (player == null) {
            return;
        }
        assignDroneOwner(drone, player.getUUID(), player.getGameProfile().getName());
    }

    public static void assignDroneOwner(Entity drone, UUID ownerUuid, String ownerName) {
        if (drone == null || !isDrone(drone) || ownerUuid == null) {
            return;
        }

        CompoundTag data = drone.getPersistentData();
        data.putUUID(TAG_DRONE_OWNER_UUID, ownerUuid);

        String normalizedName = sanitizeOwnerName(ownerName);
        if (normalizedName != null) {
            data.putString(TAG_DRONE_OWNER_NAME, normalizedName);
        } else {
            data.remove(TAG_DRONE_OWNER_NAME);
        }
    }

    public static void ensureDroneOwner(Entity drone, Player player) {
        if (drone == null || player == null || !isDrone(drone)) {
            return;
        }

        DroneOwnerIdentity ownerIdentity = resolveDroneOwner(player.level() instanceof ServerLevel serverLevel ? serverLevel : null, drone);
        if (!ownerIdentity.hasUuid()) {
            assignDroneOwner(drone, player);
        }
    }

    public static DroneOwnerIdentity resolveDroneOwner(ServerLevel level, Entity drone) {
        if (drone == null || !isDrone(drone)) {
            return DroneOwnerIdentity.UNKNOWN;
        }

        UUID ownerUuid = getStoredDroneOwnerUuid(drone);
        String ownerName = getStoredDroneOwnerName(drone);

        if (ownerUuid == null) {
            UUID controllerUuid = getDroneControllerUuid(drone);
            if (controllerUuid != null) {
                String controllerName = resolvePlayerName(level, controllerUuid);
                String resolvedName = controllerName != null ? controllerName : ownerName;
                assignDroneOwner(drone, controllerUuid, resolvedName);
                return new DroneOwnerIdentity(controllerUuid, sanitizeOwnerName(resolvedName));
            }

            ServerPlayer linkedController = findLinkedMonitorController(level, drone);
            if (linkedController != null) {
                assignDroneOwner(drone, linkedController);
                return new DroneOwnerIdentity(linkedController.getUUID(), linkedController.getGameProfile().getName());
            }

            if (ownerName != null) {
                UUID resolvedUuid = resolvePlayerUuid(level, ownerName);
                if (resolvedUuid != null) {
                    assignDroneOwner(drone, resolvedUuid, ownerName);
                    return new DroneOwnerIdentity(resolvedUuid, ownerName);
                }
            }

            return new DroneOwnerIdentity(null, ownerName);
        }

        if (ownerName == null) {
            String resolvedName = resolvePlayerName(level, ownerUuid);
            if (resolvedName != null) {
                assignDroneOwner(drone, ownerUuid, resolvedName);
                ownerName = resolvedName;
            }
        }

        return new DroneOwnerIdentity(ownerUuid, ownerName);
    }

    public static UUID getStoredDroneOwnerUuid(Entity drone) {
        if (drone == null || !isDrone(drone)) {
            return null;
        }

        CompoundTag data = drone.getPersistentData();
        return data.hasUUID(TAG_DRONE_OWNER_UUID) ? data.getUUID(TAG_DRONE_OWNER_UUID) : null;
    }

    public static String getStoredDroneOwnerName(Entity drone) {
        if (drone == null || !isDrone(drone)) {
            return null;
        }

        return sanitizeOwnerName(drone.getPersistentData().getString(TAG_DRONE_OWNER_NAME));
    }

    private static UUID resolvePlayerUuid(ServerLevel level, String ownerName) {
        String sanitized = sanitizeOwnerName(ownerName);
        if (level == null || sanitized == null) {
            return null;
        }

        ServerPlayer player = level.getServer().getPlayerList().getPlayerByName(sanitized);
        return player == null ? null : player.getUUID();
    }

    private static String resolvePlayerName(ServerLevel level, UUID ownerUuid) {
        if (level == null || ownerUuid == null) {
            return null;
        }

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUuid);
        return player == null ? null : sanitizeOwnerName(player.getGameProfile().getName());
    }

    private static String sanitizeOwnerName(String ownerName) {
        if (ownerName == null) {
            return null;
        }
        String sanitized = ownerName.trim();
        return sanitized.isEmpty() ? null : sanitized;
    }

    public record DroneOwnerIdentity(UUID uuid, String name) {
        public static final DroneOwnerIdentity UNKNOWN = new DroneOwnerIdentity(null, null);

        public DroneOwnerIdentity {
            name = sanitizeOwnerName(name);
        }

        public boolean isKnown() {
            return hasUuid() || hasName();
        }

        public boolean hasUuid() {
            return uuid != null;
        }

        public boolean hasName() {
            return name != null && !name.isBlank();
        }

        public String normalizedName() {
            return hasName() ? name.toLowerCase(Locale.ROOT) : "";
        }
    }
}
