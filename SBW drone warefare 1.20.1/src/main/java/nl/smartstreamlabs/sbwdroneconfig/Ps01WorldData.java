package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.nbt.*;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.level.*;import net.minecraft.world.level.saveddata.SavedData;import java.util.*;
/** A small persistent per-chunk index. Tombstones clean unloaded parts without chunk tickets or world scans. */
public final class Ps01WorldData extends SavedData{
 public static final class Entry{final BlockPos root;final Direction face;boolean removed;Entry(BlockPos p,Direction f,boolean r){root=p.immutable();face=f;removed=r;}}
 private final Map<BlockPos,Entry> entries=new HashMap<>();private final Map<Long,Set<BlockPos>> chunks=new HashMap<>();
 public static Ps01WorldData get(ServerLevel l){return l.getDataStorage().computeIfAbsent(Ps01WorldData::load,Ps01WorldData::new,"sbw_ps01_shelters");}
 private void index(Entry e){for(var c:all(e.face,e.root))chunks.computeIfAbsent(new ChunkPos(c).toLong(),k->new HashSet<>()).add(e.root);}
 public void add(BlockPos p,Direction f){if(entries.containsKey(p))return;var e=new Entry(p,f,false);entries.put(e.root,e);index(e);setDirty();}
 public Collection<Entry> near(AABBQuery q){var roots=new HashSet<BlockPos>();for(int x=q.x0;x<=q.x1;x++)for(int z=q.z0;z<=q.z1;z++)roots.addAll(chunks.getOrDefault(ChunkPos.asLong(x,z),Set.of()));var out=new ArrayList<Entry>();for(var p:roots){var e=entries.get(p);if(e!=null&&!e.removed)out.add(e);}return out;}
 public record AABBQuery(int x0,int z0,int x1,int z1){}
 public void removed(ServerLevel l,BlockPos p,Direction f){add(p,f);entries.get(p).removed=true;setDirty();clean(l,entries.get(p));}
 public void chunk(ServerLevel l,long key){for(var p:List.copyOf(chunks.getOrDefault(key,Set.of()))){var e=entries.get(p);if(e==null)continue;if(!e.removed&&l.hasChunkAt(p)&&!(l.getBlockState(p).getBlock()instanceof Ps01Block)){e.removed=true;setDirty();}if(e.removed)clean(l,e);}}
 private void clean(ServerLevel l,Entry e){boolean pending=false;boolean previous=Ps01Block.MUTATING.get();Ps01Block.MUTATING.set(true);try{for(var p:all(e.face,e.root)){if(!l.hasChunkAt(p)){pending=true;continue;}var s=l.getBlockState(p);if(s.getBlock()instanceof Ps01Block&&Ps01Block.root(p,s).equals(e.root))l.removeBlock(p,false);}}finally{Ps01Block.MUTATING.set(previous);}if(!pending){entries.remove(e.root);for(var it=chunks.values().iterator();it.hasNext();){var set=it.next();set.remove(e.root);if(set.isEmpty())it.remove();}setDirty();}}
 public static List<BlockPos> all(Direction f,BlockPos root){var out=new ArrayList<BlockPos>();for(var c:Ps01Geometry.BODY)out.add(Ps01Geometry.cell(root,f,c));for(var c:Ps01Geometry.DOOR)out.add(Ps01Geometry.cell(root,f,c));return out;}
 public static Ps01WorldData load(CompoundTag n){var data=new Ps01WorldData();for(var t:n.getList("Shelters",10)){var v=(CompoundTag)t;var e=new Entry(BlockPos.of(v.getLong("Root")),Direction.from2DDataValue(v.getInt("Facing")),v.getBoolean("Removed"));data.entries.put(e.root,e);data.index(e);}return data;}
 @Override public CompoundTag save(CompoundTag n){var a=new ListTag();for(var e:entries.values()){var v=new CompoundTag();v.putLong("Root",e.root.asLong());v.putInt("Facing",e.face.get2DDataValue());v.putBoolean("Removed",e.removed);a.add(v);}n.put("Shelters",a);return n;}
}
