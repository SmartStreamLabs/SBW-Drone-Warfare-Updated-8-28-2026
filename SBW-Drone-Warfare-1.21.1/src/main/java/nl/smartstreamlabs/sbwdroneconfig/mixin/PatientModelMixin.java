package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import net.minecraft.client.model.geom.ModelPart;
@Mixin(value=net.minecraft.client.model.HumanoidModel.class,remap=false)
public abstract class PatientModelMixin {
 @Shadow public ModelPart head;@Shadow public ModelPart hat;@Shadow public ModelPart body;@Shadow public ModelPart leftArm;@Shadow public ModelPart rightArm;@Shadow public ModelPart leftLeg;@Shadow public ModelPart rightLeg;
 @Unique private boolean rescue$headMoved;@Unique private float rescue$headZ,rescue$hatZ;
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("HEAD"))
 private void rescue$restore(net.minecraft.world.entity.LivingEntity p,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){if(rescue$headMoved){head.z=rescue$headZ;hat.z=rescue$hatZ;rescue$headMoved=false;}}
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
 private void rescue$straight(net.minecraft.world.entity.LivingEntity p,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){if(p instanceof net.minecraft.world.entity.player.Player&&p.getVehicle() instanceof nl.smartstreamlabs.sbwdroneconfig.MedicalTrailerEntity){for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg}){part.xRot=0;part.yRot=0;part.zRot=0;}rescue$headZ=head.z;rescue$hatZ=hat.z;head.z-=nl.smartstreamlabs.sbwdroneconfig.PatientPose.HEAD_LIFT_PIXELS;hat.z-=nl.smartstreamlabs.sbwdroneconfig.PatientPose.HEAD_LIFT_PIXELS;rescue$headMoved=true;leftArm.zRot=.05f;rightArm.zRot=-.05f;}}
}
