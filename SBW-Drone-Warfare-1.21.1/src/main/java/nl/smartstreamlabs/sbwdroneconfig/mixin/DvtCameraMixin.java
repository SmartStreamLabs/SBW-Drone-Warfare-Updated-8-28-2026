package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.*;import net.minecraft.client.Camera;import net.minecraft.world.entity.Entity;import net.minecraft.world.level.BlockGetter;import net.minecraft.world.phys.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Same first-person lens/orientation as the existing pilot camera, without fabricating a monitor. */
@Mixin(value=Camera.class,priority=2000,remap=false)public abstract class DvtCameraMixin {
 @Shadow protected abstract void setPosition(double x,double y,double z);@Shadow protected abstract void setRotation(float yaw,float pitch);
 @Inject(method="setup",at=@At(value="FIELD",target="Lnet/minecraft/client/Camera;detached:Z",opcode=181,shift=At.Shift.AFTER),cancellable=true)
 private void dvt$lens(BlockGetter level,Entity entity,boolean third,boolean inverse,float partial,CallbackInfo ci){
  if(!DvtClient.capturing()||!DroneClientViewContext.isReadOnlyCameraIntercept(entity))return;
  if(entity instanceof Sd01Entity sd){var f=sd.cameraFrame(partial);var v=f.transformPosition(new org.joml.Vector3d());var d=f.transformDirection(new org.joml.Vector3d(0,0,1)).normalize();setPosition(v.x,v.y,v.z);setRotation((float)Math.toDegrees(-Math.atan2(d.x,d.z)),(float)-Math.toDegrees(Math.asin(d.y)));ci.cancel();return;}
  if(entity instanceof Gad01Entity g){var lens=g.camera(false,partial);var direction=g.getVehicleTransform(partial).transformDirection(new org.joml.Vector3d(0,0,1)).normalize();setPosition(lens.x+direction.x*.06,lens.y+direction.y*.06,lens.z+direction.z*.06);setRotation((float)Math.toDegrees(-Math.atan2(direction.x,direction.z)),(float)-Math.toDegrees(Math.asin(direction.y)));ci.cancel();return;}
  if(!(entity instanceof com.atsuishio.superbwarfare.entity.vehicle.DroneEntity d))return;
  Vec3 lens;float yaw=d.getYaw(partial),pitch=d.getPitch(partial);
  if(d instanceof Hx2Entity h){lens=h.getCameraPosition(partial,net.minecraft.client.Minecraft.getInstance().player,false,false);var a=h.getCameraRotation(partial,net.minecraft.client.Minecraft.getInstance().player,false,false);yaw=a.x;pitch=a.y;}
  else if(d instanceof Fl01Entity f){lens=f.getCameraPosition(partial,net.minecraft.client.Minecraft.getInstance().player,false,false);var a=f.getCameraRotation(partial,net.minecraft.client.Minecraft.getInstance().player,false,false);yaw=a.x;pitch=a.y;}
  else if(d instanceof Rd01Entity r)lens=Rd01Pose.lens(r,partial);
  else if(d instanceof Id01Entity i)lens=Id01Pose.lens(i,partial);
  else if(d instanceof CubedFpvDroneEntity f)lens=CubedFpvCameraViewMath.fpvCameraPosition(f,partial);
  else{var point=new org.joml.Vector4d(0,.075,.18,1).mul(d.getClientVehicleTransform(partial));lens=new Vec3(point.x,point.y,point.z);}
  setPosition(lens.x,lens.y,lens.z);setRotation(yaw,pitch);ci.cancel();
 }
}
