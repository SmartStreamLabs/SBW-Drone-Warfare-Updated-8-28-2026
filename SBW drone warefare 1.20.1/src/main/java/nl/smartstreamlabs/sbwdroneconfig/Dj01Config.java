package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server settings belonging exclusively to the new directional handheld jammer. */
public final class Dj01Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED, AFFECT_OWN, AFFECT_ALLIES;
    public static final ForgeConfigSpec.DoubleValue RANGE, CONE;
    public static final ForgeConfigSpec.IntValue COST;
    static {
        var b=ConfigSchema.builder();
        ENABLED=b.comment("Enable DJ-01 emission. The existing global jammer enable also applies.").define("enabled",true);
        RANGE=b.comment("DJ-01 maximum beam distance in blocks. Does not change drone radio range.").defineInRange("maxDistance",128d,2d,512d);
        CONE=b.comment("Full DJ-01 cone angle in degrees (half on each side of the sight).").defineInRange("coneDegrees",24d,2d,90d);
        COST=b.comment("Native SBW/FE energy consumed for each powered emission tick. Capacity: 100000 FE.").defineInRange("energyPerTick",40,1,100000);
        AFFECT_OWN=b.comment("Allow DJ-01 to affect its operator's own drones.").define("affectOwnDrones",true);
        AFFECT_ALLIES=b.comment("Allow DJ-01 to affect drones owned or controlled by allied scoreboard-team players.").define("affectAlliedDrones",false);
        SPEC=b.build();
    }
    private Dj01Config(){}
}
