package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

public final class SbwCompat {
    public static final String TAG_LINKED = "Linked";
    public static final String TAG_USING = "Using";
    public static final String TAG_LINKED_DRONE = "LinkedDrone";
    public static final String TAG_DRONE_OWNER_UUID = "sbwdroneconfigDroneOwnerUuid";
    public static final String TAG_DRONE_OWNER_NAME = "sbwdroneconfigDroneOwnerName";
    private static final String SBW_VEHICLE_ENTITY_CLASS = "com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity";
    private static volatile Class<?> sbwVehicleEntityClass;
    private static volatile boolean sbwVehicleEntityLookupAttempted;

    private SbwCompat() {
    }

    public static boolean isMonitor(ItemStack stack) {
        return isLegacyMonitor(stack);
    }

    public static boolean isLegacyMonitor(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null && SbwDroneRangeConfig.MONITOR_ITEM_ID.equals(key.toString());
    }

    public static boolean isFpvGoggles(ItemStack stack) {
        return !stack.isEmpty() && stack.is(AddonItems.FPV_GOGGLES.get());
    }

    public static boolean isLinkedMonitor(ItemStack stack) {
        if (!isMonitor(stack)) {
            return false;
        }
        normalizeInactiveMonitorTag(stack);
        return isLinkedMonitorTag(stack.getTag());
    }

    public static boolean isLegacyLinkedMonitor(ItemStack stack) {
        if (!isLegacyMonitor(stack)) {
            return false;
        }
        normalizeInactiveMonitorTag(stack);
        return isLinkedMonitorTag(stack.getTag());
    }

    public static boolean isLegacyUsingMonitor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return isLegacyLinkedMonitor(stack) && tag != null && tag.getBoolean(TAG_USING);
    }

    public static boolean isUsingLinkedMonitor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return isLinkedMonitor(stack) && tag != null && tag.getBoolean(TAG_USING);
    }

    public static UUID getLinkedDroneUuid(ItemStack stack) {
        if (!isLinkedMonitor(stack)) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        String uuidString = tag.getString(TAG_LINKED_DRONE);
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean isLinkedMonitorTag(CompoundTag tag) {
        return tag != null
                && tag.getBoolean(TAG_LINKED)
                && tag.contains(TAG_LINKED_DRONE)
                && !"none".equals(tag.getString(TAG_LINKED_DRONE));
    }

    public static void normalizeInactiveMonitorStack(ItemStack stack) {
        if (!isMonitor(stack)) {
            return;
        }
        normalizeInactiveMonitorTag(stack);
    }

    private static void normalizeInactiveMonitorTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || isLinkedMonitorTag(tag)) {
            return;
        }

        if (containsOnlyInactiveMonitorState(tag)) {
            stack.setTag(null);
        }
    }

    private static boolean containsOnlyInactiveMonitorState(CompoundTag tag) {
        if (tag.isEmpty()) {
            return true;
        }

        for (String key : tag.getAllKeys()) {
            if (!isMonitorStateKey(key)) {
                return false;
            }
        }

        if (tag.getBoolean(TAG_LINKED) || tag.getBoolean(TAG_USING)) {
            return false;
        }

        return !tag.contains(TAG_LINKED_DRONE) || "none".equals(tag.getString(TAG_LINKED_DRONE));
    }

    private static boolean isMonitorStateKey(String key) {
        return TAG_LINKED.equals(key)
                || TAG_USING.equals(key)
                || TAG_LINKED_DRONE.equals(key)
                || "PosX".equals(key)
                || "PosY".equals(key)
                || "PosZ".equals(key);
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
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && (SbwDroneRangeConfig.DRONE_ENTITY_ID.equals(key.toString())
                || SbwDroneRangeConfig.CUBED_FPV_DRONE_ENTITY_ID.equals(key.toString())
                || SbwDroneRangeConfig.DELIVERY_DRONE_ENTITY_ID.equals(key.toString())
                || SbwDroneRangeConfig.GLIDE_BOMB_ENTITY_ID.equals(key.toString())
                || SbwDroneRangeConfig.GERANIUM_2_ENTITY_ID.equals(key.toString())
                || SbwDroneRangeConfig.HEAVY_BOMBER_DRONE_ENTITY_ID.equals(key.toString())
                || SbwDroneRangeConfig.UKRAINIAN_BAYRAKTAR_TB2_ENTITY_ID.equals(key.toString())
                || SbwDroneRangeConfig.LUCAS_DRONE_ENTITY_ID.equals(key.toString()));
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

    public static boolean hasVehicleAntiDroneNetUpgrade(Entity vehicle) {
        if (!AddonConfig.vehicleAntiDroneNetEnabled() || !isSbwVehicle(vehicle) || !vehicle.isAlive()) {
            return false;
        }

        int size = getVehicleContainerSize(vehicle);
        if (size <= 0) {
            return false;
        }

        for (int slot = 0; slot < size; slot++) {
            if (isVehicleAntiDroneNetUpgradeStack(getVehicleItem(vehicle, slot))) {
                return true;
            }
        }

        return false;
    }

    public static Vec3 getVehicleRightVector(Entity vehicle, float partialTick) {
        Vec3 reflected = invokeVehicleVector(vehicle, "getRightVec", partialTick);
        if (reflected != null && reflected.lengthSqr() > 1.0E-6D) {
            return reflected.normalize();
        }

        Vec3 forward = getVehicleForwardFallback(vehicle);
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        return right.lengthSqr() > 1.0E-6D ? right.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
    }

    public static Vec3 getVehicleUpVector(Entity vehicle, float partialTick) {
        Vec3 reflected = invokeVehicleVector(vehicle, "getUpVec", partialTick);
        if (reflected != null && reflected.lengthSqr() > 1.0E-6D) {
            return reflected.normalize();
        }
        return new Vec3(0.0D, 1.0D, 0.0D);
    }

    public static Vec3 getVehicleForwardVector(Entity vehicle, float partialTick) {
        Vec3 right = getVehicleRightVector(vehicle, partialTick);
        Vec3 up = getVehicleUpVector(vehicle, partialTick);
        Vec3 forward = right.cross(up);
        if (forward.lengthSqr() > 1.0E-6D) {
            return forward.normalize();
        }
        return getVehicleForwardFallback(vehicle);
    }

    public static boolean isUsingLinkedMonitorForDrone(Player player, Entity drone) {
        if (player == null || drone == null) {
            return false;
        }
        return isUsingLinkedMonitorForDrone(getActiveLinkedMonitor(player), drone);
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
        return activeMonitor.isEmpty() ? findMonitorStack(player, false) : activeMonitor;
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
        return requireUsing ? isLegacyUsingMonitor(stack) : isLegacyLinkedMonitor(stack);
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

    public static boolean hasLinkedMonitorForDrone(Player player, Entity drone) {
        return !getLinkedMonitorForDrone(player, drone).isEmpty();
    }

    public static ItemStack getLinkedMonitorForDrone(Player player, Entity drone) {
        if (player == null || drone == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (isLegacyLinkedMonitorForDrone(mainHand, drone)) {
            return mainHand;
        }

        ItemStack offhand = player.getOffhandItem();
        if (isLegacyLinkedMonitorForDrone(offhand, drone)) {
            return offhand;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (isLegacyLinkedMonitorForDrone(stack, drone)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isLinkedMonitorForDrone(ItemStack stack, Entity drone) {
        if (stack.isEmpty() || drone == null) {
            return false;
        }

        UUID linkedDrone = getLinkedDroneUuid(stack);
        return linkedDrone != null && linkedDrone.equals(drone.getUUID());
    }

    private static boolean isLegacyLinkedMonitorForDrone(ItemStack stack, Entity drone) {
        if (stack.isEmpty() || drone == null || !isLegacyLinkedMonitor(stack)) {
            return false;
        }

        UUID linkedDrone = getLinkedDroneUuid(stack);
        return linkedDrone != null && linkedDrone.equals(drone.getUUID());
    }

    public static boolean isSbwBattery(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
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
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.putBoolean(TAG_USING, false);
        normalizeInactiveMonitorTag(stack);
    }

    public static void setUsingMonitor(ItemStack stack, boolean using) {
        if (!isLinkedMonitor(stack)) {
            return;
        }
        stack.getOrCreateTag().putBoolean(TAG_USING, using);
    }

    public static void stopUsingLegacyMonitor(ItemStack stack) {
        if (!isLegacyMonitor(stack)) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.putBoolean(TAG_USING, false);
        normalizeInactiveMonitorTag(stack);
    }

    public static void linkMonitorToDrone(ItemStack stack, Entity drone, boolean using) {
        if (!isMonitor(stack) || drone == null) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_LINKED, true);
        tag.putBoolean(TAG_USING, using);
        tag.putString(TAG_LINKED_DRONE, drone.getStringUUID());
    }

    public static boolean unlinkMonitorFromDrone(ItemStack stack, Player player, Entity drone) {
        if (!isLinkedMonitorForDrone(stack, drone)) {
            return false;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_LINKED, false);
        tag.putBoolean(TAG_USING, false);
        tag.putString(TAG_LINKED_DRONE, "none");
        tag.remove("PosX");
        tag.remove("PosY");
        tag.remove("PosZ");
        normalizeInactiveMonitorTag(stack);
        return true;
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
        if (drone == null || !isDrone(drone)) {
            return null;
        }

        try {
            Method method = drone.getClass().getMethod("getController");
            Object result = method.invoke(drone);
            if (result instanceof Player player) {
                return player.getUUID();
            }
            if (result instanceof Entity entity) {
                return entity.getUUID();
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return null;
    }

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

    private static Class<?> getSbwVehicleEntityClass() {
        Class<?> cached = sbwVehicleEntityClass;
        if (cached != null || sbwVehicleEntityLookupAttempted) {
            return cached;
        }

        sbwVehicleEntityLookupAttempted = true;
        try {
            cached = Class.forName(SBW_VEHICLE_ENTITY_CLASS);
            sbwVehicleEntityClass = cached;
            return cached;
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static int getVehicleContainerSize(Entity vehicle) {
        Object result = invokeNoArg(vehicle, "getContainerSize");
        return result instanceof Number number ? Math.max(0, number.intValue()) : 0;
    }

    private static ItemStack getVehicleItem(Entity vehicle, int slot) {
        if (vehicle == null) {
            return ItemStack.EMPTY;
        }

        try {
            Object result = vehicle.getClass().getMethod("getItem", int.class).invoke(vehicle, slot);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static Object invokeNoArg(Entity entity, String methodName) {
        if (entity == null) {
            return null;
        }

        try {
            return entity.getClass().getMethod(methodName).invoke(entity);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static Vec3 invokeVehicleVector(Entity vehicle, String methodName, float partialTick) {
        if (!isSbwVehicle(vehicle)) {
            return null;
        }

        try {
            Object result = vehicle.getClass().getMethod(methodName, float.class).invoke(vehicle, partialTick);
            return result instanceof Vec3 vec3 ? vec3 : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static Vec3 getVehicleForwardFallback(Entity vehicle) {
        if (vehicle == null) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }

        Vec3 forward = Vec3.directionFromRotation(vehicle.getXRot(), vehicle.getYRot());
        return forward.lengthSqr() > 1.0E-6D ? forward.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
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
