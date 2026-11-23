package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(com.atsuishio.superbwarfare.tools.CustomExplosion.class)
public abstract class RubbleCustomExplosionMixin implements nl.smartstreamlabs.sbwdroneconfig.Lm01ExplosionPolicy {
    @org.spongepowered.asm.mixin.Unique private boolean lm01$noTerrain;
    @Override public void lm01$denyTerrain(){lm01$noTerrain=true;}
    @Override public boolean lm01$terrainDenied(){return lm01$noTerrain;}

    @org.spongepowered.asm.mixin.injection.Redirect(method="explode",at=@At(value="INVOKE",target="Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;get()Ljava/lang/Object;",remap=false))
    private Object rubble$nativeBlockPass(net.minecraftforge.common.ForgeConfigSpec.BooleanValue value){if(value==com.atsuishio.superbwarfare.config.server.ExplosionConfig.EXPLOSION_DESTROY&&(lm01$noTerrain||nl.smartstreamlabs.sbwdroneconfig.RubbleEvents.owns((Explosion)(Object)this)))return false;return value.get();}
    @Inject(method="processBlockList",remap=false,at=@At("HEAD"),cancellable=true,require=0)
    private void rubble$stage(java.util.Collection<net.minecraft.core.BlockPos> blocks,CallbackInfo ci){if(lm01$noTerrain||nl.smartstreamlabs.sbwdroneconfig.RubbleEvents.stage((Explosion)(Object)this,blocks))ci.cancel();}
    @Inject(method="explode",at=@At(value="INVOKE",target="Lnet/minecraftforge/event/ForgeEventFactory;onExplosionDetonate(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/Explosion;Ljava/util/List;D)V",remap=false))
    private void rubble$candidates(CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.RubbleEvents.prepare((Explosion)(Object)this);}
    @Inject(method="finalizeExplosion",at=@At("HEAD"))
    private void rubble$finish(boolean particles,CallbackInfo ci){nl.smartstreamlabs.sbwdroneconfig.RubbleEvents.finish((Explosion)(Object)this);}
    @Inject(method="finalizeExplosion",at=@At("TAIL"))
    private void sbwdroneconfig$sharedVisuals(boolean particles,CallbackInfo ci){((nl.smartstreamlabs.sbwdroneconfig.ExplosionVisualOwner)(Object)this).sbwdroneconfig$confirmVisuals();}
}
