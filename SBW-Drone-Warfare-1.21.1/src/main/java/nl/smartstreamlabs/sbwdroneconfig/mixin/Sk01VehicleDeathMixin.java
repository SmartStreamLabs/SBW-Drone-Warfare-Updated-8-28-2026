package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.Entity;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import nl.smartstreamlabs.sbwdroneconfig.Sk01Deaths;
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity",remap=false)
public abstract class Sk01VehicleDeathMixin {
 @Unique private Sk01Deaths.Snapshot sk01$death;
 @Inject(method="destroy",at=@At("HEAD"))private void sk01$start(CallbackInfo ci){var e=(Entity)(Object)this;sk01$death=e.isAddedToLevel()?Sk01Deaths.capture(e):null;}
 @Inject(method="destroy",at=@At("TAIL"))private void sk01$end(CallbackInfo ci){if(sk01$death!=null)Sk01Deaths.commit((Entity)(Object)this,sk01$death);sk01$death=null;}
}
