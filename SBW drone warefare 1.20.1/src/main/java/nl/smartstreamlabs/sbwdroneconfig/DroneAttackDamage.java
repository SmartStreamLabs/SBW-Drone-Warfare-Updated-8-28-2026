package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;
/** Immutable attack metadata; never modifies native DamageSource/owner attribution. */
public final class DroneAttackDamage {
 public static final String TYPE="sbwAttackDroneType",PAYLOAD="sbwAttackPayload",OWNER="sbwAttackOwner";
 public record Identity(UUID id,String drone,String payload,String owner){}
 private static final ThreadLocal<Identity> CURRENT=new ThreadLocal<>();
 public static Identity current(){return CURRENT.get();}
 public static final class Scope implements AutoCloseable {private final Identity old;Scope(Identity value){old=CURRENT.get();if(value==null)CURRENT.remove();else CURRENT.set(value);}public void close(){if(old==null)CURRENT.remove();else CURRENT.set(old);}}
 public static Scope scope(Entity drone){return new Scope(identify(drone,null));}
 public static void mark(Entity payload,Entity drone,String item){
  if(drone instanceof DeliveryDroneEntity||drone instanceof Rd01Entity)return;
  var t=payload.getPersistentData();t.putString(TYPE,BuiltInRegistries.ENTITY_TYPE.getKey(drone.getType()).toString());t.putString(PAYLOAD,item.isEmpty()||item.equals("minecraft:air")?BuiltInRegistries.ENTITY_TYPE.getKey(payload.getType()).toString():item);
  UUID owner=owner(drone);if(owner!=null)t.putString(OWNER,owner.toString());
 }
 private static UUID owner(Entity e){UUID id=SbwCompat.getDroneControllerUuid(e);return id!=null?id:e.getPersistentData().hasUUID(SbwCompat.TAG_DRONE_OWNER_UUID)?e.getPersistentData().getUUID(SbwCompat.TAG_DRONE_OWNER_UUID):null;}
 public static Identity identify(Entity source,Entity projectile){
  if(source==null)return null;var tag=source.getPersistentData();
  if(tag.contains(TYPE))return new Identity(source.getUUID(),tag.getString(TYPE),tag.getString(PAYLOAD),tag.getString(OWNER));
  if(!SbwCompat.isDrone(source)||source instanceof DeliveryDroneEntity||source instanceof Rd01Entity)return null;
  boolean inherent=source instanceof Fl01Entity||source instanceof Hx2Entity||source instanceof Id01Entity||source instanceof Geranium2Entity||source instanceof GlideBombEntity||source instanceof LucasDroneEntity&&!(source instanceof BayraktarTb2Entity);
  String item="";
  if(source instanceof com.atsuishio.superbwarfare.entity.vehicle.DroneEntity){var stack=SbwCompat.getDroneCurrentItem(source);if(!stack.isEmpty()){String key=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();var data=com.atsuishio.superbwarfare.data.CustomData.DRONE_ATTACHMENT.get(key);if(data!=null&&data.explosionDamage>0&&data.explosionRadius>0)item=key;}}
  if(item.isEmpty()&&projectile instanceof net.minecraft.world.entity.projectile.Projectile&&projectile!=source)item=projectile.getPersistentData().contains(PAYLOAD)?projectile.getPersistentData().getString(PAYLOAD):BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).toString();
  if(!inherent&&item.isEmpty())return null;
  UUID owner=owner(source);return new Identity(UUID.randomUUID(),BuiltInRegistries.ENTITY_TYPE.getKey(source.getType()).toString(),item,owner==null?"":owner.toString());
 }
 public static boolean validOverride(Object value){if(!(value instanceof String s))return false;int n=s.indexOf('=');if(n<1||ResourceLocation.tryParse(s.substring(0,n).trim())==null)return false;try{double d=Double.parseDouble(s.substring(n+1).trim());return Double.isFinite(d)&&d>=0&&d<=100;}catch(NumberFormatException e){return false;}}
 private static List<? extends String> lastTypes=List.of(),lastPayloads=List.of();private static Map<String,Double> types=Map.of(),payloads=Map.of();
 private static Map<String,Double> parse(List<? extends String> list){Map<String,Double> result=new HashMap<>();for(String s:list){int n=s.indexOf('=');result.put(s.substring(0,n).trim(),Double.parseDouble(s.substring(n+1).trim()));}return Map.copyOf(result);}
 public static double multiplier(Identity attack){if(attack==null)return 1;var t=DroneVehicleDamageConfig.TYPES.get();var p=DroneVehicleDamageConfig.PAYLOADS.get();if(!lastTypes.equals(t)){types=parse(t);lastTypes=List.copyOf(t);}if(!lastPayloads.equals(p)){payloads=parse(p);lastPayloads=List.copyOf(p);}return payloads.getOrDefault(attack.payload,types.getOrDefault(attack.drone,DroneVehicleDamageConfig.MULTIPLIER.get()));}
}
