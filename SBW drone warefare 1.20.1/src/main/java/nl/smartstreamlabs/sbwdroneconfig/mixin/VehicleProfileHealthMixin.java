package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.Entity;
import nl.smartstreamlabs.sbwdroneconfig.VehicleProfiles;
/** Native vehicle defaults remain authoritative; the profile is applied once at the virtual health boundary. */
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity",remap=false)
public abstract class VehicleProfileHealthMixin {
 @Inject(method="getMaxHealth",at=@At("RETURN"),cancellable=true,require=1)
 private void droneWarfare$profileHealth(CallbackInfoReturnable<Float> reply){reply.setReturnValue(VehicleProfiles.health((Entity)(Object)this,reply.getReturnValue()));}
}
