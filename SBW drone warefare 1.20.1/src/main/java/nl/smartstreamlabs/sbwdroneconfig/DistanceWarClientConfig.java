package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class DistanceWarClientConfig {
    public static final ForgeConfigSpec SPEC;public static final ForgeConfigSpec.DoubleValue VOLUME;
    static{var b=ConfigSchema.builder();VOLUME=b.comment("Personal Distance War Sounds gain; zero mutes this feature. Master and Ambient volume still apply.").defineInRange("volume",1d,0,1);SPEC=b.build();}
    private DistanceWarClientConfig(){}
}
