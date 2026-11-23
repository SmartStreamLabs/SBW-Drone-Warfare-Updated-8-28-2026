package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.util.function.BooleanSupplier;import net.minecraft.world.entity.*;import net.minecraft.server.level.*;import net.minecraft.server.MinecraftServer;import net.minecraft.world.phys.Vec3;
/** Capture before other death listeners restore remote pilots; commit after all cancellation/revive decisions. */
public final class Sk01Deaths {
 public record Snapshot(ServerLevel level,Vec3 at,float yaw){}
 private record Candidate(Entity entity,Snapshot snapshot,List<BooleanSupplier> decisions){}
 private static final Map<Entity,Candidate>PENDING=new IdentityHashMap<>();
 private static final Sk01IdentityWeakMap<Entity,Boolean>VETO=new Sk01IdentityWeakMap<>();
 private record Committed(ServerLevel level,UUID id){}
 private static final Sk01IdentityWeakMap<Entity,Committed>COMMITTED=new Sk01IdentityWeakMap<>();
 public static Snapshot capture(Entity e){if(!(e.level() instanceof ServerLevel l))return null;Vec3 at=e.position();float yaw=e.getYRot();if(e instanceof ServerPlayer p){var original=PlayerDroneAnchorManager.getCapturedOriginalPosition(p);var dim=PlayerDroneAnchorManager.getCapturedOriginalDimension(p);if(original!=null&&dim!=null){var physical=p.getServer().getLevel(dim);if(physical!=null){l=physical;at=original;yaw=PlayerDroneAnchorManager.getCapturedOriginalYaw(p);}}}return new Snapshot(l,at,yaw);}
 public static void consider(LivingEntity e,BooleanSupplier cancelled){if(e.level() instanceof ServerLevel&&e.isAddedToWorld()&&Sk01Config.ENABLED.get()){VETO.remove(e);PENDING.computeIfAbsent(e,key->new Candidate(e,capture(e),new ArrayList<>(1))).decisions.add(cancelled);}}
 public static void flush(MinecraftServer s){VETO.prune();COMMITTED.prune();long until=System.nanoTime()+500_000;int budget=Sk01Config.WORK.get();var it=PENDING.values().iterator();while(it.hasNext()&&budget-->0&&System.nanoTime()<until){var c=it.next();if(c.snapshot.level.getServer()!=s)continue;it.remove();if(c.decisions.stream().allMatch(BooleanSupplier::getAsBoolean)){VETO.put(c.entity,true);continue;}if(c.entity instanceof LivingEntity living&&living.isDeadOrDying())commit(c.entity,c.snapshot);}}
 public static void commit(Entity e,Snapshot at){if(at!=null){var entry=Sk01Server.create(e,at.level,at.at,at.yaw);if(entry!=null&&e instanceof LivingEntity)COMMITTED.put(e,new Committed(at.level,entry.id()));}}
 /** Explicit actual-destruction endpoints that do not delegate to SBW VehicleEntity.destroy. */
 public static void destroyed(Entity e){if(!(e.level() instanceof ServerLevel l)||!e.isAddedToWorld())return;if(!l.getServer().isSameThread()){l.getServer().execute(()->destroyed(e));return;}commit(e,capture(e));}
 public static void removed(Entity e,Entity.RemovalReason reason){if(!(e.level() instanceof ServerLevel)||!e.isAddedToWorld()||VETO.containsKey(e))return;boolean actual=reason==Entity.RemovalReason.KILLED&&(e instanceof LivingEntity||e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity);
  if(!actual)return;if(e instanceof LivingEntity living){PENDING.computeIfAbsent(e,key->new Candidate(e,capture(e),new ArrayList<>(List.of(()->false))));return;}var pending=PENDING.get(e);commit(e,pending==null?capture(e):pending.snapshot);}
 public static void recovered(LivingEntity e){if(!(e.level() instanceof ServerLevel))return;if(e.getHealth()>0){VETO.remove(e);var saved=COMMITTED.remove(e);if(saved!=null&&!e.isRemoved())Sk01Server.remove(Sk01Server.context(saved.level),saved.id);if(e.getPersistentData().hasUUID(Sk01Server.DEATH_TAG))e.getPersistentData().remove(Sk01Server.DEATH_TAG);}}
 public static void stop(MinecraftServer s){PENDING.values().removeIf(c->c.snapshot.level.getServer()==s);VETO.removeIf((e,value)->e.getServer()==s);COMMITTED.removeIf((e,c)->c.level.getServer()==s);}
 private Sk01Deaths(){}
}
