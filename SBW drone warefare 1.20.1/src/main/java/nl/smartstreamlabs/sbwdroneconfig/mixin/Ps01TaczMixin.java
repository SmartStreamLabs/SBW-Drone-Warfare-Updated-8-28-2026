package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Optional TaCZ adapter; no hard dependency or changes to its bullet/penetration logic. */
@Pseudo @Mixin(targets="com.tacz.guns.entity.EntityKineticBullet",remap=false)
public abstract class Ps01TaczMixin{
 @Inject(method="onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)V",at=@At("HEAD"),require=0)
 private void ps01$hit(net.minecraft.world.phys.BlockHitResult hit,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.Ps01Compat.impact((net.minecraft.world.entity.Entity)(Object)this,hit);}
 @Inject(method={"onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V","m_8060_(Lnet/minecraft/world/phys/BlockHitResult;)V"},at=@At("HEAD"),require=0)
 private void ps01$legacy(net.minecraft.world.phys.BlockHitResult hit,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.Ps01Compat.impact((net.minecraft.world.entity.Entity)(Object)this,hit);}
}
