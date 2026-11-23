package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.*;
import java.util.*;
/** Attack context is captured before damage and outlives removal of its causing drone. */
public final class Vc01Damage {
 private static final Set<Explosion> MARKED=Collections.newSetFromMap(new WeakHashMap<>()),SEEN=Collections.newSetFromMap(new WeakHashMap<>());
 private record Shield(Entity vehicle,Map<String,Vc01Panels.Health> before,Map<String,Float> factors,String forced,Impact impact){}
 private record Attack(ServerLevel level,long expires,Vec3 origin,Map<UUID,Shield> shields,DroneAttackDamage.Identity identity,double boost){}
 private static final Map<Explosion,DroneAttackDamage.Identity> IDENTITIES=new WeakHashMap<>();
 private static final LinkedHashMap<DamageSource,Attack> ATTACKS=new LinkedHashMap<>();
 private static boolean droneAttack(Entity e){return e!=null&&(SbwCompat.isDrone(e)||e.getPersistentData().getBoolean(DroneExplosionVfx.PAYLOAD_TAG)||e instanceof AntiVehicleMortarPayloadEntity);}
 private static final ThreadLocal<Impact> IMPACT=new ThreadLocal<>();
 private static final class Impact {final ServerLevel level;final Entity vehicle;final String panel;final Vec3 at;final float strength;double boost=1;boolean used,charged;Impact(ServerLevel l,Entity e,String p,Vec3 at,float strength){level=l;vehicle=e;panel=p;this.at=at;this.strength=strength;}}
 public static final class ImpactScope implements AutoCloseable {private final Impact previous,current;private ImpactScope(Impact p,Impact current){previous=p;this.current=current;}public void close(){try{if(current.used&&!current.charged&&current.vehicle!=null&&current.panel!=null&&SbwCompat.hasVehicleAntiDroneNetUpgrade(current.vehicle)){Vc01Panels.damage(current.vehicle,current.panel,current.strength*current.boost*Vc01Settings.DAMAGE.get());current.charged=true;}}finally{if(previous==null)IMPACT.remove();else IMPACT.set(previous);}}}
 public static ImpactScope intercept(ServerLevel l,Entity vehicle,String panel,Vec3 at,float strength){var old=IMPACT.get();Impact now=new Impact(l,vehicle,panel,at,strength);IMPACT.set(now);return new ImpactScope(old,now);}
 public static void mark(Explosion explosion){MARKED.add(explosion);if(DroneAttackDamage.current()!=null)IDENTITIES.put(explosion,DroneAttackDamage.current());}
 public static void capture(ServerLevel level,Explosion explosion,List<Entity> affected){
  cleanup(level);if(SEEN.contains(explosion))return;
  if(!MARKED.contains(explosion)&&!droneAttack(explosion.getDirectSourceEntity()))return;
  var identity=IDENTITIES.get(explosion);if(identity==null)identity=DroneAttackDamage.identify(explosion.getDirectSourceEntity(),((nl.smartstreamlabs.sbwdroneconfig.mixin.Vc01ExplosionAccess)explosion).vc01$source().getDirectEntity());double boost=DroneAttackDamage.multiplier(identity);
  SEEN.add(explosion);Map<UUID,Shield> shields=new HashMap<>();Impact impact=IMPACT.get();boolean forced=impact!=null&&!impact.used&&impact.level==level&&impact.at.distanceToSqr(explosion.getPosition())<1e-8;if(forced){impact.used=true;impact.boost=boost;}
  for(Entity e:affected)if(Vc01Settings.ENABLED.get()&&SbwCompat.hasVehicleAntiDroneNetUpgrade(e)&&Vc01Geometry.profile(e)!=null)shields.put(e.getUUID(),new Shield(e,before(e,identity),new HashMap<>(),forced&&impact.vehicle==e?impact.panel:null,forced&&impact.vehicle==e?impact:null));
  ATTACKS.put(((nl.smartstreamlabs.sbwdroneconfig.mixin.Vc01ExplosionAccess)explosion).vc01$source(),new Attack(level,level.getGameTime()+2,explosion.getPosition(),shields,identity,boost));while(ATTACKS.size()>128)ATTACKS.remove(ATTACKS.keySet().iterator().next());
 }
 public static void component(ServerLevel level,DamageSource source,Entity payload,List<Entity> targets){
  cleanup(level);var identity=DroneAttackDamage.identify(payload,null);Map<UUID,Shield> shields=new HashMap<>();for(Entity e:targets)if(Vc01Settings.ENABLED.get()&&SbwCompat.hasVehicleAntiDroneNetUpgrade(e)&&Vc01Geometry.profile(e)!=null)shields.put(e.getUUID(),new Shield(e,before(e,identity),new HashMap<>(),null,null));
  ATTACKS.put(source,new Attack(level,level.getGameTime()+2,payload.position(),shields,identity,DroneAttackDamage.multiplier(identity)));while(ATTACKS.size()>128)ATTACKS.remove(ATTACKS.keySet().iterator().next());
 }
 private static Map<String,Vc01Panels.Health> before(Entity vehicle,DroneAttackDamage.Identity identity){if(identity!=null)for(Attack a:ATTACKS.values())if(a.level==vehicle.level()&&a.expires>=vehicle.level().getGameTime()&&a.identity!=null&&a.identity.id().equals(identity.id())){Shield s=a.shields.get(vehicle.getUUID());if(s!=null)return s.before;}return Vc01Panels.snapshot(vehicle);}
 public static void clear(){ATTACKS.clear();MARKED.clear();SEEN.clear();IDENTITIES.clear();}
 public static void unload(ServerLevel level){ATTACKS.entrySet().removeIf(x->x.getValue().level==level);}
 public static void cleanup(ServerLevel level){ATTACKS.entrySet().removeIf(x->x.getValue().level==level&&x.getValue().expires<level.getGameTime());}
 public static float filter(Entity vehicle,DamageSource source,float armored,float original){
  if(!(vehicle.level() instanceof ServerLevel level)||!SbwCompat.isVehicleNetHost(vehicle))return armored;
  Attack attack=ATTACKS.get(source);if(attack==null||attack.level!=level||attack.expires<level.getGameTime())return armored;
  float penetrating=Math.max(armored,Math.max(0,original)*.35f);
  double unprotectedBoost=attack.boost*(attack.identity!=null?DroneVehicleDamageConfig.UNPROTECTED.get():1);
  Shield shield=attack.shields.get(vehicle.getUUID());if(shield==null||!Vc01Settings.ENABLED.get()||!SbwCompat.hasVehicleAntiDroneNetUpgrade(vehicle))return (float)(penetrating*unprotectedBoost);
  Float previous=shield.factors.get("applied");if(previous!=null)return (float)((previous<1?armored*Math.min(attack.boost,1):penetrating*unprotectedBoost)*previous);
  var hit=shield.forced!=null?shield.forced:occluder(vehicle,attack.origin,target(vehicle,source),shield.before);
  float factor=1;if(hit!=null){factor=(float)Vc01Panels.factor(Vc01Settings.REDUCTION.get(),shield.before.get(hit).fraction());Vc01Panels.damage(vehicle,hit,(shield.impact!=null?shield.impact.strength:Math.max(0,original))*attack.boost*Vc01Settings.DAMAGE.get());if(shield.impact!=null)shield.impact.charged=true;}
  shield.factors.put("applied",factor);
  if(Boolean.getBoolean("sbw.vc01.trace"))System.out.println("VC01_DAMAGE vehicle="+vehicle.getUUID()+" panel="+hit+" original="+original+" armored="+armored+" boost="+attack.boost+" factor="+factor+" final="+((factor<1?armored*Math.min(attack.boost,1):penetrating*unprotectedBoost)*factor));
  return (float)((factor<1?armored*Math.min(attack.boost,1):penetrating*unprotectedBoost)*factor);
 }
 private static Vec3 target(Entity vehicle,DamageSource source){
  if(source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile){
   var part=com.atsuishio.superbwarfare.entity.mixin.OBBHitter.Companion.getInstance(projectile).sbw$getCurrentHitPart();
   if(part!=null){for(var obb:((com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)vehicle).getOBBs())if(obb.part==part){Vec3 sum=Vec3.ZERO;var vertices=obb.getVertices();for(var v:vertices)sum=sum.add(v.x,v.y,v.z);if(vertices.length>0)return sum.scale(1.0/vertices.length);}}
  }
  // SBW's area explosion route otherwise supplies one damage amount for the whole entity.
  return vehicle.getBoundingBox().getCenter();
 }
 public static String occluder(Entity vehicle,Vec3 from,Vec3 to,Map<String,Vc01Panels.Health> before){
  var profile=Vc01Geometry.profile(vehicle);if(profile==null)return null;double best=Double.POSITIVE_INFINITY;String result=null;
  AABB point=new AABB(from.x-.001,from.y-.001,from.z-.001,from.x+.001,from.y+.001,from.z+.001);Vec3 movement=to.subtract(from);
  for(int b=0;b<profile.branches().size();b++){var branch=profile.branches().get(b);var matrix=Vc01Geometry.matrix(vehicle,branch,1);for(int p=0;p<branch.panels().size();p++){String key=Vc01Panels.id(b,branch.panelIds().get(p));var health=before.get(key);if(health==null||health.fraction()<=0)continue;double t=NtGeometry.sweep(point,movement,Vc01Geometry.triangle(branch.panels().get(p),matrix));if(Double.isFinite(t)&&t<best&&t<.999){best=t;result=key;}}}return result;
 }
}
