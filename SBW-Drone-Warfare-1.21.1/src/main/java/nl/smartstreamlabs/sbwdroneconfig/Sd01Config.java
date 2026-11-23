package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Independent submarine settings; aircraft/ship defaults are not repurposed. Speeds are blocks/tick. */
public final class Sd01Config {
 public enum FailSafe { HOLD, SINK, SURFACE }
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.DoubleValue FORWARD,REVERSE,VERTICAL,ACCEL,TURN,LOOK,RANGE,WATER_RANGE;
 public static final ModConfigSpec.IntValue DRIVE_COST,HOLD_COST,LIGHT_COST,LIGHT_RANGE,SONAR_RANGE,SONAR_COOLDOWN,SONAR_COST,DEPTH_SCAN;
 public static final ModConfigSpec.BooleanValue JAMMERS;
 public static final ModConfigSpec.EnumValue<FailSafe> LOST,EMPTY;
 static {var b=ConfigSchema.builder();
  b.push("movement");FORWARD=b.defineInRange("forwardSpeed",.22,.03,.6);REVERSE=b.defineInRange("reverseSpeed",.10,.02,.3);VERTICAL=b.defineInRange("verticalSpeed",.075,.01,.18);ACCEL=b.defineInRange("acceleration",.012,.002,.05);TURN=b.defineInRange("turnDegreesPerTick",2.0,.25,4);LOOK=b.defineInRange("cameraDegreesPerTick",5d,1,12);b.pop();
  b.push("connection");RANGE=b.comment("Also bounded by the existing global droneMaxRange. No aircraft weather/water disconnect rules.").defineInRange("range",10000d,16,10000);WATER_RANGE=b.defineInRange("submergedRangeMultiplier",1d,.05,1);JAMMERS=b.define("ordinaryRfJammersAffectSubmarine",false);LOST=b.defineEnum("onLinkLoss",FailSafe.HOLD);EMPTY=b.comment("HOLD cannot create energy; without power HOLD becomes a gentle sink.").defineEnum("onEmptyBattery",FailSafe.SINK);b.pop();
  b.push("energy");DRIVE_COST=b.defineInRange("driveCostPerSecond",40,0,2000);HOLD_COST=b.defineInRange("depthHoldCostPerSecond",4,0,200);LIGHT_COST=b.defineInRange("lightsCostPerSecond",6,0,200);b.pop();
  b.push("sensors");LIGHT_RANGE=b.defineInRange("lightRange",12,4,20);SONAR_RANGE=b.defineInRange("sonarRange",24,8,48);SONAR_COOLDOWN=b.defineInRange("sonarCooldownTicks",100,40,400);SONAR_COST=b.defineInRange("sonarEnergy",80,0,2000);DEPTH_SCAN=b.comment("One vertical water column, at most once per second. A solid cover/unknown surface displays --.").defineInRange("depthColumnLimit",96,16,128);b.pop();SPEC=b.build();
 }
 private Sd01Config(){}
}
