package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.bus.api.SubscribeEvent;
/** Existing armor values are the defaults; settings never replace item registrations or erase stored wear. */
@EventBusSubscriber(modid="sbwdroneconfig",bus=EventBusSubscriber.Bus.MOD)
public final class UniformArmorConfig {
 public record Fields(ModConfigSpec.DoubleValue head,ModConfigSpec.DoubleValue chest,ModConfigSpec.DoubleValue legs,ModConfigSpec.DoubleValue boots,ModConfigSpec.DoubleValue armor,ModConfigSpec.DoubleValue durability,ModConfigSpec.BooleanValue wear){}
 public record Settings(double head,double chest,double legs,double boots,double armor,double durability,boolean wear){}
 public static final ModConfigSpec SPEC;public static final Map<String,Fields> SETS;public static final ModConfigSpec.IntValue REFRESH;
 private static volatile Map<String,Settings> snapshot;
 static{var b=ConfigSchema.builder();REFRESH=b.comment("One shared budget for refreshing equipped uniform attributes after a config change. Existing equipment is indexed; no world/entity scan per tick.").defineInRange("refreshWearersPerTick",4,1,256);var sets=new LinkedHashMap<String,Fields>();for(String name:List.of("mu01","us01")){b.push(name);sets.put(name,new Fields(b.defineInRange("helmetBulletResistance",.17,0,1),b.defineInRange("vestBulletResistance",.7,0,1),b.defineInRange("trousersBulletResistance",.08,0,1),b.defineInRange("bootsBulletResistance",.05,0,1),b.comment("Multiplier on the existing iron-material armor attribute, separate from SBW bullet resistance.").defineInRange("armorMultiplier",1d,0,4),b.comment("Multiplier on each item's existing maximum durability. Stored item damage is retained. Native integer limits apply.").defineInRange("durabilityMultiplier",1d,.01,1000000),b.comment("Reduce SBW bullet resistance in proportion to remaining item durability.").define("wearAffectsResistance",true)));b.pop();}SETS=Map.copyOf(sets);SPEC=b.build();snapshot=bake(false);}
 private static Map<String,Settings> bake(boolean loaded){var out=new LinkedHashMap<String,Settings>();for(var e:SETS.entrySet()){var f=e.getValue();out.put(e.getKey(),new Settings(read(f.head(),loaded),read(f.chest(),loaded),read(f.legs(),loaded),read(f.boots(),loaded),read(f.armor(),loaded),read(f.durability(),loaded),loaded?f.wear().get():f.wear().getDefault()));}return Map.copyOf(out);}
 private static double read(ModConfigSpec.DoubleValue value,boolean loaded){return loaded?value.get():value.getDefault();}
 @SubscribeEvent public static void changed(ModConfigEvent event){if(event.getConfig().getSpec()!=SPEC)return;var next=bake(!(event instanceof ModConfigEvent.Unloading));if(!next.equals(snapshot)){snapshot=next;UniformArmorRefresh.changed();}}
 public static Settings settings(String set){return snapshot.get(set);}
 public static double bullet(String set,EquipmentSlot slot,int damage,int maximum){var s=settings(set);double base=switch(slot){case HEAD->s.head();case CHEST->s.chest();case LEGS->s.legs();case FEET->s.boots();default->0;};return s.wear()?base*Math.max(0,1-damage/(double)Math.max(1,maximum)):base;}
 public static int durability(String set,int nativeMaximum){if(nativeMaximum<=0)return nativeMaximum;return (int)Math.max(1,Math.min(Integer.MAX_VALUE,Math.round(nativeMaximum*settings(set).durability())));}
 private UniformArmorConfig(){}
}
