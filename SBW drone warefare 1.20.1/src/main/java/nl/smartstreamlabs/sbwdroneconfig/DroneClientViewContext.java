package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class DroneClientViewContext {
    private static UUID readOnlyInterceptDroneId;

    private DroneClientViewContext() {
    }

    public static ItemStack activeMonitor(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        return player == null ? ItemStack.EMPTY : SbwCompat.getActiveLinkedMonitor(player);
    }

    public static UUID activeDroneId(Minecraft minecraft) {
        if (readOnlyInterceptDroneId != null) {
            return readOnlyInterceptDroneId;
        }
        ItemStack monitor = activeMonitor(minecraft);
        return monitor.isEmpty() ? null : SbwCompat.getLinkedDroneUuid(monitor);
    }

    public static Entity activeDroneEntity(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        UUID linkedDroneId = activeDroneId(minecraft);
        if (level == null || linkedDroneId == null) {
            return null;
        }

        Entity fiberEntity = DroneFiberOpticClient.findActiveDroneEntity(level);
        if (fiberEntity != null && linkedDroneId.equals(fiberEntity.getUUID())) {
            return fiberEntity;
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (linkedDroneId.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }

    public static CubedFpvDroneEntity activeCubedFpvDrone(Minecraft minecraft) {
        Entity drone = activeDroneEntity(minecraft);
        return drone instanceof CubedFpvDroneEntity cubed ? cubed : null;
    }

    public static DeliveryDroneEntity activeDeliveryDrone(Minecraft minecraft) {
        Entity drone = activeDroneEntity(minecraft);
        return isExactDeliveryDrone(drone) ? (DeliveryDroneEntity) drone : null;
    }

    public static boolean isLocalFpvViewOf(CubedFpvDroneEntity drone) {
        if (drone == null) {
            return false;
        }
        if (isReadOnlyCameraIntercept(drone)) {
            return true;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (minecraft.player == null || level == null || drone.level() != level) {
            return false;
        }

        ItemStack monitor = activeMonitor(minecraft);
        UUID linkedDroneId = SbwCompat.getLinkedDroneUuid(monitor);
        if (linkedDroneId == null || !linkedDroneId.equals(drone.getUUID())) {
            return false;
        }

        Entity activeDrone = activeDroneEntity(minecraft);
        return activeDrone instanceof CubedFpvDroneEntity
                && activeDrone.getId() == drone.getId()
                && activeDrone.getUUID().equals(drone.getUUID());
    }

    public static LucasDroneEntity activeLucasDrone(Minecraft minecraft) {
        Entity drone = activeDroneEntity(minecraft);
        return drone instanceof LucasDroneEntity lucas ? lucas : null;
    }

    public static boolean isLocalDeliveryViewOf(DeliveryDroneEntity drone) {
        if (drone == null) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (minecraft.player == null || level == null || drone.level() != level) {
            return false;
        }

        ItemStack monitor = activeMonitor(minecraft);
        UUID linkedDroneId = SbwCompat.getLinkedDroneUuid(monitor);
        if (linkedDroneId == null || !linkedDroneId.equals(drone.getUUID())) {
            return false;
        }

        Entity activeDrone = activeDroneEntity(minecraft);
        return isExactDeliveryDrone(activeDrone)
                && activeDrone.getId() == drone.getId()
                && activeDrone.getUUID().equals(drone.getUUID());
    }

    public static boolean isActiveCubedFpvView() {
        return activeCubedFpvDrone(Minecraft.getInstance()) != null;
    }

    public static boolean isActiveLucasView() {
        return activeLucasDrone(Minecraft.getInstance()) != null;
    }

    public static boolean isActiveDeliveryView() {
        return activeDeliveryDrone(Minecraft.getInstance()) != null;
    }

    public static void beginReadOnlyCameraIntercept(UUID droneId) {
        readOnlyInterceptDroneId = droneId;
    }

    public static void endReadOnlyCameraIntercept() {
        readOnlyInterceptDroneId = null;
    }

    public static boolean isReadOnlyCameraInterceptActive() {
        return readOnlyInterceptDroneId != null;
    }

    public static boolean isReadOnlyCameraIntercept(Entity entity) {
        return entity != null && readOnlyInterceptDroneId != null && readOnlyInterceptDroneId.equals(entity.getUUID());
    }

    private static boolean isExactDeliveryDrone(Entity entity) {
        return entity instanceof DeliveryDroneEntity && entity.getType() == AddonEntities.DELIVERY_DRONE.get();
    }
}
