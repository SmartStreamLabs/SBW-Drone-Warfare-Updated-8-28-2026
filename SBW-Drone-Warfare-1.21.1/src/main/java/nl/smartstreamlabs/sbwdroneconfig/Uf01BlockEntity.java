package nl.smartstreamlabs.sbwdroneconfig;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The existing root owns only settings/ownership and a transient transport, with no chunk tickets. */
public final class Uf01BlockEntity extends BlockEntity {
    public UUID musicId=UUID.randomUUID(),owner;
    public String ownerName="";
    public int volume=50,range=64;
    public boolean repeat=true;
    public final UfMusicClock music=new UfMusicClock();
    public UUID session=UUID.randomUUID();
    public Uf01BlockEntity(BlockPos p,BlockState s){super(AddonBlockEntities.UF01.get(),p,s);}
    @Override public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide)UfMusicServer.loaded(this);}
    @Override public void setRemoved(){if(level!=null&&!level.isClientSide)UfMusicServer.unloaded(this);super.setRemoved();}
    @Override public void onChunkUnloaded(){if(level!=null&&!level.isClientSide)UfMusicServer.unloaded(this);super.onChunkUnloaded();}
    @Override protected void saveAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider r){super.saveAdditional(n,r);n.putUUID("MusicIdentity",musicId);if(owner!=null)n.putUUID("MusicOwner",owner);n.putString("MusicOwnerName",ownerName);n.putInt("MusicVolume",volume);n.putInt("MusicRange",range);n.putBoolean("MusicRepeat",repeat);}
    @Override public void loadAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider r){super.loadAdditional(n,r);musicId=n.hasUUID("MusicIdentity")?n.getUUID("MusicIdentity"):UUID.randomUUID();owner=n.hasUUID("MusicOwner")?n.getUUID("MusicOwner"):null;ownerName=n.getString("MusicOwnerName");volume=n.contains("MusicVolume")?Math.max(0,Math.min(100,n.getInt("MusicVolume"))):50;range=n.contains("MusicRange")?Math.max(8,Math.min(256,n.getInt("MusicRange"))):UfMusicConfig.RANGE.get();repeat=true;music.stop();}
    public AABB getRenderBoundingBox(){return Uf01Layout.bounds(worldPosition,getBlockState().getValue(Uf01Block.FACING));}
}
