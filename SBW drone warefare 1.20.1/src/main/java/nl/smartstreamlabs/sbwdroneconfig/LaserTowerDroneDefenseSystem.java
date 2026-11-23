package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class LaserTowerDroneDefenseSystem {
    private static final String LASER_TOWER_ENTITY_ID = "superbwarfare:laser_tower";
    private static final String TAG_WHITELIST = SbwDroneRangeConfig.MOD_ID + "_laser_tower_whitelist";
    private static final int MAX_WHITELIST_ENTRIES = 64;
    private static final double MAX_INTERACTION_DISTANCE_SQR = 12.0D * 12.0D;

    private LaserTowerDroneDefenseSystem() {
    }

    public static boolean isLaserTower(Entity entity) {
        if (entity == null) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && LASER_TOWER_ENTITY_ID.equals(key.toString());
    }

    public static boolean isLaserTowerWrench(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(AddonItems.LASER_TOWER_WRENCH.get());
    }

    public static double extendSeekRange(Entity tower, double original) {
        return isLaserTower(tower) ? Math.max(original, AddonConfig.laserDefenseTowerDroneRange()) : original;
    }

    public static Entity findDroneTarget(Entity tower, Vec3 origin, double minAngle, double maxAngle, double minRange, double seekRange, double minTargetSize) {
        if (!isLaserTower(tower) || tower.level().isClientSide) {
            return null;
        }

        double range = extendSeekRange(tower, seekRange);
        double minRangeSqr = minRange * minRange;
        double maxRangeSqr = range * range;
        AABB searchBox = new AABB(origin, origin).inflate(range);

        return tower.level()
                .getEntities(tower, searchBox, candidate -> isValidDroneTarget(tower, candidate, origin, minRangeSqr, maxRangeSqr))
                .stream()
                .min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(origin)))
                .orElse(null);
    }

    private static boolean isValidDroneTarget(Entity tower, Entity candidate, Vec3 origin, double minRangeSqr, double maxRangeSqr) {
        if (candidate == null || candidate.isRemoved() || !candidate.isAlive() || !SbwCompat.isDrone(candidate)) {
            return false;
        }
        double distanceSqr = candidate.distanceToSqr(origin);
        if (distanceSqr < minRangeSqr || distanceSqr > maxRangeSqr) {
            return false;
        }
        if (isDroneWhitelisted(tower, candidate)) {
            return false;
        }
        return hasLineOfSight(tower, origin, candidate);
    }

    private static boolean hasLineOfSight(Entity tower, Vec3 origin, Entity target) {
        if (tower.level() != target.level()) {
            return false;
        }
        Vec3 end = target.getBoundingBox().getCenter();
        HitResult hit = tower.level().clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, tower));
        return hit.getType() != HitResult.Type.BLOCK;
    }

    public static boolean isDroneWhitelisted(Entity tower, Entity drone) {
        if (!AddonConfig.laserDefenseTowerWhitelistEnabled()) {
            return false;
        }

        List<String> entries = getWhitelistedGamertags(tower);
        if (entries.isEmpty()) {
            return false;
        }

        SbwCompat.DroneOwnerIdentity owner = tower.level() instanceof ServerLevel serverLevel
                ? SbwCompat.resolveDroneOwner(serverLevel, drone)
                : SbwCompat.DroneOwnerIdentity.UNKNOWN;
        String ownerName = owner.name();
        String ownerUuid = owner.uuid() != null ? owner.uuid().toString() : null;
        String customName = drone.hasCustomName() ? drone.getCustomName().getString() : null;
        String entityName = drone.getName().getString();

        for (String entry : entries) {
            String normalized = normalize(entry);
            if (normalized.isEmpty()) {
                continue;
            }
            if (ownerName != null && normalize(ownerName).equals(normalized)) {
                return true;
            }
            if (ownerUuid != null && ownerUuid.equalsIgnoreCase(entry.trim())) {
                return true;
            }
            if (customName != null && normalize(customName).equals(normalized)) {
                return true;
            }
            if (normalize(entityName).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getWhitelistedGamertags(Entity tower) {
        List<String> result = new ArrayList<>();
        if (!isLaserTower(tower)) {
            return result;
        }

        ListTag list = tower.getPersistentData().getList(TAG_WHITELIST, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String value = sanitize(list.getString(i));
            if (!value.isEmpty() && result.stream().noneMatch(existing -> existing.equalsIgnoreCase(value))) {
                result.add(value);
            }
        }
        return result;
    }

    public static boolean addWhitelistedGamertag(Entity tower, String rawName) {
        String name = sanitize(rawName);
        if (!isLaserTower(tower) || name.isEmpty()) {
            return false;
        }

        List<String> entries = getWhitelistedGamertags(tower);
        if (entries.stream().anyMatch(existing -> existing.equalsIgnoreCase(name)) || entries.size() >= MAX_WHITELIST_ENTRIES) {
            return false;
        }

        entries.add(name);
        saveWhitelistedGamertags(tower, entries);
        return true;
    }

    public static boolean removeWhitelistedGamertag(Entity tower, String rawName) {
        String name = sanitize(rawName);
        if (!isLaserTower(tower) || name.isEmpty()) {
            return false;
        }

        List<String> entries = getWhitelistedGamertags(tower);
        boolean removed = entries.removeIf(existing -> existing.equalsIgnoreCase(name));
        if (removed) {
            saveWhitelistedGamertags(tower, entries);
        }
        return removed;
    }

    private static void saveWhitelistedGamertags(Entity tower, List<String> entries) {
        ListTag list = new ListTag();
        for (String entry : entries) {
            String sanitized = sanitize(entry);
            if (!sanitized.isEmpty()) {
                list.add(StringTag.valueOf(sanitized));
            }
        }
        tower.getPersistentData().put(TAG_WHITELIST, list);
    }

    public static boolean canConfigure(Player player, Entity tower) {
        return player instanceof ServerPlayer
                && isLaserTower(tower)
                && !tower.isRemoved()
                && player.distanceToSqr(tower) <= MAX_INTERACTION_DISTANCE_SQR
                && (isLaserTowerWrench(player.getItemInHand(InteractionHand.MAIN_HAND))
                || isLaserTowerWrench(player.getItemInHand(InteractionHand.OFF_HAND)));
    }

    public static void openWhitelist(ServerPlayer player, Entity tower) {
        AddonNetwork.openLaserTowerWhitelist(player, new OpenLaserTowerWhitelistMessage(tower.getId(), getWhitelistedGamertags(tower)));
    }

    private static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("@")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.length() > 36) {
            trimmed = trimmed.substring(0, 36);
        }
        return trimmed;
    }

    private static String normalize(String value) {
        return sanitize(value).toLowerCase(Locale.ROOT);
    }
}
