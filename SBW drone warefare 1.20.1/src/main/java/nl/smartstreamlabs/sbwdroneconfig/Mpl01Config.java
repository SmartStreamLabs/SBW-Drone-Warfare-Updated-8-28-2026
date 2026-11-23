package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
/** Minecraft game balance only. No real-world missile parameters. */
public final class Mpl01Config {
 public static final ForgeConfigSpec SPEC;public static final ForgeConfigSpec.IntValue SCAN,DEPLOY,RELOAD,LIFE,MAX_MISSILES,QUALITY;public static final ForgeConfigSpec.DoubleValue RANGE,SPEED,DAMAGE,RADIUS,TURN,FUSE;public static final ForgeConfigSpec.BooleanValue BLOCK_DAMAGE;
 static{var b=ConfigSchema.builder();SCAN=b.defineInRange("radarIntervalTicks",20,5,200);DEPLOY=b.defineInRange("deploymentTicks",80,40,400);RELOAD=b.defineInRange("reloadTicks",100,20,1200);LIFE=b.defineInRange("missileLifetimeTicks",240,40,1200);MAX_MISSILES=b.defineInRange("maximumActiveMissilesPerLevel",32,1,128);QUALITY=b.comment("0 disables the cosmetic smoke/flame trail; 1 low, 2 normal, 3 high.").defineInRange("trailQuality",2,0,3);RANGE=b.defineInRange("gameTargetRange",384d,32,1024);SPEED=b.defineInRange("gameMissileBlocksPerTick",2.6,.5,5);DAMAGE=b.defineInRange("gameExplosionDamage",100d,1,500);RADIUS=b.defineInRange("gameExplosionRadius",4d,1,8);TURN=b.defineInRange("gameTurnDegreesPerTick",8d,1,30);FUSE=b.defineInRange("gameProximityFuse",1.6,.3,3);BLOCK_DAMAGE=b.comment("Also subject to the existing destruction mode, Superb Warfare setting and protection hooks.").define("environmentalDamage",false);SPEC=b.build();}
 private Mpl01Config(){}
}
