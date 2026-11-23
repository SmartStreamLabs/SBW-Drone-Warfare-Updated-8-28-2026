package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Rd01Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue SPEED, CLIMB, ACCELERATION, WINCH, PICKUP;
    static {
        var b=ConfigSchema.builder();
        SPEED=b.comment("RD-01 aerial speed cap, blocks/tick; native SBW drone propulsion is retained.").defineInRange("maximumFlightSpeed",.32,.05,.7);
        CLIMB=b.defineInRange("maximumVerticalSpeed",.16,.03,.3);
        ACCELERATION=b.defineInRange("maximumAcceleration",.025,.005,.06);
        WINCH=b.comment("Cable extension speed, blocks/tick. Harness range 0..2.5 blocks; supplied bind at 0.25.").defineInRange("winchSpeed",.025,.005,.06);
        PICKUP=b.comment("Maximum player distance from the harness interaction anchor.").defineInRange("pickupDistance",2.3,1,3);
        SPEC=b.build();
    }
    private Rd01Config() {}
}
