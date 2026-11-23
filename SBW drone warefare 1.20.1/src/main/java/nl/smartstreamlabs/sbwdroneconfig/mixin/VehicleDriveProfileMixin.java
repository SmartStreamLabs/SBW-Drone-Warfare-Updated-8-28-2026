package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import nl.smartstreamlabs.sbwdroneconfig.*;
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity",remap=false)
public abstract class VehicleDriveProfileMixin {
 @Unique private EngineInfo droneWarfare$nativeEngine,droneWarfare$tunedEngine;
 @Unique private VehicleDriveConfig.Factors droneWarfare$engineFactors;
 @Inject(method="getEngineInfo",at=@At("RETURN"),cancellable=true,require=1)
 private void droneWarfare$tunedEngine(CallbackInfoReturnable<EngineInfo> reply){
  EngineInfo original=reply.getReturnValue();if(original==null)return;var factors=VehicleDriveConfig.factors((net.minecraft.world.entity.Entity)(Object)this);
  if(factors==VehicleDriveConfig.IDENTITY)return;
  if(original!=droneWarfare$nativeEngine||!factors.equals(droneWarfare$engineFactors)){droneWarfare$nativeEngine=original;droneWarfare$engineFactors=factors;droneWarfare$tunedEngine=VehicleDriveTuning.copy(original,factors);}
  reply.setReturnValue(droneWarfare$tunedEngine);
 }
}
