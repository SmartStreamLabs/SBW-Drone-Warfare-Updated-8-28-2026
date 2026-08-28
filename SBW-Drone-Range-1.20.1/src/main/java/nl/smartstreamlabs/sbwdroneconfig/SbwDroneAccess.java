package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface SbwDroneAccess {
    String adapterName();

    String supportedVersionPrefix();

    String[] monitorClassNames();

    String[] droneItemClassNames();

    String[] deployerItemClassNames();

    default boolean isMonitor(ItemStack stack) {
        return SbwItemResolver.stackMatches(stack, SbwDroneRangeConfig.MONITOR_ITEM_ID);
    }

    default boolean isSbwDrone(Entity entity) {
        return SbwEntityResolver.entityTypeMatches(entity, SbwDroneRangeConfig.DRONE_ENTITY_ID);
    }

    default boolean isSupportedDrone(Entity entity) {
        return SbwEntityResolver.entityTypeMatchesAny(
                entity,
                SbwDroneRangeConfig.DRONE_ENTITY_ID,
                SbwDroneRangeConfig.CUBED_FPV_DRONE_ENTITY_ID,
                SbwDroneRangeConfig.DELIVERY_DRONE_ENTITY_ID,
                SbwDroneRangeConfig.GLIDE_BOMB_ENTITY_ID,
                SbwDroneRangeConfig.GERANIUM_2_ENTITY_ID,
                SbwDroneRangeConfig.HEAVY_BOMBER_DRONE_ENTITY_ID,
                SbwDroneRangeConfig.UKRAINIAN_BAYRAKTAR_TB2_ENTITY_ID,
                SbwDroneRangeConfig.LUCAS_DRONE_ENTITY_ID
        );
    }

    default SbwCompat.DroneOwnerIdentity getDroneOwner(ServerLevel level, Entity drone) {
        return SbwCompat.resolveDroneOwner(level, drone);
    }

    default Vec3 getDronePosition(Entity drone) {
        return drone == null ? Vec3.ZERO : drone.position();
    }

    default String getDroneDisplayName(Entity drone) {
        if (drone == null) {
            return "";
        }

        Component customName = drone.getCustomName();
        if (customName != null) {
            return customName.getString();
        }

        return drone.getType().getDescription().getString();
    }

    default Item resolveSbwItem(String id) {
        return SbwItemResolver.resolve(id);
    }

    default EntityType<?> resolveSbwEntityType(String id) {
        return SbwEntityResolver.resolve(id);
    }

    default void unlinkMonitor(ItemStack stack, Player player) {
        if (!isMonitor(stack)) {
            return;
        }

        CompoundTag tag = AddonStackTags.getOrCreate(stack);
        tag.putBoolean(SbwCompat.TAG_LINKED, false);
        tag.putBoolean(SbwCompat.TAG_USING, false);
        tag.putString(SbwCompat.TAG_LINKED_DRONE, "none");
        tag.remove("PosX");
        tag.remove("PosY");
        tag.remove("PosZ");
        AddonStackTags.save(stack, tag);
    }

    default boolean hasAnyClass(String[] classNames) {
        if (classNames == null) {
            return false;
        }

        for (String className : classNames) {
            if (classExists(className)) {
                return true;
            }
        }
        return false;
    }

    default boolean classExists(String className) {
        if (className == null || className.isBlank()) {
            return false;
        }
        String resourcePath = className.replace('.', '/') + ".class";
        return SbwDroneAccess.class.getClassLoader().getResource(resourcePath) != null;
    }
}
