package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import nl.smartstreamlabs.sbwdroneconfig.*;
/** HX's central mesh origin must not inherit the stock DroneEntity camera-height correction. */
@Mixin(value=net.minecraft.client.Camera.class,priority=1200,remap=false)
public abstract class Hx2CameraMixin {
 @Unique private java.lang.ref.WeakReference<Hx2Entity> hx2$cached=new java.lang.ref.WeakReference<>(null);
 @Unique private java.util.UUID hx2$checked;@Unique private boolean hx2$other;@Unique private long hx2$lookup=-1;

 @Shadow protected abstract void setPosition(double x,double y,double z);
 @Shadow protected abstract void setRotation(float yaw,float pitch);
 @Inject(method="setup",at=@At(value="FIELD",target="Lnet/minecraft/client/Camera;partialTickTime:F",opcode=181,shift=At.Shift.AFTER),cancellable=true)
 private void hx2$lens(net.minecraft.world.level.BlockGetter level,net.minecraft.world.entity.Entity entity,boolean detached,boolean mirror,float partial,CallbackInfo c){
  var m=net.minecraft.client.Minecraft.getInstance();if(PortableFpvDetectorRenderContext.isOffscreenRendering())return;
  var monitor=SbwCompat.getActiveLinkedMonitor(m.player);var id=SbwCompat.getLinkedDroneUuid(monitor);
  if(id==null||!SbwCompat.isUsingLinkedMonitor(monitor)){if(hx2$checked!=null){hx2$cached.clear();hx2$checked=null;hx2$other=false;}return;}
  if(!id.equals(hx2$checked)){hx2$checked=id;hx2$other=false;hx2$lookup=-1;hx2$cached.clear();}if(hx2$other)return;
  Hx2Entity h=hx2$cached.get();if(h==null||h.level()!=m.level||h.isRemoved()){
   if(m.level==null||hx2$lookup==m.level.getGameTime())return;hx2$lookup=m.level.getGameTime();var target=DroneClientViewContext.activeDroneEntity(m);
   if(!(target instanceof Hx2Entity value)){hx2$other=target!=null;return;}h=value;hx2$cached=new java.lang.ref.WeakReference<>(h);
  }
  if(!SbwCompat.isUsingLinkedMonitorForDrone(m.player,h))return;
  var at=h.getCameraPosition(partial,m.player,detached,mirror);var angles=h.getCameraRotation(partial,m.player,detached,mirror);setRotation(angles.x,angles.y);setPosition(at.x,at.y,at.z);c.cancel();
 }
}
