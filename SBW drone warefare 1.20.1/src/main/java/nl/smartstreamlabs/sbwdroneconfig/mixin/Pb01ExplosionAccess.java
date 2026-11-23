package nl.smartstreamlabs.sbwdroneconfig.mixin;
@org.spongepowered.asm.mixin.Mixin(value=net.minecraft.world.level.Explosion.class)
public interface Pb01ExplosionAccess{@org.spongepowered.asm.mixin.gen.Accessor("blockInteraction") net.minecraft.world.level.Explosion.BlockInteraction pb01$blocks();}
