package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.nbt.*;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.level.saveddata.SavedData;import java.util.*;
/** Persistent identity leases prevent creative copies of a packed machine opening
 * the same stock twice, including when the original chunk is unloaded. */
public final class DwfClaims extends SavedData{
 public UUID worldId=UUID.randomUUID();
 private record Location(String dimension,long pos){}private final Map<UUID,Location>placed=new HashMap<>();
 public static DwfClaims get(ServerLevel level){return level.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(DwfClaims::new,DwfClaims::load),"dwf01_placements");}
 public boolean claim(UUID id,String dimension,long pos){var next=new Location(dimension,pos);var old=placed.putIfAbsent(id,next);if(old==null)setDirty();return old==null||old.equals(next);}
 public void release(UUID id){if(placed.remove(id)!=null)setDirty();}
 private static DwfClaims load(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registry){var data=new DwfClaims();if(tag.hasUUID("World"))data.worldId=tag.getUUID("World");for(var value:tag.getList("Placed",Tag.TAG_COMPOUND)){var t=(CompoundTag)value;data.placed.put(t.getUUID("Id"),new Location(t.getString("Dimension"),t.getLong("Pos")));}return data;}
 @Override public CompoundTag save(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registry){tag.putUUID("World",worldId);var list=new ListTag();placed.forEach((id,at)->{var t=new CompoundTag();t.putUUID("Id",id);t.putString("Dimension",at.dimension());t.putLong("Pos",at.pos());list.add(t);});tag.put("Placed",list);return tag;}
}
