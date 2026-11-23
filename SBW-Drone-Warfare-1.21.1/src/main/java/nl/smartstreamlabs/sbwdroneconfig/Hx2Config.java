package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class Hx2Config {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.DoubleValue MIN_SPEED,SPEED,ACCELERATION,TURN,PITCH,BLAST;
 public static final ModConfigSpec.IntValue CAPACITY,USE,RANGE,LIFETIME;
 static {var b=ConfigSchema.builder();
  MIN_SPEED=b.comment("Minimum powered forward speed, blocks/tick.").defineInRange("minimumSpeed",.5,.1,3);
  SPEED=b.defineInRange("maximumSpeed",1.8,.2,6);ACCELERATION=b.defineInRange("acceleration",.065,.005,.3);
  TURN=b.comment("Maximum yaw change in degrees per server tick.").defineInRange("turnRate",2.8,.1,8);PITCH=b.defineInRange("pitchRate",1.5,.1,5);
  CAPACITY=b.defineInRange("energyCapacity",36000,100,10000000);USE=b.defineInRange("flightEnergyPerTick",6,1,10000);
  RANGE=b.comment("HX-2 RF limit; the existing server/weather limit still applies.").defineInRange("controlRange",10000,16,1000000);
  LIFETIME=b.comment("Maximum active flight ticks, followed by an unpowered glide.").defineInRange("flightLifetimeTicks",6000,100,72000);
  BLAST=b.comment("Game explosion strength; uses the existing attack damage and cage pipeline.").defineInRange("explosionPower",7.0,.1,20);SPEC=b.build();}
 private Hx2Config(){}
}
