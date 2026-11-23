package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
/** Only native wheel/track/ship propulsion: custom aircraft and SD-01 retain their own controller settings. */
@Mod.EventBusSubscriber(modid="sbwdroneconfig",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class VehicleDriveConfig {
 public static final List<String> TYPES=List.of("vatag_ugv_v01","supply_rover_sr01","mine_rover_mr01","mobile_jammer_truck_mj03","drone_carrier_dc01","water_drone_wd01","naval_drone_carrier_ndc01","quadruped_drone_qd01","heavy_ground_drone_hgd01","anti_drone_vehicle_adv01","mobile_patriot_launcher_mpl01","ground_assault_drone_gad01");
 public record Fields(ForgeConfigSpec.DoubleValue forward,ForgeConfigSpec.DoubleValue reverse,ForgeConfigSpec.DoubleValue acceleration,ForgeConfigSpec.DoubleValue braking,ForgeConfigSpec.DoubleValue turning,ForgeConfigSpec.DoubleValue energy){}
 public record Factors(double forward,double reverse,double acceleration,double braking,double turning,double energy){}
 public static final Factors IDENTITY=new Factors(1,1,1,1,1,1);
 public static final ForgeConfigSpec SPEC;public static final Fields GENERAL;public static final Map<String,Fields> PROFILES;
 private static volatile Map<net.minecraft.resources.ResourceLocation,Factors> snapshot=Map.of();
 static{var b=ConfigSchema.builder();b.push("defaults");GENERAL=fields(b,false,"");b.pop();b.push("types");var profiles=new LinkedHashMap<String,Fields>();for(String type:TYPES){b.push(type);profiles.put(type,fields(b,true,type));b.pop();}b.pop();PROFILES=Map.copyOf(profiles);SPEC=b.build();}
 private static Fields fields(ForgeConfigSpec.Builder b,boolean perType,String type){return new Fields(field(b,"forwardSpeedMultiplier",perType),field(b,"reverseSpeedMultiplier",perType),field(b,"accelerationMultiplier",perType),field(b,"brakingMultiplier",perType),field(b,"turningMultiplier",perType),type.equals("naval_drone_carrier_ndc01")?null:field(b,"engineEnergyMultiplier",perType));}
 private static ForgeConfigSpec.DoubleValue field(ForgeConfigSpec.Builder b,String name,boolean perType){return b.comment("Bounded drive-physics adjustment (up to 16x) on the matching native engine parameter after datapack/per-instance engine data. A per-type zero inherits the general factor. Existing controller limits and power availability still apply. Storage capacities are not limited by this range.").defineInRange(name,perType?0d:1d,perType?0d:Double.MIN_NORMAL,16d);}
 private static Factors bake(Fields fields,Factors defaults){return new Factors(VehicleProfileValues.resolve(fields.forward().get(),defaults.forward()),VehicleProfileValues.resolve(fields.reverse().get(),defaults.reverse()),VehicleProfileValues.resolve(fields.acceleration().get(),defaults.acceleration()),VehicleProfileValues.resolve(fields.braking().get(),defaults.braking()),VehicleProfileValues.resolve(fields.turning().get(),defaults.turning()),fields.energy()==null?1:VehicleProfileValues.resolve(fields.energy().get(),defaults.energy()));}
 @SubscribeEvent public static void changed(ModConfigEvent event){if(event.getConfig().getSpec()!=SPEC)return;if(event instanceof ModConfigEvent.Unloading){snapshot=Map.of();return;}var general=bake(GENERAL,IDENTITY);var values=new LinkedHashMap<net.minecraft.resources.ResourceLocation,Factors>();for(var e:PROFILES.entrySet())values.put(net.minecraft.resources.ResourceLocation.tryParse("sbwdroneconfig:"+e.getKey()),bake(e.getValue(),general));snapshot=Map.copyOf(values);}
 public static Factors factors(net.minecraft.world.entity.Entity entity){return snapshot.getOrDefault(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),IDENTITY);}
 private VehicleDriveConfig(){}
}
