package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import nl.smartstreamlabs.sbwdroneconfig.*;
/** FL-01 uses its supplied nose lens; the stock small-drone camera correction lands inside its airframe. */
@Mixin(value=net.minecraft.client.Camera.class,priority=1200)
public abstract class Fl01CameraMixin {
 @Unique private java.lang.ref.WeakReference<Fl01Entity> fl01$cached=new java.lang.ref.WeakReference<>(null);
 @Unique private java.util.UUID fl01$checked;@Unique private boolean fl01$other;@Unique private long fl01$lookup=-1;

 @Shadow protected abstract void setPosition(double x,double y,double z);
 @Shadow protected abstract void setRotation(float yaw,float pitch);
 @Inject(method="setup",at=@At(value="FIELD",target="Lnet/minecraft/client/Camera;detached:Z",opcode=181,shift=At.Shift.AFTER),cancellable=true)
 private void fl01$lens(net.minecraft.world.level.BlockGetter level,net.minecraft.world.entity.Entity entity,boolean detached,boolean mirror,float partial,CallbackInfo c){
  var m=net.minecraft.client.Minecraft.getInstance();if(PortableFpvDetectorRenderContext.isOffscreenRendering())return;
  var monitor=SbwCompat.getActiveLinkedMonitor(m.player);var id=SbwCompat.getLinkedDroneUuid(monitor);
  if(id==null||!SbwCompat.isUsingLinkedMonitor(monitor)){if(fl01$checked!=null){fl01$cached.clear();fl01$checked=null;fl01$other=false;}return;}
  if(!id.equals(fl01$checked)){fl01$checked=id;fl01$other=false;fl01$lookup=-1;fl01$cached.clear();}if(fl01$other)return;
  Fl01Entity h=fl01$cached.get();if(h==null||h.level()!=m.level||h.isRemoved()){
   if(m.level==null||fl01$lookup==m.level.getGameTime())return;fl01$lookup=m.level.getGameTime();var target=DroneClientViewContext.activeDroneEntity(m);
   if(!(target instanceof Fl01Entity value)){fl01$other=target!=null;return;}h=value;fl01$cached=new java.lang.ref.WeakReference<>(h);
  }
  if(!SbwCompat.isUsingLinkedMonitorForDrone(m.player,h))return;
  var at=h.getCameraPosition(partial,m.player,detached,mirror);var angles=h.getCameraRotation(partial,m.player,detached,mirror);setRotation(angles.x,angles.y);setPosition(at.x,at.y,at.z);c.cancel();
 }
}
