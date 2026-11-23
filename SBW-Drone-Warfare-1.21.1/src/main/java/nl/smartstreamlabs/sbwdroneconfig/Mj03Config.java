package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class Mj03Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue ENERGY_PER_TICK;
    static{var b=ConfigSchema.builder();ENERGY_PER_TICK=b.comment("SBW energy consumed per deployed MJ-03 per server tick. Radius is fixed at 12000 blocks.").defineInRange("jammerEnergyPerTick",80,1,10000);SPEC=b.build();}
}
