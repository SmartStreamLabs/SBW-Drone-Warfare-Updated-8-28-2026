package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class MoistureClientConfig {
 public static final ForgeConfigSpec SPEC;public static final ForgeConfigSpec.BooleanValue DROPS,LIGHT;
 static{var b=ConfigSchema.builder();DROPS=b.define("cameraDroplets",true);LIGHT=b.define("lightweightDroplets",true);SPEC=b.build();}
}
