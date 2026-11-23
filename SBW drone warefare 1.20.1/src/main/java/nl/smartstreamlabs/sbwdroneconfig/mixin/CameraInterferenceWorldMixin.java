package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.GameRenderer;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public abstract class CameraInterferenceWorldMixin {
    @Inject(method="renderLevel",at=@At("RETURN"))private void drone$interference(float partial,long nanos,com.mojang.blaze3d.vertex.PoseStack pose,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.CameraInterferenceRender.process(partial);}
}
