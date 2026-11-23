package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import net.minecraft.world.level.block.Block;
/** Native SBW small shells otherwise delete one helper directly, bypassing the bunker durability and claims. */
@Mixin(targets="com.atsuishio.superbwarfare.entity.projectile.SmallCannonShellEntity",remap=false)
public abstract class Pb01SbwBlockMixin{
 @Redirect(method={"afterHitBlock","onHitBlock","m_8060_"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/Block;m_155943_()F",remap=false),require=1)
 private float pb01$structuredImpact(Block block){return block instanceof nl.smartstreamlabs.sbwdroneconfig.Pb01Block?-1:block.defaultDestroyTime();}
}
