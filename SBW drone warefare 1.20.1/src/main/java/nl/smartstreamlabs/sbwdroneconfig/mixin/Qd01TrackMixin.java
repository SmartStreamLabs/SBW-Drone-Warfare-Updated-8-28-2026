package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;import nl.smartstreamlabs.sbwdroneconfig.Qd01Entity;
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils",remap=false)
public abstract class Qd01TrackMixin {
 @Inject(method="trackEngine",at=@At("HEAD"),require=1)private static void qd01$brake(VehicleEntity v,com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo.Track info,CallbackInfo ci){if(v instanceof Qd01Entity q&&!v.level().isClientSide)q.beforeTrack();}
}
