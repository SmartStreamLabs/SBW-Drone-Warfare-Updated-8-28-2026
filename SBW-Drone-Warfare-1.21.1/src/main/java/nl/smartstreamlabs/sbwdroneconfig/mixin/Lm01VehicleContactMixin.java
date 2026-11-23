package nl.smartstreamlabs.sbwdroneconfig.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.phys.Vec3;

@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity",remap=false)
public abstract class Lm01VehicleContactMixin {
 @Unique private Vec3 lm01$previous;
 @Inject(method={"baseTick","m_6075_"},at=@At("TAIL"),require=1)
 private void lm01$groundContact(CallbackInfo ci){var v=(com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)(Object)this;if(v.level().isClientSide)return;nl.smartstreamlabs.sbwdroneconfig.Lm01Contact.vehicle(v,lm01$previous);lm01$previous=v.position();}
}
