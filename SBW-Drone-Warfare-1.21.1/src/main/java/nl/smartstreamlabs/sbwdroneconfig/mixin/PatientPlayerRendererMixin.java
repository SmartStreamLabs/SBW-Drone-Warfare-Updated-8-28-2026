package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.minecraft.client.renderer.entity.player.PlayerRenderer.class,remap=false)
public abstract class PatientPlayerRendererMixin {
 @Inject(method="setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",at=@At("HEAD"),cancellable=true)
 private void rescue$lying(net.minecraft.client.player.AbstractClientPlayer p,com.mojang.blaze3d.vertex.PoseStack stack,float bob,float body,float partial,float scale,CallbackInfo ci){if(p.getVehicle() instanceof nl.smartstreamlabs.sbwdroneconfig.MedicalTrailerEntity t){var offset=t.hitchRenderOffset(partial);stack.translate(offset.x,offset.y,offset.z);stack.mulPose(nl.smartstreamlabs.sbwdroneconfig.PatientPose.orientation(t.getVehicleTransform(partial)));ci.cancel();}}
}
