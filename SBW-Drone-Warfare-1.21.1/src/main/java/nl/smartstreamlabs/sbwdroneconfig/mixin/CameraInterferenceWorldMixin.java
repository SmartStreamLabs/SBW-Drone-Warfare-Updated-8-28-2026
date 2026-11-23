package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.GameRenderer;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=GameRenderer.class,remap=false)
public abstract class CameraInterferenceWorldMixin {
    @Inject(method="renderLevel",at=@At("RETURN"))private void drone$interference(net.minecraft.client.DeltaTracker delta,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.CameraInterferenceRender.process(delta.getGameTimeDeltaPartialTick(false));}
}
