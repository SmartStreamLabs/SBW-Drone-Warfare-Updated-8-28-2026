package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.data.gun.GunData;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;import java.util.*;
/** Bounded server acquisition. Clients never nominate a target or advance a lock. */
public final class Dil01System {
 public static final double RANGE=256,CONE=10;public static final int LOCK_TICKS=30;
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 private static class Session {UUID gun,target;int progress;long seen;boolean clear;}
 public static void clear(UUID p){SESSIONS.remove(p);}
 public static void clearAll(){SESSIONS.clear();}
 public static void reset(Player p){clear(p.getUUID());for(var stack:p.getInventory().items)if(Dil01Item.is(stack)){var d=GunData.from(stack);d.zooming.set(false);for(String key:new String[]{"DILLocked","DILProgress","DILStamp","DILTarget","DILTargetId"})d.data().remove(key);Dil01GunDataCompat.changed(d);}}
 public static boolean hostile(Player p,Entity target){
  if(p==null||!p.isAlive()||p.isSpectator()||target==null||!target.isAlive()||target.isRemoved()||target.level()!=p.level()||!SbwCompat.isDrone(target)||target.isPassenger())return false;
  if(!(target instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v)||v.isWreck()||target.onGround()||target instanceof Hx2Entity h&&(!h.flying()||h.mount()!=null)||target instanceof Id01Entity i&&(i.mode()==Id01Entity.Mode.STANDBY||i.mode()==Id01Entity.Mode.DISABLED))return false;
  if(!(p.level() instanceof net.minecraft.server.level.ServerLevel l))return false;var owner=SbwCompat.resolveDroneOwner(l,target);if(!owner.hasUuid())return false;if(owner.uuid().equals(p.getUUID()))return true;
  var o=l.getServer().getPlayerList().getPlayer(owner.uuid());if(o!=null&&p.isAlliedTo(o))return false;var name=o==null?owner.name():o.getScoreboardName();var a=p.getTeam();var b=name==null?null:l.getScoreboard().getPlayersTeam(name);return a==null||b==null||!a.isAlliedTo(b);
 }
 private static Vec3 centre(Entity e){return e.getBoundingBox().getCenter();}
 private static boolean sight(Player p,Entity t){if(!hostile(p,t)||p.getEyePosition().distanceTo(centre(t))>RANGE)return false;var delta=centre(t).subtract(p.getEyePosition());double radius=Math.min(4,t.getBoundingBox().getSize()*.3);double extra=Math.toDegrees(Math.atan2(radius,Math.max(1,delta.length())));if(p.getLookAngle().dot(delta.normalize())<Math.cos(Math.toRadians(CONE+extra)))return false;return p.level().clip(new ClipContext(p.getEyePosition(),centre(t),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;}
 public static Entity locked(ServerPlayer p,GunData data){var s=SESSIONS.get(p.getUUID());if(s==null||s.progress<LOCK_TICKS||!s.clear||!data.zooming.get()||!p.getMainHandItem().equals(data.stack)||!s.gun.equals(data.data().getUUID("UUID")))return null;var e=p.serverLevel().getEntity(s.target);return sight(p,e)?e:null;}
 public static void tick(Entity holder,GunData data,boolean selected){
  if(!(holder instanceof ServerPlayer p))return;var tag=data.data();UUID gun=tag.getUUID("UUID");var old=SESSIONS.get(p.getUUID());
  if(!selected||!data.zooming.get()||!p.isAlive()||p.isSpectator()||data.reloading()||data.ammo.get()!=1){if(old!=null&&old.gun.equals(gun))SESSIONS.remove(p.getUUID());write(data,null,0,false,p.level().getGameTime());return;}
  Session s=old;if(s==null||!s.gun.equals(gun)){s=new Session();s.gun=gun;SESSIONS.put(p.getUUID(),s);}long now=p.level().getGameTime();s.seen=now;
  Entity e=s.target==null?null:p.serverLevel().getEntity(s.target);boolean visible=sight(p,e);
  if(!visible&&now%4==0){var eye=p.getEyePosition();var direction=p.getLookAngle();var candidates=p.serverLevel().getEntities(p,new AABB(eye,eye).inflate(RANGE),t->hostile(p,t));candidates.sort(Comparator.comparingDouble(t->1-direction.dot(centre(t).subtract(eye).normalize())));int n=0;for(var t:candidates){if(++n>64)break;if(sight(p,t)){if(!t.getUUID().equals(s.target))s.progress=0;s.target=t.getUUID();e=t;visible=true;break;}}}
  s.clear=visible;s.progress=visible?Math.min(LOCK_TICKS,s.progress+1):Math.max(0,s.progress-3);if(s.progress==0&&!visible)s.target=null;
  if(now%4==0||tag.getBoolean("DILLocked")!=(visible&&s.progress==LOCK_TICKS))write(data,e,s.progress,visible&&s.progress==LOCK_TICKS,now);
 }
 private static void write(GunData data,Entity target,int progress,boolean locked,long time){var t=data.data();if(!locked&&progress==0&&target==null&&!t.getBoolean("DILLocked")&&t.getInt("DILProgress")==0&&!t.contains("DILTarget"))return;t.putBoolean("DILLocked",locked);t.putInt("DILProgress",progress);t.putLong("DILStamp",time);if(target!=null){t.putUUID("DILTarget",target.getUUID());t.putInt("DILTargetId",target.getId());}else{t.remove("DILTarget");t.putInt("DILTargetId",-1);}Dil01GunDataCompat.changed(data);}
 public static void fired(ServerPlayer p,GunData d){SESSIONS.remove(p.getUUID());write(d,null,0,false,p.level().getGameTime());}
 private Dil01System(){}
}
