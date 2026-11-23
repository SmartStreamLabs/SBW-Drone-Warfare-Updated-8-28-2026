package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.Options;import nl.smartstreamlabs.sbwdroneconfig.DvtClient;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Options.class)public abstract class DvtRenderDistanceMixin {@Inject(method="getEffectiveRenderDistance",at=@At("HEAD"),cancellable=true)private void dvt$distance(CallbackInfoReturnable<Integer> ci){if(DvtClient.capturing())ci.setReturnValue(2);}}
