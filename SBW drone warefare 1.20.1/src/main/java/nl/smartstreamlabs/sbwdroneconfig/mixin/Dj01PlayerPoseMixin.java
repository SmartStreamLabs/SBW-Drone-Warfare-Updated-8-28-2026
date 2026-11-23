package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(net.minecraft.client.model.HumanoidModel.class)
public abstract class Dj01PlayerPoseMixin {
    @Shadow public ModelPart head;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightArm;
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void dj01$hands(net.minecraft.world.entity.LivingEntity entity,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){
        if(entity instanceof net.minecraft.world.entity.player.Player p)nl.smartstreamlabs.sbwdroneconfig.Dj01Pose.pose(p,head,leftArm,rightArm);
    }
}
