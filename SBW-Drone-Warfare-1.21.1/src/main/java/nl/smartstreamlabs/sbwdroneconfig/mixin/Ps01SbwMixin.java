package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** The native fast projectile path does not always publish Forge ProjectileImpactEvent. */
@Mixin(targets="com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile",remap=false)
public abstract class Ps01SbwMixin{
 @Inject(method={"onHit","m_6532_"},at=@At("HEAD"),require=1)
 private void ps01$impact(net.minecraft.world.phys.HitResult hit,CallbackInfo ci){if(hit instanceof net.minecraft.world.phys.BlockHitResult block)nl.smartstreamlabs.sbwdroneconfig.Ps01Compat.impact((net.minecraft.world.entity.Entity)(Object)this,block);}
}
