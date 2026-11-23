package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.level.block.entity.BlockEntity;
/** Persistent identity and lid state only; feeds and access remain per viewer, server-authorized. */
public final class Dvt01BlockEntity extends BlockEntity {
 public BlockPos pbRoot;public UUID pbBunker;public net.minecraft.core.Direction pbFacing=net.minecraft.core.Direction.NORTH;public net.minecraft.world.item.ItemStack pbItem=net.minecraft.world.item.ItemStack.EMPTY;
 public UUID owner,drone;public boolean open=true;public long transition;public float from,antennaFrom;
 public Dvt01BlockEntity(BlockPos p,BlockState s){super(AddonBlockEntities.DVT01.get(),p,s);}
 public float closed(float partial){if(level==null)return open?0:1;float t=net.minecraft.util.Mth.clamp((level.getGameTime()+partial-transition)/24f,0,1);return from+((open?0:1)-from)*t;}
 public float folded(float partial){if(level==null)return open?0:1;float t=net.minecraft.util.Mth.clamp((level.getGameTime()+partial-transition)/14f,0,1);return antennaFrom+((open?0:1)-antennaFrom)*t;}
 public void toggle(){antennaFrom=folded(0);from=closed(0);open=!open;transition=level.getGameTime();changed();}
 public void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 @Override protected void saveAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(n,registries);if(pbRoot!=null){n.putLong("PbRoot",pbRoot.asLong());if(pbBunker!=null)n.putUUID("PbBunker",pbBunker);n.putInt("PbFacing",pbFacing.get2DDataValue());if(!pbItem.isEmpty())n.put("PbItem",pbItem.save(registries));}if(owner!=null)n.putUUID("Owner",owner);if(drone!=null)n.putUUID("Drone",drone);n.putBoolean("Open",open);n.putLong("Transition",transition);n.putFloat("From",from);n.putFloat("AntennaFrom",antennaFrom);}
 @Override public void loadAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(n,registries);pbRoot=n.contains("PbRoot")?BlockPos.of(n.getLong("PbRoot")):null;pbBunker=n.hasUUID("PbBunker")?n.getUUID("PbBunker"):null;pbFacing=net.minecraft.core.Direction.from2DDataValue(n.getInt("PbFacing"));pbItem=net.minecraft.world.item.ItemStack.parseOptional(registries,n.getCompound("PbItem"));owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;drone=n.hasUUID("Drone")?n.getUUID("Drone"):null;open=!n.contains("Open")||n.getBoolean("Open");transition=n.getLong("Transition");from=n.getFloat("From");antennaFrom=n.getFloat("AntennaFrom");}
 @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(.35,1,.35).move(Pb01Dvt.offset(this));}
}
