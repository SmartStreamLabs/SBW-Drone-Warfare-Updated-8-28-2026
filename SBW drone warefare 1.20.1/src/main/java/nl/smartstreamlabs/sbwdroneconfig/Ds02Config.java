package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class Ds02Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue RANGE;
    static{var b=ConfigSchema.builder();RANGE=b.comment("DS-02 positional hearing radius in blocks. Requires the siren chunk to be loaded on the client; never loads chunks for sound.").defineInRange("audibleRangeBlocks",96.0,8.0,512.0);SPEC=b.build();}
}
