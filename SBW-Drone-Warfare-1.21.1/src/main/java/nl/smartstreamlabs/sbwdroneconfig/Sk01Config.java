package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.neoforge.common.ModConfigSpec;
public final class Sk01Config {
 public static final ModConfigSpec SPEC; public static final ModConfigSpec.BooleanValue ENABLED,AUTO_REMOVE,SHOVEL;
 public static final ModConfigSpec.IntValue AGE,WORK,MICROS; public static final ModConfigSpec.DoubleValue SCALE;public static final ModConfigSpec.ConfigValue<String>MODEL;
 static {var b=ConfigSchema.builder();b.comment("Persistent cosmetic remains for actual living deaths. Drones and vehicles are excluded. No loot or collision.");
 ENABLED=b.define("enabled",true);MODEL=b.comment("Resource namespace/path for the standard model pair; resource packs can replace soldier.bin and lod.bin. Never an entity filter.").define("defaultModel","sbwdroneconfig:sk01",v->v instanceof String s&&s.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")&&!s.contains(".."));
 SCALE=b.defineInRange("globalScale",1d,.1d,4d);AUTO_REMOVE=b.define("automaticCleanup",false);AGE=b.defineInRange("cleanupAfterSeconds",86400,1,31536000);SHOVEL=b.define("cleanupWithRS01",true);
 WORK=b.comment("Shared work units for all dimensions: placement probes, expiry and bounded chunk sync pages.").defineInRange("globalWorkUnitsPerTick",128,1,8192);MICROS=b.defineInRange("globalWorkMicrosecondsPerTick",1500,100,50000);SPEC=b.build();}
}
