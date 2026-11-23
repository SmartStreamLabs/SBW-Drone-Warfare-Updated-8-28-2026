package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
/** Server game values; native SBW hull, damage and ship physics remain authoritative. */
public final class Ndc01Config{
 public static final ForgeConfigSpec SPEC;public static final ForgeConfigSpec.DoubleValue SPEED;public static final ForgeConfigSpec.IntValue HEALTH,ENERGY,USE,IDLE,LAUNCH;
 static{var b=ConfigSchema.builder();SPEED=b.defineInRange("speedMultiplier",1d,.1,2);HEALTH=b.defineInRange("health",1200,50,100000);ENERGY=b.defineInRange("energyCapacityFE",2000000,1000,100000000);USE=b.comment("FE per second at full propulsion; scales with engine power.").defineInRange("propulsionFEPerSecond",800,0,100000);IDLE=b.defineInRange("idleFEPerSecond",4,0,10000);LAUNCH=b.defineInRange("launchEnergyFE",200,1,100000);SPEC=b.build();}
 private Ndc01Config(){}
}
