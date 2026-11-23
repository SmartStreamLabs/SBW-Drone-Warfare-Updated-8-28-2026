package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Server-authoritative game tuning, in blocks/tick and ticks. */
public final class Id01Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue SPEED, ACCELERATION, TURN, RANGE, BLAST, IMPACT, MIN_SPEED;
    public static final ModConfigSpec.IntValue RESERVE, DURATION;
    static {
        var b=ConfigSchema.builder();
        MIN_SPEED=b.comment("Minimum active forward flight speed in blocks/tick.").defineInRange("minimumForwardSpeed",.4,.1,1.5);
        SPEED=b.defineInRange("flightSpeed",.85,.1,2.0);
        ACCELERATION=b.defineInRange("acceleration",.045,.005,.15);
        TURN=b.defineInRange("turnDegreesPerTick",5.0,1.0,15.0);
        RANGE=b.comment("Loaded drone targets only; never loads the target's route.").defineInRange("radarRange",512.0,16.0,2048.0);
        BLAST=b.comment("Contact blast; existing crash block-damage policy is respected.").defineInRange("contactBlastRadius",3.2,1.0,8.0);
        IMPACT=b.comment("Minimum selected-target impact damage. A permitted contact consumes both drones in one combined blast; normal hurt/protection hooks still apply.").defineInRange("contactDamage",24.0,0.0,200.0);
        RESERVE=b.comment("Minimum return reserve in existing drone FE; travel-to-home costs are added dynamically.").defineInRange("returnReserve",200,0,1000000);
        DURATION=b.defineInRange("maxFlightTicks",6000,200,72000);
        SPEC=b.build();
    }
}
