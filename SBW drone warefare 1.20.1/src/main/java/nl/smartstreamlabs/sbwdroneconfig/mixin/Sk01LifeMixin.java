package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.LivingEntity;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public abstract class Sk01LifeMixin {
 @Inject(method="setHealth",at=@At("TAIL"))private void sk01$revived(float amount,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.Sk01Deaths.recovered((LivingEntity)(Object)this);}
}
