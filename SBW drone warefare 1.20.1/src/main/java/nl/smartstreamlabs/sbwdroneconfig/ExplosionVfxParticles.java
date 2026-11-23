package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
public final class ExplosionVfxParticles {
    public static final DeferredRegister<ParticleType<?>> TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, SbwDroneRangeConfig.MOD_ID);
    public static final RegistryObject<SimpleParticleType> FIRE = TYPES.register("vfx_fire", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SMOKE = TYPES.register("vfx_smoke", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> REST = TYPES.register("vfx_rest", () -> new SimpleParticleType(false));
    private ExplosionVfxParticles() {}
}
