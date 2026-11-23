package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Client presentation only; does not change vehicle tuning. */
public final class DroneOsdConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue VISIBLE;
    public static final ModConfigSpec.DoubleValue SCALE;
    static {var b=ConfigSchema.builder();VISIBLE=b.comment("Show the shared drone camera OSD. F1 still hides it.").define("visible",true);SCALE=b.comment("OSD text/layout scale; automatically fits the camera viewport.").defineInRange("scale",1.0,.6,1.5);SPEC=b.build();}
    private DroneOsdConfig(){}
}
