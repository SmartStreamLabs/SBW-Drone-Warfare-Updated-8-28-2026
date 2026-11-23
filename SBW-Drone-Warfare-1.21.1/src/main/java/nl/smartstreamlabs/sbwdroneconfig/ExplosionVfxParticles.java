package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
public final class ExplosionVfxParticles {
    public static final DeferredRegister<ParticleType<?>> TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, SbwDroneRangeConfig.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE = TYPES.register("vfx_fire", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKE = TYPES.register("vfx_smoke", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> REST = TYPES.register("vfx_rest", () -> new SimpleParticleType(false));
    private ExplosionVfxParticles() {}
}
