package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Explosion.class,remap=false)
public abstract class RubbleExplosionMixin {
    @Inject(method="explode",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/event/EventHooks;onExplosionDetonate(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/Explosion;Ljava/util/List;D)V",remap=false))
    private void rubble$candidates(CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.RubbleEvents.prepare((Explosion)(Object)this);}
    @Inject(method="finalizeExplosion",at=@At("HEAD"))
    private void rubble$finish(boolean particles,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.RubbleEvents.finish((Explosion)(Object)this);}
}
