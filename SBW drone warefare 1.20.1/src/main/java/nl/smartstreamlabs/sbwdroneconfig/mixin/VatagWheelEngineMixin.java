package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;import nl.smartstreamlabs.sbwdroneconfig.VatagEntity;
/** Only bypass the empty-seat input reset for an authenticated VATAG session. No passenger is fabricated. */
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils",remap=false)
public abstract class VatagWheelEngineMixin {
 @Redirect(method="wheelEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setForwardInputDown(Z)V",ordinal=1),require=1)
 private static void vatag$ForwardInputDown(VehicleEntity e,boolean value){if(!(e instanceof VatagEntity v)||!v.remotelyControlled())e.setForwardInputDown(value);}
 @Redirect(method="wheelEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setBackInputDown(Z)V",ordinal=1),require=1)
 private static void vatag$BackInputDown(VehicleEntity e,boolean value){if(!(e instanceof VatagEntity v)||!v.remotelyControlled())e.setBackInputDown(value);}
 @Redirect(method="wheelEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setLeftInputDown(Z)V",ordinal=1),require=1)
 private static void vatag$LeftInputDown(VehicleEntity e,boolean value){if(!(e instanceof VatagEntity v)||!v.remotelyControlled())e.setLeftInputDown(value);}
 @Redirect(method="wheelEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setRightInputDown(Z)V",ordinal=1),require=1)
 private static void vatag$RightInputDown(VehicleEntity e,boolean value){if(!(e instanceof VatagEntity v)||!v.remotelyControlled())e.setRightInputDown(value);}
 @Redirect(method="wheelEngine",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;setPower(F)V",ordinal=1),require=1)
 private static void vatag$Power(VehicleEntity e,float value){if(!(e instanceof VatagEntity v)||!v.remotelyControlled())e.setPower(value);}
}
