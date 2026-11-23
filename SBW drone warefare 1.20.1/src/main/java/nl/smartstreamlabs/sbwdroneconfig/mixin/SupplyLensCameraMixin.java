package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import nl.smartstreamlabs.sbwdroneconfig.*;
/** The final Camera pose must use the lens, not vanilla's smoothed player eye height. */
@Mixin(value=net.minecraft.client.Camera.class,priority=1100)
public abstract class SupplyLensCameraMixin {
 @Shadow protected abstract void setPosition(double x,double y,double z);
 @Shadow protected abstract void setRotation(float yaw,float pitch);
 @Inject(method="setup",at=@At("TAIL"))
 private void rescue$lens(net.minecraft.world.level.BlockGetter level,net.minecraft.world.entity.Entity entity,boolean detached,boolean inverse,float partial,CallbackInfo ci){
  if(Rd01Client.active()&&Rd01Client.vehicle()!=null){var p=Rd01Client.viewPosition(partial);var a=Rd01Client.viewAngles(partial);setPosition(p.x,p.y,p.z);setRotation(a[0],a[1]);return;}
  if(Id01Client.active()&&Id01Client.vehicle()!=null){var p=Id01Client.viewPosition(partial);var a=Id01Client.viewAngles(partial);setPosition(p.x,p.y,p.z);setRotation(a[0],a[1]);return;}
  if(!VatagClient.active()||!VatagClient.groundCamera())return;
  var p=VatagClient.viewPosition(partial);var a=VatagClient.viewAngles(partial);setPosition(p.x,p.y,p.z);setRotation(a[0],a[1]);
 }
}
