package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.level.block.LightBlock;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.core.BlockPos;import net.minecraft.server.level.ServerLevel;import net.minecraft.util.RandomSource;
/** Waterlogged vanilla-light fallback with an expiring scheduled lease, including after save/reload. No item. */
public final class Sd01LampBlock extends LightBlock {
 public Sd01LampBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties p){super(p);}
 @Override public void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){Sd01Lights.expire(level,pos);}
}
