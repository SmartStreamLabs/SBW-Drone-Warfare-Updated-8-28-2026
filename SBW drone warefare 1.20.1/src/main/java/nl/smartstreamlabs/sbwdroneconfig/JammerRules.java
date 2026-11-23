package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import java.util.*;
public final class JammerRules {
    public static boolean affects(ServerLevel level, Entity entity, UUID ownerUuid, Set<String> whitelistedGamertags) {
        if(entity instanceof Hgd01Entity h)return Hgd01Jam.affects(level,h,ownerUuid,whitelistedGamertags);
        if (entity == null || !entity.isAlive() || !SbwCompat.isDrone(entity)) {
            return false;
        }
        String whitelistedGamertag = findWhitelistedGamertag(level, entity, whitelistedGamertags);
        if (whitelistedGamertag != null) {
            if (AddonConfig.debugJammer()) {
                SbwDroneRangeConfig.LOGGER.info(
                        "Jammer at {} ignored whitelisted player {} for drone {}",
                        entity.blockPosition(),
                        whitelistedGamertag,
                        entity.getUUID()
                );
            }
            return false;
        }
        if (AddonConfig.affectFriendlyDrones() || ownerUuid == null) {
            return true;
        }

        SbwCompat.DroneOwnerIdentity ownerIdentity = SbwCompat.resolveDroneOwner(level, entity);
        return !ownerIdentity.hasUuid() || !ownerUuid.equals(ownerIdentity.uuid());
    }

    private static String findWhitelistedGamertag(ServerLevel level, Entity drone, Set<String> whitelistedGamertags) {
        if (whitelistedGamertags.isEmpty()) {
            return null;
        }

        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        if (controllerUuid != null) {
            ServerPlayer controller = level.getServer().getPlayerList().getPlayer(controllerUuid);
            if (controller != null && whitelistedGamertags.contains(AddonConfig.normalizeName(controller.getGameProfile().getName(),AddonConfig.blacklistMatchCaseInsensitive()))) {
                return controller.getGameProfile().getName();
            }
        }

        SbwCompat.DroneOwnerIdentity ownerIdentity = SbwCompat.resolveDroneOwner(level, drone);
        if (ownerIdentity.hasName() && whitelistedGamertags.contains(AddonConfig.normalizeName(ownerIdentity.name(),AddonConfig.blacklistMatchCaseInsensitive()))) {
            return ownerIdentity.name();
        }

        if (ownerIdentity.hasUuid()) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerIdentity.uuid());
            if (owner != null && whitelistedGamertags.contains(AddonConfig.normalizeName(owner.getGameProfile().getName(),AddonConfig.blacklistMatchCaseInsensitive()))) {
                return owner.getGameProfile().getName();
            }
        }

        return null;
    }

}
