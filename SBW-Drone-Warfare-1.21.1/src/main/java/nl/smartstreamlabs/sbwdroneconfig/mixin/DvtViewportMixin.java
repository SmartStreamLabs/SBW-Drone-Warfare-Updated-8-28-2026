package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.DvtClient;import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.GameRenderer;import com.mojang.blaze3d.systems.RenderSystem;import com.mojang.blaze3d.platform.Window;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.Shadow;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import org.spongepowered.asm.mixin.injection.*;
/** Scoped video aspect ratio and camera clipping, independent of the bounded terrain build radius. */
@Mixin(value=GameRenderer.class,remap=false)public abstract class DvtViewportMixin {
 @Redirect(method="getProjectionMatrix",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/Window;getWidth()I"))private int dvt$width(Window w){return DvtClient.capturing()?Minecraft.getInstance().getMainRenderTarget().viewWidth:w.getWidth();}
 @Redirect(method="getProjectionMatrix",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/Window;getHeight()I"))private int dvt$height(Window w){return DvtClient.capturing()?Minecraft.getInstance().getMainRenderTarget().viewHeight:w.getHeight();}
 @Shadow private float renderDistance;
 @Inject(method="renderLevel",at=@At(value="FIELD",target="Lnet/minecraft/client/renderer/GameRenderer;renderDistance:F",opcode=181,shift=At.Shift.AFTER))private void dvt$clip(CallbackInfo ci){if(DvtClient.capturing())renderDistance=Math.max(64,Math.min(256,Minecraft.getInstance().options.renderDistance().get()*16));}
}
