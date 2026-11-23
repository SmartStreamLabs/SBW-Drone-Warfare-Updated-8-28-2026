package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=net.minecraft.client.MouseHandler.class,remap=false)
public abstract class VatagMouseMixin {
 @Shadow(remap=false) private double accumulatedDX;@Shadow(remap=false) private double accumulatedDY;
 @Inject(method="turnPlayer",at=@At("HEAD"),cancellable=true,remap=false)
 private void vatag$look(CallbackInfo ci){if(nl.smartstreamlabs.sbwdroneconfig.Rd01Client.active()){nl.smartstreamlabs.sbwdroneconfig.Rd01Client.look(accumulatedDX,accumulatedDY);accumulatedDX=0;accumulatedDY=0;ci.cancel();return;}if(nl.smartstreamlabs.sbwdroneconfig.Id01Client.active()){nl.smartstreamlabs.sbwdroneconfig.Id01Client.look(accumulatedDX,accumulatedDY);accumulatedDX=0;accumulatedDY=0;ci.cancel();return;}if(nl.smartstreamlabs.sbwdroneconfig.VatagClient.active()){nl.smartstreamlabs.sbwdroneconfig.VatagClient.look(accumulatedDX,accumulatedDY);accumulatedDX=0;accumulatedDY=0;ci.cancel();}}
}
