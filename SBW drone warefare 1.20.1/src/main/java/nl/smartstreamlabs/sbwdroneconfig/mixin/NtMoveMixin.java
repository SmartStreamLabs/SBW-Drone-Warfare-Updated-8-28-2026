package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.*;import net.minecraft.world.phys.Vec3;import nl.smartstreamlabs.sbwdroneconfig.NtCollision;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity",remap=false)
public abstract class NtMoveMixin {
 @Unique private NtCollision.Contact nt01$contact;
 @Unique private nl.smartstreamlabs.sbwdroneconfig.Vc01Movement.Contact vc01$contact;
 @ModifyVariable(method="vMove",at=@At("HEAD"),argsOnly=true) private Vec3 nt01$clip(Vec3 motion){nt01$contact=NtCollision.before((Entity)(Object)this,motion);Vec3 allowed=nt01$contact==null?motion:nt01$contact.movement();vc01$contact=nl.smartstreamlabs.sbwdroneconfig.Vc01Movement.before((Entity)(Object)this,allowed);return vc01$contact==null?allowed:vc01$contact.movement();}
 @Inject(method="vMove",at=@At("RETURN")) private void nt01$react(MoverType mover,Vec3 motion,CallbackInfo ci){var c=nt01$contact;nt01$contact=null;NtCollision.after((Entity)(Object)this,c);var hit=vc01$contact;vc01$contact=null;nl.smartstreamlabs.sbwdroneconfig.Vc01Movement.after((Entity)(Object)this,hit);}
}
