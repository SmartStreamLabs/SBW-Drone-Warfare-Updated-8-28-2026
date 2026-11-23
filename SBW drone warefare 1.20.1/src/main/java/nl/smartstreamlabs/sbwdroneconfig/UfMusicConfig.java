package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class UfMusicConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue RANGE;
    public static final ForgeConfigSpec.BooleanValue PUBLIC;
    static {var b=ConfigSchema.builder();b.push("UF01Music");
        RANGE=b.comment("Default audible radius for newly placed/legacy flagpoles, independent of volume. Operators can change each flag in its menu.").defineInRange("defaultRangeBlocks",64,8,256);
        PUBLIC=b.comment("Allow everyone nearby to operate music. Otherwise owner, allied team and operators only. Legacy unowned flags require an operator.").define("publicControls",false);
        b.pop();SPEC=b.build();}
    private UfMusicConfig(){}
}
