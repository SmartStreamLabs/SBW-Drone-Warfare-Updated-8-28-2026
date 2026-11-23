package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Live balance multipliers over the existing data/controller values; registrations and inventories are untouched. */
@Mod.EventBusSubscriber(modid="sbwdroneconfig",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class VehicleProfilesConfig {
 public static final List<String> TYPES=List.of("superbwarfare:drone","sbwdroneconfig:cubed_fpv_drone","sbwdroneconfig:lucas_drone","sbwdroneconfig:delivery_drone","sbwdroneconfig:glide_bomb","sbwdroneconfig:geranium_2","sbwdroneconfig:heavy_bomber_drone","sbwdroneconfig:ukrainian_bayraktar_tb2","sbwdroneconfig:interceptor_drone_id01","sbwdroneconfig:rescue_drone_rd01","sbwdroneconfig:vatag_ugv_v01","sbwdroneconfig:supply_rover_sr01","sbwdroneconfig:mine_rover_mr01","sbwdroneconfig:medical_trailer_mt01","sbwdroneconfig:mobile_jammer_truck_mj03","sbwdroneconfig:drone_carrier_dc01","sbwdroneconfig:water_drone_wd01","sbwdroneconfig:naval_drone_carrier_ndc01","sbwdroneconfig:submarine_drone_sd01","sbwdroneconfig:quadruped_drone_qd01","sbwdroneconfig:heavy_ground_drone_hgd01","sbwdroneconfig:anti_drone_vehicle_adv01","sbwdroneconfig:mobile_patriot_launcher_mpl01","sbwdroneconfig:hx2_drone","sbwdroneconfig:flamingo_fl01","sbwdroneconfig:ground_assault_drone_gad01");
 public record Fields(ForgeConfigSpec.DoubleValue health,ForgeConfigSpec.DoubleValue incomingDamage){}
 public static final ForgeConfigSpec SPEC;
 public static final Fields GENERAL;
 public static final Map<String,Fields> PROFILES;
 private static volatile Map<net.minecraft.resources.ResourceLocation,VehicleProfileValues.Factors> snapshot=Map.of();
 static {
  var b=ConfigSchema.builder();b.push("defaults");GENERAL=fields(b,false);b.pop();
  var profiles=new LinkedHashMap<String,Fields>();b.push("types");
  for(String type:TYPES){b.push(type.replace(':','_'));profiles.put(type,fields(b,true));b.pop();}b.pop();
  PROFILES=Collections.unmodifiableMap(profiles);SPEC=b.build();
 }
 private static Fields fields(ForgeConfigSpec.Builder b,boolean override){
  double initial=override?0:1,minimum=override?0:Double.MIN_NORMAL;
  return new Fields(b.comment("Hull health multiplier on existing native/datapack or model health. A per-type zero inherits the general multiplier. Changing this never heals existing damage; float engine limits still apply.").defineInRange("hullHealthMultiplier",initial,minimum,Double.MAX_VALUE),
    b.comment("Incoming damage multiplier after native armor and VC-01 cage protection. A per-type zero inherits the general multiplier. Applies only to the listed Drone Warfare vehicle types.").defineInRange("incomingDamageMultiplier",initial,minimum,Double.MAX_VALUE));
 }
 @SubscribeEvent public static void changed(ModConfigEvent event){if(event.getConfig().getSpec()!=SPEC)return;if(event instanceof ModConfigEvent.Unloading){snapshot=Map.of();return;}var values=new LinkedHashMap<net.minecraft.resources.ResourceLocation,VehicleProfileValues.Factors>();double health=GENERAL.health().get(),damage=GENERAL.incomingDamage().get();for(var entry:PROFILES.entrySet())values.put(net.minecraft.resources.ResourceLocation.tryParse(entry.getKey()),new VehicleProfileValues.Factors(VehicleProfileValues.resolve(entry.getValue().health().get(),health),VehicleProfileValues.resolve(entry.getValue().incomingDamage().get(),damage)));snapshot=Map.copyOf(values);}
 public static VehicleProfileValues.Factors factors(net.minecraft.resources.ResourceLocation id){return snapshot.getOrDefault(id,VehicleProfileValues.IDENTITY);}
 public static String inheritedFrom(ConfigCatalog.Definition d){if(!Set.of("VehicleProfilesConfig","VehicleDriveConfig").contains(d.family())||d.path().size()!=3)return "";return d.family()+":defaults."+d.path().get(2);}
 private VehicleProfilesConfig(){}
}
