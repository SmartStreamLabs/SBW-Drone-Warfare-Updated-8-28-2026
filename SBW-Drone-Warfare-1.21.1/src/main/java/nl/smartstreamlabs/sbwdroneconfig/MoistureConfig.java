package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.neoforged.neoforge.common.ModConfigSpec;
public final class MoistureConfig {
 public enum Profile {LIMITED,STORM_ONLY,RAINPROOF,WATERPROOF,SUBMERSIBLE,EXCLUDED}
 public static final Map<String,ModConfigSpec.EnumValue<Profile>> PROFILES=new LinkedHashMap<>();
 public static final ModConfigSpec SPEC;public static final ModConfigSpec.BooleanValue ENABLED,LENS,DAMAGE;
 public static final ModConfigSpec.DoubleValue RAIN,STORM,DRY,WARN,CAMERA,MOTOR,SEVERE,DAMAGE_FRACTION;public static final ModConfigSpec.IntValue DAMAGE_SECONDS;
 public static void model(ModConfigSpec.Builder b,String id,Profile p){PROFILES.put(id,b.defineEnum(id.replace(':','_'),p));}
 static{var b=ConfigSchema.builder();b.push("moisture");ENABLED=b.define("enabled",true);LENS=b.define("cameraDroplets",true);DAMAGE=b.define("permanentDamage",true);
 RAIN=b.comment("Percentage points per simulated second for LIMITED profiles in ordinary exposed vanilla rain.").defineInRange("ordinaryRainPerSecond",.10,0,10);STORM=b.comment("Vanilla rain during thunderstorm is a gameplay category, not measured local intensity.").defineInRange("thunderRainPerSecond",.20,0,10);DRY=b.defineInRange("dryPerSecond",.05,.001,10);WARN=b.defineInRange("warningPercent",25d,1,100);CAMERA=b.defineInRange("cameraFaultPercent",50d,1,100);MOTOR=b.defineInRange("motorFaultPercent",80d,1,100);SEVERE=b.defineInRange("damagePercent",90d,1,100);DAMAGE_SECONDS=b.defineInRange("damageIntervalSeconds",15,1,600);DAMAGE_FRACTION=b.comment("Fraction of native maximum hull health per damage event; only while severely wet and still exposed.").defineInRange("damageFraction",.01,0,.1);
 b.push("models");model(b,"superbwarfare:drone",Profile.LIMITED);model(b,"sbwdroneconfig:cubed_fpv_drone",Profile.LIMITED);
 for(String id:new String[]{"heavy_bomber_drone","delivery_drone","rescue_drone_rd01"})model(b,"sbwdroneconfig:"+id,Profile.STORM_ONLY);
 for(String id:new String[]{"lucas_drone","geranium_2","glide_bomb","ukrainian_bayraktar_tb2","hx2_drone","interceptor_drone_id01","quadruped_drone_qd01","heavy_ground_drone_hgd01","ground_assault_drone_gad01","vatag_ugv_v01","supply_rover_sr01","mine_rover_mr01","drone_carrier_dc01"})model(b,"sbwdroneconfig:"+id,Profile.RAINPROOF);
 model(b,"sbwdroneconfig:water_drone_wd01",Profile.WATERPROOF);model(b,"sbwdroneconfig:naval_drone_carrier_ndc01",Profile.WATERPROOF);model(b,"sbwdroneconfig:submarine_drone_sd01",Profile.SUBMERSIBLE);
 for(String id:new String[]{"anti_drone_vehicle_adv01","mobile_patriot_launcher_mpl01","mobile_jammer_truck_mj03"})model(b,"sbwdroneconfig:"+id,Profile.EXCLUDED);
 b.pop();b.pop();SPEC=b.build();}
 private MoistureConfig(){}
}
