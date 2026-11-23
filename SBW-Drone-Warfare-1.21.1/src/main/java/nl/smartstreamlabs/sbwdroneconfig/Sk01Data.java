package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;import net.minecraft.nbt.*;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.level.saveddata.SavedData;
/** Versioned dimension-local data, indexed by chunk and UUID. Pending positions survive restart. */
public final class Sk01Data extends SavedData {
 public final Map<UUID,Sk01Entry> entries=new HashMap<>();public final Map<Long,NavigableMap<UUID,Sk01Entry>>chunks=new HashMap<>();
 public static Sk01Data get(ServerLevel l){return l.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Sk01Data::new,Sk01Data::load),"sk01_remains");}
 public NavigableMap<UUID,Sk01Entry> chunk(long key){var m=chunks.get(key);return m==null?Collections.emptyNavigableMap():m;}
 public void put(Sk01Entry e){var old=entries.put(e.id(),e);if(old!=null){var m=chunks.get(old.chunk());m.remove(old.id());if(m.isEmpty())chunks.remove(old.chunk());}chunks.computeIfAbsent(e.chunk(),k->new TreeMap<>()).put(e.id(),e);setDirty();}
 public Sk01Entry remove(UUID id){var e=entries.remove(id);if(e!=null){var m=chunks.get(e.chunk());m.remove(id);if(m.isEmpty())chunks.remove(e.chunk());setDirty();}return e;}
 private static Sk01Data load(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registry){if(tag.getInt("Version")>1)throw new IllegalArgumentException("Newer SK-01 data schema");var d=new Sk01Data();for(var item:tag.getList("Remains",Tag.TAG_COMPOUND)){var t=(CompoundTag)item;var e=new Sk01Entry(t.getUUID("Id"),t.hasUUID("Source")?t.getUUID("Source"):t.getUUID("Id"),t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z"),t.getFloat("Yaw"),t.getFloat("Scale"),t.getString("Model"),t.getLong("Created"),t.getBoolean("Pending"));d.put(e);}return d;}
 @Override public CompoundTag save(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registry){tag.putInt("Version",1);var list=new ListTag();for(var e:entries.values()){var t=new CompoundTag();t.putUUID("Id",e.id());t.putUUID("Source",e.source());t.putDouble("X",e.x());t.putDouble("Y",e.y());t.putDouble("Z",e.z());t.putFloat("Yaw",e.yaw());t.putFloat("Scale",e.scale());t.putString("Model",e.model());t.putLong("Created",e.created());t.putBoolean("Pending",e.pending());list.add(t);}tag.put("Remains",list);return tag;}
}
