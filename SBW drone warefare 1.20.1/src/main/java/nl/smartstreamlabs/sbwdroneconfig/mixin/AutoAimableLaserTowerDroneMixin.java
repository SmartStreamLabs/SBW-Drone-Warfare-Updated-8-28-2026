package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import nl.smartstreamlabs.sbwdroneconfig.LaserTowerDroneDefenseSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.atsuishio.superbwarfare.entity.vehicle.base.AutoAimableEntity", remap = false)
public abstract class AutoAimableLaserTowerDroneMixin {
    @ModifyVariable(method = "autoAim", at = @At(value = "STORE"), ordinal = 0, require = 0)
    private double sbwdroneconfig$extendLaserTowerRange(double original) {
        return LaserTowerDroneDefenseSystem.extendSeekRange((Entity) (Object) this, original);
    }

    @Inject(method = "seekNearLivingEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void sbwdroneconfig$seekDrones(Vec3 pos, double minAngle, double maxAngle, double minRange, double seekRange, double size, CallbackInfoReturnable<Entity> cir) {
        Entity target = LaserTowerDroneDefenseSystem.findDroneTarget((Entity) (Object) this, pos, minAngle, maxAngle, minRange, seekRange, size);
        if (target != null) {
            cir.setReturnValue(target);
        }
    }
}
