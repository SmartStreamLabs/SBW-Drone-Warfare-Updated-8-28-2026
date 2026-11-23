package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;import net.minecraft.world.entity.Entity;import net.minecraft.world.entity.player.Player;import net.minecraft.world.phys.*;import nl.smartstreamlabs.sbwdroneconfig.*;
@Mixin(Entity.class)
public abstract class PatientBodyMixin {
 @Unique private boolean rescue$wasPatient;
 @Unique private MedicalTrailerEntity rescue$trailer(){var e=(Entity)(Object)this;return e instanceof Player&&e.getVehicle() instanceof MedicalTrailerEntity t?t:null;}
 @Inject(method="makeBoundingBox",at=@At("HEAD"),cancellable=true)
 private void rescue$bounds(CallbackInfoReturnable<AABB> ci){var t=rescue$trailer();if(t!=null)ci.setReturnValue(PatientPose.bounds(t.getVehicleTransform(1)));}
 @Inject(method="getEyePosition()Lnet/minecraft/world/phys/Vec3;",at=@At("HEAD"),cancellable=true)
 private void rescue$eye(CallbackInfoReturnable<Vec3> ci){var t=rescue$trailer();if(t!=null)ci.setReturnValue(t.patientCamera());}
 @Inject(method="getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",at=@At("HEAD"),cancellable=true)
 private void rescue$eyePartial(float partial,CallbackInfoReturnable<Vec3> ci){var t=rescue$trailer();if(t!=null)ci.setReturnValue(t.patientCamera());}
 @Inject(method="getEyeHeight()F",at=@At("HEAD"),cancellable=true)
 private void rescue$eyeHeight(CallbackInfoReturnable<Float> ci){var t=rescue$trailer();if(t!=null)ci.setReturnValue((float)(t.patientCamera().y-((Entity)(Object)this).getY()));}
 @Inject(method="stopRiding",at=@At("HEAD"))private void rescue$remember(CallbackInfo ci){rescue$wasPatient=rescue$trailer()!=null;}
 @Inject(method="stopRiding",at=@At("RETURN"))private void rescue$restoreBounds(CallbackInfo ci){if(rescue$wasPatient&&rescue$trailer()==null){var e=(Entity)(Object)this;e.setBoundingBox(e.getDimensions(e.getPose()).makeBoundingBox(e.position()));}rescue$wasPatient=false;}
}
