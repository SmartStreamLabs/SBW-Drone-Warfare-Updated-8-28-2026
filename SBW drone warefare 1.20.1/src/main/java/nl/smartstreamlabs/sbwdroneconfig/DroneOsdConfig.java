package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
/** Client presentation only; does not change vehicle tuning. */
public final class DroneOsdConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue VISIBLE;
    public static final ForgeConfigSpec.DoubleValue SCALE;
    static {var b=ConfigSchema.builder();VISIBLE=b.comment("Show the shared drone camera OSD. F1 still hides it.").define("visible",true);SCALE=b.comment("OSD text/layout scale; automatically fits the camera viewport.").defineInRange("scale",1.0,.6,1.5);SPEC=b.build();}
    private DroneOsdConfig(){}
}
