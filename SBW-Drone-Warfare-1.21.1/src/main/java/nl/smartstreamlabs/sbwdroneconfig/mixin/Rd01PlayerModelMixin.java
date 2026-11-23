package nl.smartstreamlabs.sbwdroneconfig.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(value=net.minecraft.client.model.HumanoidModel.class,priority=900,remap=false)
public abstract class Rd01PlayerModelMixin {
    @Shadow public ModelPart body;@Shadow public ModelPart leftArm;@Shadow public ModelPart rightArm;@Shadow public ModelPart leftLeg;@Shadow public ModelPart rightLeg;
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void rd01$hang(net.minecraft.world.entity.LivingEntity p,float walk,float amount,float age,float yaw,float pitch,CallbackInfo c) {
        if(p instanceof net.minecraft.world.entity.player.Player&&p.getVehicle() instanceof nl.smartstreamlabs.sbwdroneconfig.Rd01Entity) {
            body.xRot=body.yRot=body.zRot=0;
            leftLeg.xRot=rightLeg.xRot=-1.05f;leftLeg.yRot=.08f;rightLeg.yRot=-.08f;leftLeg.zRot=rightLeg.zRot=0;
            leftArm.xRot=rightArm.xRot=-.18f;leftArm.zRot=-.12f;rightArm.zRot=.12f;
        }
    }
}
