package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** SD-01 owns its configured lamp key during control; vanilla advancements must not steal the camera. */
@Mixin(value=net.minecraft.client.KeyboardHandler.class,remap=false)
public abstract class Sd01LightKeyMixin {
 @Inject(method="keyPress",remap=false,at=@At("HEAD"),cancellable=true)
 private void drone$lamp(long window,int key,int scan,int action,int modifiers,CallbackInfo ci){
  var m=Minecraft.getInstance();
  if(window==m.getWindow().getWindow()&&m.screen==null&&nl.smartstreamlabs.sbwdroneconfig.VatagClient.active()&&nl.smartstreamlabs.sbwdroneconfig.VatagClient.vehicle() instanceof nl.smartstreamlabs.sbwdroneconfig.Sd01Entity&&nl.smartstreamlabs.sbwdroneconfig.Sd01Client.lampKey(key,scan))ci.cancel();
 }
}
