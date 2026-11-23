package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Explosion.class)
public interface RubbleExplosionAccess {
    @Accessor("radius") float rubble$radius();
    @Accessor("level") Level rubble$level();
}
