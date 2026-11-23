package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Explosion.class)
public interface Vc01ExplosionAccess {
 @Accessor("damageSource") DamageSource vc01$source();
}
