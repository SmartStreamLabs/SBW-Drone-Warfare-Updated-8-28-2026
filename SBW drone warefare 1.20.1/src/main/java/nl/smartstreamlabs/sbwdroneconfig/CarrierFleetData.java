package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Server-owned roster, independent of monitor copies and of the carrier's lifetime.
 * Unloading a chunk updates the last position; only destruction marks an entry lost. */
public final class CarrierFleetData extends SavedData {
    public static final class Entry {
        public final UUID id, home;
        public final int number;
        public String dimension, name;
        public BlockPos position;
        public boolean destroyed;
        Entry(UUID id, UUID home, int number, String dimension, BlockPos position, String name) {
            this.id=id;this.home=home;this.number=number;this.dimension=dimension;this.position=position.immutable();this.name=name;
        }
    }
    private final Map<UUID,Entry> entries=new HashMap<>();
    private final Map<UUID,LinkedHashMap<UUID,Entry>> fleets=new HashMap<>();
    private final Map<UUID,Integer> nextNumbers=new HashMap<>();
    public static CarrierFleetData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(CarrierFleetData::load,CarrierFleetData::new,"sbw_carrier_fleets");
    }
    public void rememberHome(UUID id,String dimension,BlockPos position,String name){if(entries.containsKey(id))return;entries.put(id,new Entry(id,id,0,dimension,position,name));setDirty();}
    public void forget(UUID id){var e=entries.remove(id);if(e!=null){var f=fleets.get(e.home);if(f!=null)f.remove(id);setDirty();}}
    public Entry entry(UUID id){return entries.get(id);}
    public List<Entry> fleet(UUID home){var f=fleets.get(home);return f==null?List.of():f.values().stream().sorted(Comparator.comparingInt(e->e.number)).toList();}
    public Entry register(Entity entity,UUID home,boolean carrier){
        var e=entries.get(entity.getUUID());
        if(e!=null){if(e.home.equals(home)){refresh(entity);return e;}var previous=fleets.get(e.home);if(previous!=null)previous.remove(e.id);}
        int number=carrier?0:nextNumbers.compute(home,(k,n)->n==null?1:n+1);
        e=new Entry(entity.getUUID(),home,number,entity.level().dimension().location().toString(),entity.blockPosition(),entity.getName().getString());
        entries.put(e.id,e);if(!carrier)fleets.computeIfAbsent(home,k->new LinkedHashMap<>()).put(e.id,e);setDirty();return e;
    }
    public void refresh(Entity entity){
        var e=entries.get(entity.getUUID());if(e==null)return;
        String dim=entity.level().dimension().location().toString(),name=entity.getName().getString();var pos=entity.blockPosition();
        if(!e.position.equals(pos)||!e.dimension.equals(dim)||!e.name.equals(name)){e.position=pos.immutable();e.dimension=dim;e.name=name;setDirty();}
    }
    public void leaving(Entity entity){
        refresh(entity);var e=entries.get(entity.getUUID());var reason=entity.getRemovalReason();
        if(e!=null&&reason!=null&&reason.shouldDestroy()&&!e.destroyed){e.destroyed=true;if(e.number>0){entries.remove(e.id);var f=fleets.get(e.home);if(f!=null)f.remove(e.id);}setDirty();}
    }
    public static CarrierFleetData load(CompoundTag tag){
        var data=new CarrierFleetData();
        for(var value:tag.getList("Entries",10)){
            var n=(CompoundTag)value;if(!n.hasUUID("Id")||!n.hasUUID("Home"))continue;
            var e=new Entry(n.getUUID("Id"),n.getUUID("Home"),n.getInt("Number"),n.getString("Dimension"),BlockPos.of(n.getLong("Position")),n.getString("Name"));
            e.destroyed=n.getBoolean("Destroyed");data.entries.put(e.id,e);
            if(e.number>0){data.fleets.computeIfAbsent(e.home,k->new LinkedHashMap<>()).put(e.id,e);data.nextNumbers.merge(e.home,e.number,Math::max);}
        }
        for(var value:tag.getList("Counters",10)){var n=(CompoundTag)value;if(n.hasUUID("Home"))data.nextNumbers.merge(n.getUUID("Home"),n.getInt("Next"),Math::max);}
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag){
        var list=new ListTag();for(var e:entries.values()){
            var n=new CompoundTag();n.putUUID("Id",e.id);n.putUUID("Home",e.home);n.putInt("Number",e.number);n.putString("Dimension",e.dimension);n.putLong("Position",e.position.asLong());n.putString("Name",e.name);n.putBoolean("Destroyed",e.destroyed);list.add(n);
        }
        tag.put("Entries",list);var counters=new ListTag();for(var pair:nextNumbers.entrySet()){var n=new CompoundTag();n.putUUID("Home",pair.getKey());n.putInt("Next",pair.getValue());counters.add(n);}tag.put("Counters",counters);return tag;
    }
}
