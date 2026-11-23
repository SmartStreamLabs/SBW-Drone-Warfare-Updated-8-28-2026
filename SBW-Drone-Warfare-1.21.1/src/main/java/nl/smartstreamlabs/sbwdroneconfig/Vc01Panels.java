package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
/** Saved in native vehicle persistent data, which SBW carries in its vehicle container. */
public final class Vc01Panels {
 public static final String TAG="sbwdroneconfigVC01PanelsV1";
 public record Health(double current,double maximum){public double fraction(){return Math.max(0,Math.min(1,current/maximum));}}
 public static String label(String id){String name=id.substring(id.indexOf(':')+1).replace("_welded_mesh","").replace("truck_","").replace('_',' ');return name.substring(0,1).toUpperCase(java.util.Locale.ROOT)+name.substring(1);}
 public static String id(int branch,String panel){return branch+":"+panel;}
 public static Health health(Entity e,String key){CompoundTag root=e.getPersistentData().getCompound(TAG);if(!root.contains(key)){double max=Vc01Settings.HEALTH.get();CompoundTag tag=new CompoundTag();tag.putDouble("health",max);tag.putDouble("maximum",max);root.put(key,tag);e.getPersistentData().put(TAG,root);}CompoundTag t=root.getCompound(key);double max=t.getDouble("maximum");if(!Double.isFinite(max)||max<=0)max=Vc01Settings.HEALTH.get();double hp=t.getDouble("health");if(!e.level().isClientSide()&&max!=Vc01Settings.HEALTH.get()){hp=Math.max(0,Math.min(1,hp/max))*Vc01Settings.HEALTH.get();max=Vc01Settings.HEALTH.get();set(e,key,new Health(hp,max));}return new Health(Double.isFinite(hp)?Math.max(0,Math.min(max,hp)):0,max);}
 public static void set(Entity e,String key,Health hp){CompoundTag root=e.getPersistentData().getCompound(TAG),t=new CompoundTag();t.putDouble("health",Math.max(0,Math.min(hp.maximum,hp.current)));t.putDouble("maximum",hp.maximum);root.put(key,t);e.getPersistentData().put(TAG,root);}
 public static void damage(Entity e,String key,double amount){Health h=health(e,key);set(e,key,new Health(h.current-Math.max(0,amount),h.maximum));}
 public static Map<String,Health> snapshot(Entity e){var profile=Vc01Geometry.profile(e);Map<String,Health> result=new LinkedHashMap<>();if(profile!=null)for(int b=0;b<profile.branches().size();b++)for(String key:profile.branches().get(b).panelIds())result.computeIfAbsent(id(b,key),k->health(e,k));return Map.copyOf(result);}
 public static CompoundTag packet(Entity e){snapshot(e);return e.getPersistentData().getCompound(TAG).copy();}
 public static void receive(Entity e,CompoundTag tag){e.getPersistentData().put(TAG,tag.copy());}
 public static double factor(double maximum,double fraction){return 1-Math.max(0,Math.min(1,maximum))*Math.max(0,Math.min(1,fraction));}
}
