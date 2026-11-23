package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;import net.minecraft.world.entity.*;import net.minecraft.world.item.*;import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.renderer.MultiBufferSource;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import nl.smartstreamlabs.sbwdroneconfig.*;
/** SBW hides all left-arm guns. Render only DIL's dominant-hand model before that generic rule. */
@Mixin(value=PlayerItemInHandLayer.class)
public abstract class Dil01HeldLayerMixin {
 @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)private void dil01$held(LivingEntity entity,ItemStack stack,ItemDisplayContext display,HumanoidArm arm,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci){if(!(entity instanceof net.minecraft.world.entity.player.Player p)||!Dil01Item.is(stack))return;ci.cancel();if(arm!=p.getMainArm()||p.getVehicle() instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.banHand(p))return;Dil01Pose.third(p,stack,pose,buffers,light);}
}
