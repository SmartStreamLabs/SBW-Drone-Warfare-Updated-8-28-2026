package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.Entity;import net.minecraft.nbt.CompoundTag;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;import nl.smartstreamlabs.sbwdroneconfig.MoistureSystem;
@Mixin(value=Entity.class,remap=false)public abstract class MoistureEntityMixin {
 @Inject(method="baseTick",at=@At("TAIL"))private void moisture$tick(CallbackInfo c){MoistureSystem.tick((Entity)(Object)this);}
 @Inject(method="saveWithoutId",at=@At("RETURN"))private void moisture$save(CompoundTag n,CallbackInfoReturnable<CompoundTag> c){MoistureSystem.save((Entity)(Object)this,n);}
 @Inject(method="load",at=@At("RETURN"))private void moisture$load(CompoundTag n,CallbackInfo c){MoistureSystem.load((Entity)(Object)this,n);}
}
