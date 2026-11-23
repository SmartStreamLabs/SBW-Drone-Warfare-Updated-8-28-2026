package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class DwfConfig{
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.LongValue INPUT_ITEMS,OUTPUT_ITEMS;
 public static final ModConfigSpec.IntValue INPUT_VARIANTS,OUTPUT_VARIANTS,WORK_UNITS,WORK_MICROS,COMPLETIONS,RECIPE_CHECKS,TRANSPORT_UPDATES;
 public static final ModConfigSpec.BooleanValue INPUT_INSERT,INPUT_EXTRACT,OUTPUT_INSERT,OUTPUT_EXTRACT;
 public static final ModConfigSpec.DoubleValue SOUND_VOLUME,PRODUCTION_SPEED;
 static{var b=ConfigSchema.builder();
  INPUT_ITEMS=b.defineInRange("inputItemCapacity",64_000_000L,1,Long.MAX_VALUE);OUTPUT_ITEMS=b.defineInRange("outputItemCapacity",64_000_000L,1,Long.MAX_VALUE);
  INPUT_VARIANTS=b.defineInRange("inputVariantCapacity",65536,1,1_048_576);OUTPUT_VARIANTS=b.defineInRange("outputVariantCapacity",65536,1,1_048_576);
  b.comment("One shared allowance for ALL loaded fabricators on this server, across dimensions. Units are bounded tasks, not machines.");
  WORK_UNITS=b.defineInRange("globalWorkUnitsPerTick",128,1,4096);WORK_MICROS=b.defineInRange("globalWorkMicrosecondsPerTick",1500,50,10000);
  RECIPE_CHECKS=b.defineInRange("globalRecipeChecksPerTick",32,1,512);TRANSPORT_UPDATES=b.defineInRange("globalTransportUpdatesPerTick",32,1,512);COMPLETIONS=b.defineInRange("globalCompletionsPerTick",8,1,128);
  INPUT_INSERT=b.define("automationInputInsert",true);INPUT_EXTRACT=b.define("automationInputExtract",false);OUTPUT_INSERT=b.define("automationOutputInsert",false);OUTPUT_EXTRACT=b.define("automationOutputExtract",true);
  PRODUCTION_SPEED=b.comment("Crafting speed multiplier. The shared completion/work budget still applies to all machines.").defineInRange("productionSpeedMultiplier",2d,.25,8);SOUND_VOLUME=b.defineInRange("machineSoundVolume",0.35,0,1);SPEC=b.build();
 }
 private DwfConfig(){}
}
