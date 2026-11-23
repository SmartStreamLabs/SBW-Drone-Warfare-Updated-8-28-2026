package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class Gad01Config {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.DoubleValue SPEED,STEP,ONE_DAMAGE,TWO_DAMAGE,TWO_RADIUS;
 static{var b=ConfigSchema.builder();SPEED=b.comment("Multiplier for the native SBW wheel engine; baseline about 0.18 blocks/tick.").defineInRange("driveSpeedMultiplier",3d,.2,6);STEP=b.comment("Native collision step height in blocks. No wall climbing.").defineInRange("obstacleStepHeight",1.05d,0,1.25);ONE_DAMAGE=b.comment("One loaded mine: multiplier on the native SBW mine explosion damage.").defineInRange("oneMineDamageScale",1d,0,3);TWO_DAMAGE=b.comment("Two loaded mines: multiplier on their average native damage. Exactly one explosion.").defineInRange("twoMineDamageScale",1.6d,0,5);TWO_RADIUS=b.comment("Two loaded mines: multiplier on the larger native radius. One mine retains its native radius.").defineInRange("twoMineRadiusScale",1.15d,.1,2);SPEC=b.build();}
 private Gad01Config(){}
}
