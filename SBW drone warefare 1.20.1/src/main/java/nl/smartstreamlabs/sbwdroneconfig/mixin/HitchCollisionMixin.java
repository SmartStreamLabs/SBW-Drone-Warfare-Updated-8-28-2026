package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleMotionUtils",remap=false)
public abstract class HitchCollisionMixin {
 @Inject(method="preventStacking$lambda$0",at=@At("HEAD"),cancellable=true)
 private static void rescue$partner(com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity a,com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity b,CallbackInfoReturnable<Boolean> ci){if(nl.smartstreamlabs.sbwdroneconfig.Dc01Docking.sameCarrier(a,b)||nl.smartstreamlabs.sbwdroneconfig.HitchJoint.paired(a,b)||nl.smartstreamlabs.sbwdroneconfig.Id01Entity.contactPair(a,b))ci.setReturnValue(false);}
}
