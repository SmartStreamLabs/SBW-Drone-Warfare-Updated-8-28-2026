package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.world.level.block.state.BlockState;
/** Only the two port cells acquire an optional RS network-node proxy. */
public final class DwfPortBlockEntity extends DwfBlockEntity{
 public Object rsAttachment;
 public DwfPortBlockEntity(BlockPos p,BlockState s){super(p,s);}
 // Query only an existing FULL chunk; Level.getBlockEntity can refresh load tickets.
 @Override public DwfBlockEntity root(){if(getLevel()instanceof net.minecraft.server.level.ServerLevel level){var pos=DwfGeometry.root(getBlockPos(),facing(),getBlockState().getValue(DwfBlock.PART));var chunk=level.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);if(chunk==null)return null;var value=chunk.getBlockEntity(pos,net.minecraft.world.level.chunk.LevelChunk.EntityCreationType.CHECK);return value instanceof DwfBlockEntity root&&!root.isRemoved()?root:null;}return getLevel()==null?null:super.root();}
 @Override public void onLoad(){super.onLoad();if(getLevel()!=null&&net.neoforged.fml.ModList.get().isLoaded("refinedstorage"))DwfRs2.loaded(this);}
 @Override public void setRemoved(){super.setRemoved();if(getLevel()!=null&&!getLevel().isClientSide&&net.neoforged.fml.ModList.get().isLoaded("refinedstorage"))DwfRs2.removed(this);}
}
