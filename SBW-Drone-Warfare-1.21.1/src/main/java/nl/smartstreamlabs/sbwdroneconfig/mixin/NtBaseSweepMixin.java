package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.*;import net.minecraft.world.phys.Vec3;import nl.smartstreamlabs.sbwdroneconfig.NtCollision;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** SBW 0.8.9.1 delegates to vanilla move when no OBB info is installed. */
@Mixin(value=Entity.class,remap=false) public abstract class NtBaseSweepMixin {
 @Unique private NtCollision.Contact nt01$vanillaContact;
 @Unique private nl.smartstreamlabs.sbwdroneconfig.Vc01Movement.Contact vc01$contact;
 @ModifyVariable(method="move",at=@At("HEAD"),argsOnly=true) private Vec3 nt01$vanillaClip(Vec3 motion){nt01$vanillaContact=NtCollision.before((Entity)(Object)this,motion);Vec3 allowed=nt01$vanillaContact==null?motion:nt01$vanillaContact.movement();vc01$contact=nl.smartstreamlabs.sbwdroneconfig.Vc01Movement.before((Entity)(Object)this,allowed);return vc01$contact==null?allowed:vc01$contact.movement();}
 @Inject(method="move",at=@At("RETURN")) private void nt01$vanillaReact(MoverType mover,Vec3 motion,CallbackInfo ci){var c=nt01$vanillaContact;nt01$vanillaContact=null;NtCollision.after((Entity)(Object)this,c);var hit=vc01$contact;vc01$contact=null;nl.smartstreamlabs.sbwdroneconfig.Vc01Movement.after((Entity)(Object)this,hit);}
 @Inject(method="baseTick",at=@At("RETURN")) private void nt01$sweep(CallbackInfo ci){NtCollision.fallback((Entity)(Object)this);nl.smartstreamlabs.sbwdroneconfig.VehicleAntiDroneNetCollisionSystem.observe((Entity)(Object)this);}
}
