package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.*;
/** Loaded entities only. No radius query, chunk acquisition or route/world scan. */
public final class Mj03JammerRegistry {
    private static final Map<ServerLevel,Set<Entity>> DRONES=new IdentityHashMap<>();
    private static final Map<ServerLevel,Set<Mj03Entity>> TRUCKS=new IdentityHashMap<>();
    private static final Map<Entity,Integer> LAST_TICK=new IdentityHashMap<>();
    private static final Set<Entity> TICKING=Collections.newSetFromMap(new IdentityHashMap<>());
    public static void join(Entity e){if(!(e.level() instanceof ServerLevel l))return;if(e instanceof Mj03Entity t){TRUCKS.computeIfAbsent(l,k->new HashSet<>()).add(t);LAST_TICK.put(e,e.tickCount);}else if(e instanceof Hgd01Entity||SbwCompat.isDrone(e)){DRONES.computeIfAbsent(l,k->new HashSet<>()).add(e);LAST_TICK.put(e,e.tickCount);}}
    public static void leave(Entity e){LAST_TICK.remove(e);TICKING.remove(e);if(e.level() instanceof ServerLevel l){var d=DRONES.get(l);if(d!=null)d.remove(e);var t=TRUCKS.get(l);if(t!=null)t.remove(e);}}
    public static void clear(ServerLevel l){DRONES.remove(l);TRUCKS.remove(l);LAST_TICK.keySet().removeIf(e->e.level()==l);TICKING.removeIf(e->e.level()==l);}
    public static void clear(){DRONES.clear();TRUCKS.clear();LAST_TICK.clear();TICKING.clear();}
    public static void tick(){
        // Position-based simulation-range checks exclude entities that really tick under forced
        // tickets (including remote drone chunks). Observe actual entity ticks instead; this also
        // prevents a loaded but frozen truck from providing interference without consuming energy.
        TICKING.clear();
        LAST_TICK.entrySet().removeIf(entry->{Entity e=entry.getKey();if(e.isRemoved())return true;int now=e.tickCount;if(entry.getValue()!=now)TICKING.add(e);entry.setValue(now);return false;});
        if(!AddonConfig.enableDroneJammer())return;
        for(var entry:TRUCKS.entrySet()){
            ServerLevel l=entry.getKey();Set<Entity> drones=DRONES.get(l);if(drones==null)continue;
            entry.getValue().removeIf(t->t.isRemoved());drones.removeIf(d->d.isRemoved());
            for(Mj03Entity t:entry.getValue())if(active(t))for(Entity drone:drones){
                if(!TICKING.contains(drone))continue;
                double distance=t.distanceToSqr(drone);
                if(distance<=(double)Mj03Entity.RANGE*Mj03Entity.RANGE && JammerRules.affects(l,drone,t.owner(),t.trusted()))
                    DroneJamStateManager.recordInfluence(l,drone,Math.sqrt(distance),Mj03Entity.RANGE);
            }
        }
    }
    private static boolean active(Mj03Entity t){return t.operational() && TICKING.contains(t);}
    public static int sourceCount(){return TRUCKS.values().stream().flatMap(Collection::stream).mapToInt(t->active(t)?1:0).sum();}
}
