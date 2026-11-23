package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.level.BlockGetter;import net.minecraft.world.level.block.Blocks;import net.minecraft.world.level.block.entity.BlockEntity;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.level.material.FluidState;
/** Collision-shape neighbour reads must not turn a preview into chunk generation. */
public final class NtLoadedView implements BlockGetter {
 private final ServerLevel level;public boolean missing;public NtLoadedView(ServerLevel l){level=l;}
 @Override public BlockState getBlockState(BlockPos p){var c=level.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4);if(c==null){missing=true;return Blocks.BARRIER.defaultBlockState();}return c.getBlockState(p);}
 @Override public FluidState getFluidState(BlockPos p){return getBlockState(p).getFluidState();}
 @Override public BlockEntity getBlockEntity(BlockPos p){var c=level.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4);return c==null?null:c.getBlockEntity(p);}
 @Override public int getHeight(){return level.getHeight();}@Override public int getMinBuildHeight(){return level.getMinBuildHeight();}
}
