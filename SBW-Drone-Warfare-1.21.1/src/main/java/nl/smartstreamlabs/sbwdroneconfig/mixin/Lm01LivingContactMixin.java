package nl.smartstreamlabs.sbwdroneconfig.mixin;
@org.spongepowered.asm.mixin.Mixin(value=net.minecraft.world.entity.LivingEntity.class,remap=false)
public abstract class Lm01LivingContactMixin {
 @org.spongepowered.asm.mixin.injection.Inject(method="tick",at=@org.spongepowered.asm.mixin.injection.At("TAIL"))
 private void lm01$footPressure(org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback){nl.smartstreamlabs.sbwdroneconfig.Lm01Contact.living((net.minecraft.world.entity.LivingEntity)(Object)this);}
}
