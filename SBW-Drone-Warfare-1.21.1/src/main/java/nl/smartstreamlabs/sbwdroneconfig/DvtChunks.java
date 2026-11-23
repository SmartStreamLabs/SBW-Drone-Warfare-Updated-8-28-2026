package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.client.Minecraft;import net.minecraft.client.multiplayer.ClientLevel;import net.minecraft.world.level.*;import net.minecraft.world.level.chunk.LevelChunk;
/** A sparse second view. Vanilla player chunk storage/center remains untouched. */
public final class DvtChunks {
 private static ClientLevel world;private static final Map<Long,LevelChunk> CHUNKS=new java.util.concurrent.ConcurrentHashMap<>();private static volatile Set<Long> wanted=Set.of();public static int centerX,centerZ,radius=12;
 public static void regions(Set<Long> region){var m=Minecraft.getInstance();if(world!=m.level){CHUNKS.clear();world=m.level;}wanted=Set.copyOf(region);if(world==null)return;var it=CHUNKS.entrySet().iterator();while(it.hasNext()){var e=it.next();if(!wanted.contains(e.getKey())){world.unload(e.getValue());it.remove();}}}
 public static boolean accepts(int x,int z){return Minecraft.getInstance().level==world&&wanted.contains(ChunkPos.asLong(x,z));}
 public static boolean outside(int x,int z){return Math.abs(x-centerX)>radius||Math.abs(z-centerZ)>radius;}
 public static LevelChunk get(int x,int z){return CHUNKS.get(ChunkPos.asLong(x,z));}
 public static LevelChunk receive(ClientLevel level,int x,int z,net.minecraft.network.FriendlyByteBuf b,net.minecraft.nbt.CompoundTag n,java.util.function.Consumer<net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer){if(!accepts(x,z))return null;var cp=new ChunkPos(x,z);var chunk=CHUNKS.computeIfAbsent(cp.toLong(),k->new LevelChunk(level,cp));chunk.replaceWithPacketData(b,n,consumer);level.onChunkLoaded(cp);DvtClient.dirty(x,0,z,true);return chunk;}
 public static int size(){return CHUNKS.size();}
 public static void clear(){regions(Set.of());world=null;centerX=centerZ=0;radius=12;}
 private DvtChunks(){}
}
