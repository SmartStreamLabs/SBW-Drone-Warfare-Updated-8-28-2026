package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.neoforge.common.ModConfigSpec;
public final class Sk01ClientConfig {
 public static final ModConfigSpec SPEC;public static final ModConfigSpec.IntValue DISTANCE,LOD,MAX;
 static {var b=ConfigSchema.builder();DISTANCE=b.defineInRange("renderDistance",96,8,512);LOD=b.defineInRange("lodDistance",20,2,256);MAX=b.comment("Draw budget only; never deletes server records.").defineInRange("maximumDrawnRemains",256,0,4096);SPEC=b.build();}
}
