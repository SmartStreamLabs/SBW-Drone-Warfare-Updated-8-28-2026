package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import nl.smartstreamlabs.sbwdroneconfig.PortableFpvDetectorRenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Vanilla outline targets retain window size; return to the real detector viewport after them. */
@Mixin(value=RenderTarget.class,remap=false)
public abstract class WhiteHotVideoViewportMixin {
 @Inject(method="bindWrite",at=@At("RETURN")) private void drone$videoViewport(boolean requested,CallbackInfo ci){
  if(PortableFpvDetectorRenderContext.isOffscreenRendering()&&(Object)this==Minecraft.getInstance().getMainRenderTarget()){
   RenderTarget target=(RenderTarget)(Object)this;RenderSystem.viewport(0,0,target.viewWidth,target.viewHeight);
  }
 }
}
