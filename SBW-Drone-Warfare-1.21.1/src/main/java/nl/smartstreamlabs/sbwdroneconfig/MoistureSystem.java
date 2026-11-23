package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.world.entity.Entity;import net.minecraft.server.level.ServerPlayer;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.nbt.CompoundTag;import net.minecraft.world.item.ItemStack;
public final class MoistureSystem {
 public static final String TAG="DwMoisture";private static final Set<String> NO_LENS=Set.of("submarine_drone_sd01","anti_drone_vehicle_adv01","mobile_patriot_launcher_mpl01","mobile_jammer_truck_mj03");private static final Map<Entity,Long> LAST=new WeakHashMap<>();private static final Map<ServerPlayer,Map<UUID,Long>> SENT=new WeakHashMap<>();
 public static MoistureConfig.Profile profile(Entity e){var c=MoistureConfig.PROFILES.get(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());return c==null?MoistureConfig.Profile.EXCLUDED:c.get();}
 public static boolean vulnerable(MoistureConfig.Profile p){return p==MoistureConfig.Profile.LIMITED||p==MoistureConfig.Profile.STORM_ONLY;}
 public static boolean lens(Entity e){String id=BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();return !NO_LENS.contains(id)&&profile(e)!=MoistureConfig.Profile.EXCLUDED;}
 public static CompoundTag state(Entity e){var root=e.getPersistentData();if(!root.contains(TAG))root.put(TAG,new CompoundTag());return root.getCompound(TAG);}
 public static void tick(Entity e){if(e.level().isClientSide||!(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)||Math.floorMod(e.tickCount+e.getId(),20)!=0)return;if(!MoistureConfig.PROFILES.containsKey(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString()))return;
  long now=e.level().getGameTime();Long previous=LAST.put(e,now);double dt=previous==null?1:Math.max(0,Math.min(20,now-previous))/20d;
  var p=profile(e);var s=state(e);boolean enabled=MoistureConfig.ENABLED.get()&&AddonConfig.weatherEffectsEnabled();var weather=enabled?MoistureWeather.VANILLA.at(e):new MoistureWeather.Sample(false,false);
  boolean susceptible=enabled&&vulnerable(p);boolean exposed=weather.rain();boolean accumulating=susceptible&&exposed&&(p==MoistureConfig.Profile.LIMITED||weather.thunder());
  double wet=susceptible?MoistureMath.step(s.getDouble("Wet"),accumulating?(weather.thunder()?MoistureConfig.STORM.get():MoistureConfig.RAIN.get()):-MoistureConfig.DRY.get(),dt):0;
  double glass=enabled&&MoistureConfig.LENS.get()&&lens(e)?MoistureMath.step(s.getDouble("Lens"),exposed?12:-6,dt):0;
  s.putDouble("Wet",wet);s.putDouble("Lens",glass);s.putBoolean("Rain",exposed);int stage=!susceptible?0:MoistureMath.stage(wet,MoistureConfig.WARN.get(),MoistureConfig.CAMERA.get(),MoistureConfig.MOTOR.get(),MoistureConfig.SEVERE.get());s.putInt("Stage",stage);
  double timer=accumulating&&stage==4?s.getDouble("DamageTime")+dt:0;
  if(timer>=MoistureConfig.DAMAGE_SECONDS.get()){timer=0;if(MoistureConfig.DAMAGE.get()&&!e.isInvulnerable()&&e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&!v.isWreck()){double hp=v.getMaxHealth();v.setLastDamageSource(e.damageSources().generic());v.setLastDamageStamp(now);v.onHurt((float)(hp*MoistureConfig.DAMAGE_FRACTION.get()),null,false);}}
  s.putDouble("DamageTime",timer);
  if(susceptible&&stage>=3&&!e.onGround()&&!Dc01Docking.docked(e)){double f=MoistureMath.severity(wet,MoistureConfig.MOTOR.get());var v=e.getDeltaMovement();e.setDeltaMovement(v.x*(1-.04*f),v.y-.015*f,v.z*(1-.04*f));}
  AddonNetwork.moistureTracking(e,snapshot(e));
 }
 public static MoisturePacket snapshot(Entity e){var s=state(e);return new MoisturePacket(e.getId(),e.getUUID(),e.level().dimension().location().toString(),(float)s.getDouble("Wet"),(float)s.getDouble("Lens"),s.getBoolean("Rain"),s.getInt("Stage"));}
 public static void send(ServerPlayer p,Entity e){if(e==null||!MoistureConfig.PROFILES.containsKey(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString()))return;var map=SENT.computeIfAbsent(p,k->new LinkedHashMap<>());long now=p.serverLevel().getGameTime();if(now-map.getOrDefault(e.getUUID(),-100L)<20)return;if(map.size()>=32)map.clear();map.put(e.getUUID(),now);AddonNetwork.moistureTo(p,snapshot(e));}
 public static void save(Entity e,CompoundTag n){if(e.getPersistentData().contains(TAG))n.put(TAG,state(e).copy());}
 public static void load(Entity e,CompoundTag n){if(n.contains(TAG)){var s=n.getCompound(TAG).copy();s.putDouble("DamageTime",0);e.getPersistentData().put(TAG,s);}if(n.contains("DwMoistureHealth")&&e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v)v.setHealth(n.getFloat("DwMoistureHealth"));}
 public static ItemStack pack(Entity e,ItemStack stack){CompoundTag tag=new CompoundTag();tag.putString("id",BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());save(e,tag);if(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v)tag.putFloat("DwMoistureHealth",v.getHealth());
  var prior=stack.getOrDefault(net.minecraft.core.component.DataComponents.ENTITY_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();prior.merge(tag);stack.set(net.minecraft.core.component.DataComponents.ENTITY_DATA,net.minecraft.world.item.component.CustomData.of(prior));return stack;}
 private MoistureSystem(){}
}

