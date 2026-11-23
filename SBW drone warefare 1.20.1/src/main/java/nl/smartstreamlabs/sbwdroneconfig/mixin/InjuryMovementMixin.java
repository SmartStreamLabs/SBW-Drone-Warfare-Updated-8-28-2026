package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.InjuryHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class InjuryMovementMixin {
    @Inject(method="jumpFromGround",at=@At("HEAD"),cancellable=true)
    private void injury$jump(CallbackInfo c){if(InjuryHooks.downed((LivingEntity)(Object)this))c.cancel();}
    @Inject(method="jumpInLiquid",at=@At("HEAD"),cancellable=true)
    private void injury$swimJump(CallbackInfo c){if(InjuryHooks.downed((LivingEntity)(Object)this))c.cancel();}
    @ModifyVariable(method="setSprinting",at=@At("HEAD"),argsOnly=true)
    private boolean injury$sprint(boolean sprint){return sprint&&!InjuryHooks.downed((LivingEntity)(Object)this);}
}
