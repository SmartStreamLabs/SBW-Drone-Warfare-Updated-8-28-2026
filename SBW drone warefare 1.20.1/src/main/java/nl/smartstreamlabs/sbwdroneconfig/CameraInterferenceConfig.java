package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
/** Local video presentation; no changes to RF range, controls or jammer strength. */
public final class CameraInterferenceConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue STRENGTH;
    public static final ForgeConfigSpec.BooleanValue STUTTERS;
    static {var b=ConfigSchema.builder();STRENGTH=b.comment("Drone video interference strength. Zero disables distortion; actual jamming and warnings remain unchanged.").defineInRange("effectStrength",1d,0d,1.5d);STUTTERS=b.comment("Allow occasional short video frame holds during stronger jamming.").define("frameStutters",true);SPEC=b.build();}
    private CameraInterferenceConfig(){}
}
