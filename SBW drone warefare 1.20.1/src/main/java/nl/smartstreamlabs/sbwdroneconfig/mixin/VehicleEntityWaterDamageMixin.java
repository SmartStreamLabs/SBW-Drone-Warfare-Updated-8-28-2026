package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import nl.smartstreamlabs.sbwdroneconfig.DroneWaterDisconnectSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SBW resolves final vehicle damage inside VehicleEntity.hurt before it decides
 * to destroy the vehicle. Catch water-contact damage here so drone water
 * touches always become a disconnect instead of reaching the normal destroy /
 * explosion flow.
 */
@Mixin(targets = "com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity")
public abstract class VehicleEntityWaterDamageMixin {
    @Inject(method = "m_6469_", at = @At("HEAD"), remap = false, require = 0, cancellable = true)
    private void sbwdroneconfig$disconnectDroneInsteadOfWaterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity vehicle = (Entity) (Object) this;
        if (vehicle.level() instanceof ServerLevel serverLevel
                && DroneWaterDisconnectSystem.handleWaterContact(serverLevel, vehicle)) {
            cir.setReturnValue(false);
        }
    }
}
