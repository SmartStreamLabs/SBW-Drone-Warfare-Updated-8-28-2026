package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class MoistureClientConfig {
 public static final ModConfigSpec SPEC;public static final ModConfigSpec.BooleanValue DROPS,LIGHT;
 static{var b=ConfigSchema.builder();DROPS=b.define("cameraDroplets",true);LIGHT=b.define("lightweightDroplets",true);SPEC=b.build();}
}
