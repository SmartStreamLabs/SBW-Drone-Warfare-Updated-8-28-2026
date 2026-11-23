package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Explosion Overhaul 0.2.3.0 consults this predicate before its direct crater writes.
 * Our virtual building cells belong to their durability controller, never to a terrain job. */
@Pseudo
@Mixin(targets="com.vinlanx.explosionoverhaul.ExplosionOverhaul",remap=false)
public abstract class ExplosionOverhaulBunkerMixin {
 @Inject(method="isBlockStateBlacklisted",at=@At("HEAD"),cancellable=true)
 private static void drone$structures(BlockState state,CallbackInfoReturnable<Boolean> result){
  if(state.getBlock() instanceof nl.smartstreamlabs.sbwdroneconfig.Pb01Block||state.getBlock() instanceof nl.smartstreamlabs.sbwdroneconfig.Ps01Block)result.setReturnValue(true);
 }
}
