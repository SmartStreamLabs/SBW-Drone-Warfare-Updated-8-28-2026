package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import nl.smartstreamlabs.sbwdroneconfig.DroneCrashExplosionSystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneExplosionVfx;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
        if(nl.smartstreamlabs.sbwdroneconfig.Vc01Intercept.triggered(drone)){ci.cancel();return;}
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


    @Redirect(method = "destroy", remap = false, at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"), require = 1)
    private Explosion sbwdroneconfig$initialBurst(Level level, Entity source, double x, double y, double z, float radius, Level.ExplosionInteraction interaction) {
        try (var scope=DroneExplosionVfx.explosionScope(level,source,x,y,z,radius,DroneExplosionVfx.isAddonEntity((Entity)(Object)this))) {
            return level.explode(source,x,y,z,radius,interaction);
        }
    }

    @ModifyArg(method = "droneDrop", remap = false, at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), index = 0, require = 1)
    private Entity sbwdroneconfig$attributeDrop(Entity payload) {
        if (DroneExplosionVfx.isAddonEntity((Entity)(Object)this)) {DroneExplosionVfx.markPayload(payload);nl.smartstreamlabs.sbwdroneconfig.DroneAttackDamage.mark(payload,(Entity)(Object)this,net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(nl.smartstreamlabs.sbwdroneconfig.SbwCompat.getDroneCurrentItem((Entity)(Object)this).getItem()).toString());}
        return payload;
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
