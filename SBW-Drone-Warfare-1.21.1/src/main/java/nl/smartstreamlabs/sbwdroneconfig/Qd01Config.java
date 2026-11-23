package nl.smartstreamlabs.sbwdroneconfig;
public final class Qd01Config {
 public static final net.neoforged.neoforge.common.ModConfigSpec SPEC;
 public static final net.neoforged.neoforge.common.ModConfigSpec.DoubleValue SPEED;
 static {var b=ConfigSchema.builder();b.push("qd01");SPEED=b.comment("QD-01 native ground-drive speed multiplier. Does not change any other vehicle.").defineInRange("speedMultiplier",1d,.25d,2d);b.pop();SPEC=b.build();}
 private Qd01Config(){}
}
