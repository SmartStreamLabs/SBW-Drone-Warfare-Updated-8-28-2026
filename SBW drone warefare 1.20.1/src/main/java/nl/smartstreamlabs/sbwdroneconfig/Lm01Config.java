package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Lm01Config {
 public static final ForgeConfigSpec SPEC;
 public static final ForgeConfigSpec.BooleanValue PLAYERS,MOBS,VEHICLES,WORLD_DAMAGE;
 public static final ForgeConfigSpec.IntValue ARM_TICKS,EXPLOSIONS,MICROS;
 public static final ForgeConfigSpec.DoubleValue DAMAGE,RADIUS;
 static {
  var b=ConfigSchema.builder();
  ARM_TICKS=b.comment("One scheduled arming tick; armed mines have no ticker or radius scan.").defineInRange("armingDelayTicks",40,1,1200);
  PLAYERS=b.define("triggerPlayers",true);MOBS=b.define("triggerLivingMobs",true);VEHICLES=b.define("triggerGroundVehicles",true);
  DAMAGE=b.defineInRange("explosionDamage",70d,0,1000);RADIUS=b.defineInRange("explosionRadius",3.5d,.5,12);
  WORLD_DAMAGE=b.comment("Also requires SBW explosion destruction. Existing rubble mode, gamerules and explosion protection events still apply.").define("worldDamage",true);
  EXPLOSIONS=b.comment("One shared limit across dimensions. Excess triggered mines retain a persistent scheduled tick; never a recursive chain.").defineInRange("maxExplosionsPerServerTick",4,1,32);
  MICROS=b.comment("Soft limit between atomic explosions; an already running explosion finishes.").defineInRange("explosionWorkMicroseconds",2000,100,50000);
  SPEC=b.build();
 }
 private Lm01Config(){}
}
