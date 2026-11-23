package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.minecraft.client.model.HumanoidModel.class,remap=false)
public abstract class Dj01PlayerPoseMixin {
    @Shadow(remap=false) public ModelPart head;
    @Shadow(remap=false) public ModelPart leftArm;
    @Shadow(remap=false) public ModelPart rightArm;
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"),remap=false)
    private void dj01$hands(net.minecraft.world.entity.LivingEntity entity,float walk,float amount,float age,float yaw,float pitch,CallbackInfo ci){
        if(entity instanceof net.minecraft.world.entity.player.Player p)nl.smartstreamlabs.sbwdroneconfig.Dj01Pose.pose(p,head,leftArm,rightArm);
    }
}
