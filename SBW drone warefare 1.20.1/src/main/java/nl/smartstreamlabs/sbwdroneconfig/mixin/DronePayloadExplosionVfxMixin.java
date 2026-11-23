package nl.smartstreamlabs.sbwdroneconfig.mixin;

import com.atsuishio.superbwarfare.tools.ParticleTool;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import nl.smartstreamlabs.sbwdroneconfig.DroneExplosionVfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Same call descriptor verified in 0.8.9 Final, 0.8.9.1 Forge and 0.8.9.1 NeoForge. */
@Mixin(targets = "com.atsuishio.superbwarfare.tools.CustomExplosion$Builder", remap = false)
public abstract class DronePayloadExplosionVfxMixin {
    @Shadow private Entity directSource;
    @Shadow private Vec3 position;
    @Shadow private float radius;

    // This is the last operation, AFTER actual explode/finalize and damage processing.
    // No thread-local scopes, no speculative effects when kamikazeExplosion aborts.
    @Redirect(method = "explode", at = @At(value = "INVOKE", target =
            "Lcom/atsuishio/superbwarfare/tools/ParticleTool;spawnExplosionParticles(Lcom/atsuishio/superbwarfare/tools/ParticleTool$ParticleType;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)V"), require = 1)
    private void sbwdroneconfig$payloadVisuals(ParticleTool.ParticleType type, Level level, Vec3 particlePosition) {
        // One confirmed finalize event owns all SBW visuals/audio, including grenades.
        // Never stack the original SBW particle burst over the replacement.
        if(!(level instanceof ServerLevel))ParticleTool.spawnExplosionParticles(type,level,particlePosition);
    }
}
