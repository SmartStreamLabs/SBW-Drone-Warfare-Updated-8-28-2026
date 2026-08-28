package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import nl.smartstreamlabs.sbwdroneconfig.DroneCrashExplosionSystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneWaterDisconnectSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(targets = "com.atsuishio.superbwarfare.entity.vehicle.DroneEntity")
public abstract class DroneEntityCrashExplosionMixin {
    @Unique
    private static final Field SBWDRONECONFIG_IS_KAMIKAZE_FIELD = findDroneField("IS_KAMIKAZE");

    @Inject(method = "destroy", at = @At("HEAD"), remap = false, require = 0, cancellable = true)
    private void sbwdroneconfig$restoreCrashExplosion(CallbackInfo ci) {
        Entity drone = (Entity) (Object) this;
        if (!(drone.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (DroneWaterDisconnectSystem.interceptWaterDestroy(serverLevel, drone)) {
            ci.cancel();
            return;
        }
        if (DroneWaterDisconnectSystem.shouldSkipDestroy(drone)) {
            ci.cancel();
            return;
        }
        if (isKamikaze(drone)) {
            return;
        }

        DroneCrashExplosionSystem.handleDroneDestroy(serverLevel, drone);
    }

    @Unique
    private static boolean isKamikaze(Entity self) {
        if (SBWDRONECONFIG_IS_KAMIKAZE_FIELD == null) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            net.minecraft.network.syncher.EntityDataAccessor<Boolean> accessor =
                    (net.minecraft.network.syncher.EntityDataAccessor<Boolean>) SBWDRONECONFIG_IS_KAMIKAZE_FIELD.get(null);
            return accessor != null && self.getEntityData().get(accessor);
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    @Unique
    private static Field findDroneField(String fieldName) {
        try {
            Class<?> droneClass = Class.forName("com.atsuishio.superbwarfare.entity.vehicle.DroneEntity");
            Field field = droneClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
