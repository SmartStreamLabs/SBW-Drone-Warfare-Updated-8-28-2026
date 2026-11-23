package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerLevel;import net.minecraft.world.entity.Entity;import net.minecraft.world.phys.*;
/** Translating physical AABB, not a point ray and not a tunnel-sized collision box. */
public final class NtCollision {
 public record Contact(Vec3 movement,Vec3 point,NtStore.Hit hit){}
 public static Contact before(Entity e,Vec3 motion){if(!(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)||e.level().isClientSide()||!(e.level() instanceof ServerLevel level)||!e.isAlive()||!(SbwCompat.isDrone(e)||e instanceof VatagEntity))return null;var hit=hit(e,NtStore.get(level),e.getBoundingBox(),motion);if(hit==null)return null;if(hit.time()<1e-8&&!Double.isFinite(NtGeometry.sweep(e.getBoundingBox().move(motion),Vec3.ZERO,hit.face().triangle())))return new Contact(motion,e.position().add(motion),hit);double fraction=Math.max(0,hit.time()-.02/Math.max(.02,motion.length()));Vec3 allowed=motion.scale(fraction);return new Contact(allowed,e.position().add(allowed),hit);}
 public static void after(Entity e,Contact contact){if(contact==null||!(e.level() instanceof ServerLevel level))return;
  // An intervening terrain collision must not trigger a net beyond that obstruction.
  if(e.position().distanceToSqr(contact.point())>.03*.03)return;
  stopCustom(e);AntiDroneNetEffectSystem.jamDrone(level,e,e.position(),"NT-01 tunnel "+contact.hit().face().tunnel());
 }
 private static void stopCustom(Entity e){if(e instanceof VatagEntity v&&v.operator()!=null)VatagSessions.stop(v.operator());if(e instanceof Rd01Entity r&&r.operator()!=null)Rd01Sessions.stop(r.operator());if(e instanceof Id01Entity i){if(i.operator()!=null)Id01Sessions.stop(i.operator());i.requestReturn();}}
 public static java.util.List<AABB> parts(Entity e){if(e instanceof Hx2Entity h)return Hx2Geometry.parts(h);if(e instanceof Id01Entity i)return Id01Geometry.parts(i);if(e instanceof Rd01Entity r)return Rd01Geometry.parts(r);return java.util.List.of(e.getBoundingBox());}
 private static NtStore.Hit hit(Entity e,NtStore store,AABB broad,Vec3 motion){var best=store.hit(broad,motion);if(best==null||!(e instanceof Hx2Entity||e instanceof Id01Entity||e instanceof Rd01Entity))return best;best=null;for(var part:parts(e)){var contact=store.hit(part,motion);if(contact!=null&&(best==null||contact.time()<best.time()))best=contact;}return best;}
 private record Motion(Vec3 pos,AABB box,long tick){}
 private static final java.util.Map<Entity,Motion> PREVIOUS=new java.util.WeakHashMap<>();
 public static void clear(){PREVIOUS.clear();}
 public static void fallback(Entity e){if(!(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)||e.level().isClientSide()||!(e.level() instanceof ServerLevel level)||!e.isAlive()||!(SbwCompat.isDrone(e)||e instanceof VatagEntity))return;long tick=level.getGameTime();Vec3 at=e.position();var old=PREVIOUS.get(e);if(old!=null&&old.tick==tick-1){Vec3 delta=at.subtract(old.pos);if(delta.lengthSqr()>.0000001&&delta.lengthSqr()<1024*1024){var start=e.getBoundingBox().move(old.pos.subtract(at)).minmax(old.box);var hit=NtStore.get(level).hit(start,delta);if(hit!=null&&(e instanceof Hx2Entity||e instanceof Id01Entity||e instanceof Rd01Entity)){hit=null;for(var part:parts(e)){var contact=NtStore.get(level).hit(part.move(old.pos.subtract(at)),delta);if(contact!=null&&(hit==null||contact.time()<hit.time()))hit=contact;}}if(hit!=null){double f=Math.max(0,hit.time()-.02/Math.max(.02,delta.length()));Vec3 point=old.pos.add(delta.scale(f));e.setPos(point.x,point.y,point.z);stopCustom(e);AntiDroneNetEffectSystem.jamDrone(level,e,point,"NT-01 swept motion "+hit.face().tunnel());at=e.position();}}}PREVIOUS.put(e,new Motion(at,e.getBoundingBox(),tick));}
}
