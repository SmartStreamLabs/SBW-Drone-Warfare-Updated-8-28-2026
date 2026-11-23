package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
public final class Vc01Intercept {
 private static final java.util.Set<Entity> TRIGGERED=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
 public static boolean triggered(Entity e){return !e.level().isClientSide()&&TRIGGERED.contains(e);}
 public static void clear(){TRIGGERED.clear();}
 public static boolean hit(ServerLevel level,Entity drone,Vec3 at,String description){return hit(level,drone,at,description,null,null);}
 public static boolean hit(ServerLevel level,Entity drone,Vec3 at,String description,Entity protectedVehicle,String panel){
  if(!Vc01Settings.ENABLED.get())return false;
  if(triggered(drone))return true;
  boolean warhead=drone instanceof GlideBombEntity||drone instanceof Geranium2Entity||drone instanceof LucasDroneEntity&&!(drone instanceof BayraktarTb2Entity);
  Payload payload=payload(drone,at);
  boolean kamikaze=payload!=null;
  if(!warhead&&!kamikaze&&!(drone instanceof Id01Entity)&&!(drone instanceof Hx2Entity))return AntiDroneNetEffectSystem.jamDrone(level,drone,at,description);
  TRIGGERED.add(drone);drone.setPos(at.x,at.y,at.z);
  SbwCompat.disconnectControllersForDrone(level,drone);SbwCompat.resetDroneInput(drone);
  try(var impact=Vc01Damage.intercept(level,protectedVehicle,panel,at,payload!=null?payload.strength():drone instanceof Hx2Entity?14*Hx2Config.BLAST.get().floatValue()+1:drone instanceof Id01Entity?14*Id01Config.BLAST.get().floatValue()+1:DroneCrashExplosionSystem.cageAttackStrength(drone))){
  if(drone instanceof Hx2Entity h)h.detonate(at);else if(drone instanceof Id01Entity id)id.vc01Detonate(at);
  else if(warhead){MissionManager.destroyed(drone);drone.discard();DroneCrashExplosionSystem.handleDroneDestroy(level,drone);}
  else payload.builder().explode();
  }
  // Consume directly: native destroy/collision must not create a second attack.
  ChunkTicketManager.releaseDrone(drone);drone.discard();return true;
 }
 private record Payload(CustomExplosion.Builder builder,float strength){}
 private static Payload payload(Entity entity,Vec3 at){
  if(!(entity instanceof DroneEntity drone)||!drone.getEntityData().get(DroneEntity.IS_KAMIKAZE)||drone.getEntityData().get(DroneEntity.AMMO)<=0)return null;
  var stack=SbwCompat.getDroneCurrentItem(drone);if(stack.isEmpty())return null;
  var attachment=com.atsuishio.superbwarfare.data.CustomData.DRONE_ATTACHMENT.get(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
  if(attachment==null||attachment.explosionRadius<=0||attachment.explosionDamage<=0)return null;
  Entity source=net.minecraft.world.entity.EntityType.byString(attachment.dropEntity()).map(t->t.create(drone.level())).orElse(null);if(source==null)return null;
  source.setPos(at.x,at.y,at.z);
  Entity attacker=com.atsuishio.superbwarfare.tools.EntityFindUtil.findEntity(drone.level(),drone.getLastAttackerUUID());
  // Same SBW attachment damage/radius/attribution as kamikazeExplosion, including
  // unattended payloads; override its eye-height default with the actual panel contact.
  return new Payload(drone.createCustomExplosion().source(source).attacker(attacker).damage(attachment.explosionDamage).radius(attachment.explosionRadius).position(at),attachment.explosionDamage);
 }

}
