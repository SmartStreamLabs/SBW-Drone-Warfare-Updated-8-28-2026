package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.Wd01Entity;import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils",remap=false)
public abstract class Wd01ShipEngineMixin {
 @Inject(method="shipEngine",at=@At("HEAD")) private static void wd01$before(VehicleEntity e,com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo.Ship info,CallbackInfo ci){if(e instanceof Wd01Entity w)w.beforeShip();}
 @Inject(method="shipEngine",at=@At("TAIL")) private static void wd01$after(VehicleEntity e,com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo.Ship info,CallbackInfo ci){if(e instanceof Wd01Entity w)w.afterShip();}
 @Redirect(method="shipEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setForwardInputDown(Z)V",ordinal=1))
 private static void wd01$forward(VehicleEntity e,boolean v){if(!(e instanceof Wd01Entity w)||!w.remotelyControlled())e.setForwardInputDown(v);}
 @Redirect(method="shipEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setBackInputDown(Z)V",ordinal=1))
 private static void wd01$back(VehicleEntity e,boolean v){if(!(e instanceof Wd01Entity w)||!w.remotelyControlled())e.setBackInputDown(v);}
 @Redirect(method="shipEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setLeftInputDown(Z)V",ordinal=0))
 private static void wd01$left(VehicleEntity e,boolean v){if(!(e instanceof Wd01Entity w)||!w.remotelyControlled())e.setLeftInputDown(v);}
 @Redirect(method="shipEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setRightInputDown(Z)V",ordinal=0))
 private static void wd01$right(VehicleEntity e,boolean v){if(!(e instanceof Wd01Entity w)||!w.remotelyControlled())e.setRightInputDown(v);}
 // Replace the native unbounded cloud/bubble spray for this hull only. WD wake is capped and uses its authored anchors.
 @Redirect(method="shipEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;addRandomParticle(Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/world/phys/Vec3;FLnet/minecraft/world/level/Level;FI)V"))
 private static void wd01$wake(VehicleEntity e,net.minecraft.core.particles.ParticleOptions particle,net.minecraft.world.phys.Vec3 at,float radius,net.minecraft.world.level.Level level,float speed,int count){if(!(e instanceof Wd01Entity))e.addRandomParticle(particle,at,radius,level,speed,count);}
}
