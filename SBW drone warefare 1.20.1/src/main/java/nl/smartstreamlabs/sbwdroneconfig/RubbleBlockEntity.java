package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import java.util.*;

/** Native chunk NBT is canonical. Receipts prevent a retried moving piece being credited twice. */
public final class RubbleBlockEntity extends BlockEntity {
    public static final net.minecraftforge.client.model.data.ModelProperty<BlockState> SOURCE=new net.minecraftforge.client.model.data.ModelProperty<>();
    public record Unit(UUID receipt,BlockState state){}
    private final ArrayList<Unit> units=new ArrayList<>();
    private final HashSet<UUID> receipts=new HashSet<>();
    private BlockState appearance=Blocks.STONE.defaultBlockState();
    public int work;
    public RubbleBlockEntity(BlockPos p,BlockState s){super(AddonBlockEntities.RUBBLE.get(),p,s);}
    public int units(){return units.size();}
    public BlockState appearance(){return appearance;}
    public boolean contains(UUID id){return receipts.contains(id);}
    public boolean accepts(){return units.size()<RubbleConfig.PILE.get()&&receipts.size()<2048;}
    public boolean add(UUID id,BlockState source){if(contains(id))return true;if(units.size()>=RubbleConfig.PILE.get()||receipts.size()>=2048)return false;receipts.add(id);units.add(new Unit(id,source));appearance=units.get(0).state;changed();return true;}
    public Unit take(){if(units.isEmpty())return null;Unit u=units.remove(units.size()-1);work=0;changed();return u;}
    public int layers(){return RubbleEconomy.layers(units.size());}
    public void changed(){setChanged();if(level!=null&&!level.isClientSide){if(units.isEmpty()){level.removeBlock(worldPosition,false);return;}var s=getBlockState();if(s.getValue(RubbleBlock.LAYERS)!=layers())level.setBlock(worldPosition,s.setValue(RubbleBlock.LAYERS,layers()),3);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}}
    @Override protected void saveAdditional(CompoundTag n){super.saveAdditional(n);var a=new ListTag();for(var u:units){var t=new CompoundTag();t.putUUID("Receipt",u.receipt);t.put("State",RubbleMaterials.write(u.state));a.add(t);}n.put("Units",a);var rs=new ListTag();for(var id:receipts){var t=new CompoundTag();t.putUUID("Id",id);rs.add(t);}n.put("Receipts",rs);n.putInt("Work",work);n.put("Appearance",RubbleMaterials.write(appearance));}
    @Override public void load(CompoundTag n){super.load(n);units.clear();receipts.clear();for(var entry:n.getList("Units",10)){var t=(CompoundTag)entry;if(t.hasUUID("Receipt"))units.add(new Unit(t.getUUID("Receipt"),RubbleMaterials.read(t.getCompound("State"))));}for(var entry:n.getList("Receipts",10)){var t=(CompoundTag)entry;if(t.hasUUID("Id"))receipts.add(t.getUUID("Id"));}work=Math.max(0,n.getInt("Work"));appearance=RubbleMaterials.read(n.getCompound("Appearance"));if(level!=null&&level.isClientSide){requestModelDataUpdate();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),8);}}
    @Override public CompoundTag getUpdateTag(){var n=new CompoundTag();n.put("Appearance",RubbleMaterials.write(appearance));n.putInt("Count",units.size());n.putInt("Work",work);return n;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public net.minecraftforge.client.model.data.ModelData getModelData(){return net.minecraftforge.client.model.data.ModelData.builder().with(SOURCE,appearance).build();}
    @Override public void setRemoved(){if(level!=null&&level.isClientSide)RubbleParticleSource.remember(level,worldPosition,appearance);super.setRemoved();}
    @Override public void onLoad(){super.onLoad();if(level!=null&&level.isClientSide)requestModelDataUpdate();}
    @Override public void handleUpdateTag(CompoundTag n){appearance=RubbleMaterials.read(n.getCompound("Appearance"));requestModelDataUpdate();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),8);}
}
