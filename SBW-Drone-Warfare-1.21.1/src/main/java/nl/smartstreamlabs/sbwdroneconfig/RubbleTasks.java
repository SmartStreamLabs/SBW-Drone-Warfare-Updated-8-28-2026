package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Only explicit blast jobs are stored. Reading/chunk loading never schedules structural physics. */
public final class RubbleTasks extends SavedData {
    public static final String ID="sbwdroneconfig_rubble_tasks";
    public static final class Seed {public BlockPos pos;public int chain;Seed(BlockPos p,int c){pos=p;chain=c;}}
    public static final class Job {
        public BlockPos centre; public UUID owner;public net.minecraft.world.phys.Vec3 origin;public double core=3; public boolean securitycraft;
        public final LinkedHashMap<BlockPos,BlockState> pending=new LinkedHashMap<>(), before=new LinkedHashMap<>();
        public final ArrayDeque<Seed> seeds=new ArrayDeque<>();public final Set<BlockPos> checked=new HashSet<>();
        public final Set<BlockPos> denied=new HashSet<>();
        public Job(BlockPos p,UUID u){centre=p;owner=u;origin=net.minecraft.world.phys.Vec3.atCenterOf(p);}
    }
    public record Deposit(UUID id,BlockPos pos,BlockState state,UUID owner){public Deposit(UUID id,BlockPos pos,BlockState state){this(id,pos,state,null);}}
    public final ArrayDeque<Job> jobs=new ArrayDeque<>();
    public final ArrayDeque<Deposit> deposits=new ArrayDeque<>();
    public static RubbleTasks get(ServerLevel l){return l.getDataStorage().computeIfAbsent(new SavedData.Factory<>(RubbleTasks::new,(n,lookup)->load(n)),ID);}
    static ListTag states(Map<BlockPos,BlockState> map){var a=new ListTag();for(var e:map.entrySet()){var n=new CompoundTag();n.putLong("Pos",e.getKey().asLong());n.put("State",RubbleMaterials.write(e.getValue()));a.add(n);}return a;}
    static void states(ListTag tags,Map<BlockPos,BlockState> map){for(var entry:tags){var n=(CompoundTag)entry;map.put(BlockPos.of(n.getLong("Pos")),RubbleMaterials.read(n.getCompound("State")));}}
    public static RubbleTasks load(CompoundTag n){var d=new RubbleTasks();for(var entry:n.getList("Jobs",10)){var t=(CompoundTag)entry;var j=new Job(BlockPos.of(t.getLong("Centre")),t.hasUUID("Owner")?t.getUUID("Owner"):null);if(t.contains("OriginX"))j.origin=new net.minecraft.world.phys.Vec3(t.getDouble("OriginX"),t.getDouble("OriginY"),t.getDouble("OriginZ"));j.core=t.contains("Core")?t.getDouble("Core"):3;j.securitycraft=t.getBoolean("SecurityCraft");states(t.getList("Pending",10),j.pending);states(t.getList("Before",10),j.before);for(var a:t.getList("Seeds",10)){var s=(CompoundTag)a;j.seeds.add(new Seed(BlockPos.of(s.getLong("Pos")),s.getInt("Chain")));}for(long pos:t.getLongArray("Denied"))j.denied.add(BlockPos.of(pos));for(long p:t.getLongArray("Checked"))j.checked.add(BlockPos.of(p));d.jobs.add(j);}for(var entry:n.getList("Deposits",10)){var t=(CompoundTag)entry;d.deposits.add(new Deposit(t.getUUID("Id"),BlockPos.of(t.getLong("Pos")),RubbleMaterials.read(t.getCompound("State")),t.hasUUID("Owner")?t.getUUID("Owner"):null));}return d;}
    @Override public CompoundTag save(CompoundTag n,net.minecraft.core.HolderLookup.Provider lookup){var a=new ListTag();for(var j:jobs){var t=new CompoundTag();t.putLong("Centre",j.centre.asLong());t.putDouble("OriginX",j.origin.x);t.putDouble("OriginY",j.origin.y);t.putDouble("OriginZ",j.origin.z);t.putDouble("Core",j.core);t.putBoolean("SecurityCraft",j.securitycraft);if(j.owner!=null)t.putUUID("Owner",j.owner);t.put("Pending",states(j.pending));t.put("Before",states(j.before));var ss=new ListTag();for(var seed:j.seeds){var s=new CompoundTag();s.putLong("Pos",seed.pos.asLong());s.putInt("Chain",seed.chain);ss.add(s);}t.put("Seeds",ss);t.putLongArray("Denied",j.denied.stream().mapToLong(BlockPos::asLong).toArray());t.putLongArray("Checked",j.checked.stream().mapToLong(BlockPos::asLong).toArray());a.add(t);}n.put("Jobs",a);a=new ListTag();for(var p:deposits){var t=new CompoundTag();t.putUUID("Id",p.id);if(p.owner!=null)t.putUUID("Owner",p.owner);t.putLong("Pos",p.pos.asLong());t.put("State",RubbleMaterials.write(p.state));a.add(t);}n.put("Deposits",a);return n;}
}
