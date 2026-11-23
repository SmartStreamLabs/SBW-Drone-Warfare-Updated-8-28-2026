package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import nl.smartstreamlabs.sbwdroneconfig.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Attribute the actual payload and move its cancellable Start event ahead of native damage. */
@Mixin(targets="com.atsuishio.superbwarfare.tools.CustomExplosion$Builder",remap=false)
public abstract class RubbleBuilderMixin {
    @Shadow private Entity directSource;
    @Unique private boolean rubble$ours,rubble$cancelled;
    @Redirect(method="explode",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/tools/CustomExplosion;explode()V",remap=true))
    private void rubble$dispatch(CustomExplosion e){
        rubble$ours=DroneExplosionVfx.isAttributed(directSource);rubble$cancelled=false;
        if(rubble$ours){RubbleEvents.markPayload(e,directSource);rubble$cancelled=net.minecraftforge.event.ForgeEventFactory.onExplosionStart(directSource.level(),e);}
        if(!rubble$cancelled)e.explode();
    }
    @Redirect(method="explode",at=@At(value="INVOKE",target="Lnet/minecraftforge/event/ForgeEventFactory;onExplosionStart(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/Explosion;)Z"))
    private boolean rubble$start(Level l,Explosion e){return !rubble$ours&&net.minecraftforge.event.ForgeEventFactory.onExplosionStart(l,e);}
    @Inject(method="explode",at=@At(value="INVOKE",target="Lcom/atsuishio/superbwarfare/tools/CustomExplosion;finalizeExplosion(Z)V",remap=true),cancellable=true)
    private void rubble$abort(CallbackInfo ci){if(rubble$cancelled)ci.cancel();}
}
