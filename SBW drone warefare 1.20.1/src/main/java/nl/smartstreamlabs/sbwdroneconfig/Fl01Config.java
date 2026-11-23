package nl.smartstreamlabs.sbwdroneconfig;
public final class Fl01Config{
 public static final net.minecraftforge.common.ForgeConfigSpec SPEC;public static final net.minecraftforge.common.ForgeConfigSpec.DoubleValue SPEED,TURN,RANGE,DAMAGE,RADIUS;public static final net.minecraftforge.common.ForgeConfigSpec.IntValue FUEL;public static final net.minecraftforge.common.ForgeConfigSpec.BooleanValue BLOCK_DAMAGE;
 static{var b=ConfigSchema.builder();b.push("flamingo_gameplay");SPEED=b.comment("Game blocks/tick, independent of real-world specifications").defineInRange("speed",1.4,.4,3);TURN=b.defineInRange("turnDegreesPerTick",3.0,.5,8);RANGE=b.defineInRange("rangeBlocks",10000.0,64,100000);FUEL=b.defineInRange("fuelTicks",12000,100,72000);DAMAGE=b.defineInRange("explosionDamage",180.0,1,10000);RADIUS=b.defineInRange("explosionRadius",7.0,1,24);BLOCK_DAMAGE=b.define("blockDamage",true);b.pop();SPEC=b.build();}
}
