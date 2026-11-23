package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Vehicle-specific game settings; native SBW data continues to own armor, ammunition and drive physics. */
public final class Hgd01Config {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.DoubleValue SPEED,LASER_RANGE,LASER_DAMAGE;
 public static final ModConfigSpec.IntValue LASER_COST,LASER_INTERVAL;
 static {var b=ConfigSchema.builder();b.push("HGD01");SPEED=b.comment("Multiplier for this vehicle's native SBW tracked-drive speed.").defineInRange("driveSpeedMultiplier",1d,.1,3);LASER_RANGE=b.comment("Physical anti-drone beam range in blocks; does not change radio range.").defineInRange("laserRange",128d,8,256);LASER_DAMAGE=b.comment("Damage per emitter per server damage pulse, only to hostile drones.").defineInRange("laserDamage",6d,0,100);LASER_COST=b.defineInRange("laserEnergyPerEmitterPulse",400,1,100000);LASER_INTERVAL=b.defineInRange("laserPulseTicks",4,2,40);b.pop();SPEC=b.build();}
 private Hgd01Config(){}
}
