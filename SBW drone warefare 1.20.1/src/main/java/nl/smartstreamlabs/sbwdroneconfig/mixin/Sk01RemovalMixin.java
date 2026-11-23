package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.Entity;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class Sk01RemovalMixin {
 @Inject(method="remove",at=@At("HEAD"))private void sk01$remove(Entity.RemovalReason reason,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.Sk01Deaths.removed((Entity)(Object)this,reason);}
}
