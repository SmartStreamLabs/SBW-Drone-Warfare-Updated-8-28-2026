package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import nl.smartstreamlabs.sbwdroneconfig.DroneTrackingHooks;
import nl.smartstreamlabs.sbwdroneconfig.SbwDroneRangeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class ChunkMapTrackedEntityMixin {
    @Unique
    private static final Field SBWDRONECONFIG_ENTITY_FIELD = resolveEntityField();

    @Redirect(
            method = "updatePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;min(II)I",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private int sbwdroneconfig$extendDroneTrackingRange(int effectiveRange, int clampedViewDistanceRange, ServerPlayer player) {
        Entity trackedEntity = sbwdroneconfig$getTrackedEntity();
        int forcedRange = DroneTrackingHooks.getForcedTrackingRangeBlocks(player, trackedEntity);
        if (forcedRange <= 0) {
            return Math.min(effectiveRange, clampedViewDistanceRange);
        }

        return Math.max(effectiveRange, forcedRange);
    }

    @Redirect(remap = false, method = "updatePlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
    private net.minecraft.world.phys.Vec3 drone$trackingPosition(ServerPlayer player) {
        return nl.smartstreamlabs.sbwdroneconfig.DvtServer.tracking(player,sbwdroneconfig$getTrackedEntity(),nl.smartstreamlabs.sbwdroneconfig.DroneClientChunkSyncManager.trackingPosition(player));
    }

    @Redirect(method="updatePlayer",remap=false,at=@At(value="INVOKE",
            target="Lnet/minecraft/server/level/ChunkMap;isChunkTracked(Lnet/minecraft/server/level/ServerPlayer;II)Z"))
    private boolean drone$keepCameraEntity(net.minecraft.server.level.ChunkMap map,ServerPlayer player,int x,int z) {
        // Keep the validated camera actor paired while vanilla's acknowledged chunk batches catch up.
        if(nl.smartstreamlabs.sbwdroneconfig.DroneClientChunkSyncManager.controls(player,sbwdroneconfig$getTrackedEntity())) return true;
        return ((DroneChunkMapAccess)map).drone$isChunkTracked(player,x,z);
    }

    @Unique
    private Entity sbwdroneconfig$getTrackedEntity() {
        if (SBWDRONECONFIG_ENTITY_FIELD == null) {
            return null;
        }

        try {
            return (Entity) SBWDRONECONFIG_ENTITY_FIELD.get(this);
        } catch (IllegalAccessException exception) {
            SbwDroneRangeConfig.LOGGER.warn("Could not read tracked Superb Warfare drone entity from ChunkMap$TrackedEntity.", exception);
            return null;
        }
    }

    @Unique
    private static Field resolveEntityField() {
        for (Field field : getTrackedEntityClass().getDeclaredFields()) {
            if (!Entity.class.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);
            return field;
        }

        SbwDroneRangeConfig.LOGGER.warn("Could not locate the tracked entity field in ChunkMap$TrackedEntity.");
        return null;
    }

    @Unique
    private static Class<?> getTrackedEntityClass() {
        try {
            return Class.forName("net.minecraft.server.level.ChunkMap$TrackedEntity");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Could not load ChunkMap$TrackedEntity for the SBW drone tracking mixin.", exception);
        }
    }
}
