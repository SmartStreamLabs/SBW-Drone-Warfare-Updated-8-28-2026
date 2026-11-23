package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class ConfigSchema {
 public static final int CURRENT=1;
 public static final String VERSION="_configVersion";
 private ConfigSchema(){}
 public static ForgeConfigSpec.Builder builder(){
  var b=new ForgeConfigSpec.Builder();b.comment("Drone Warfare configuration schema version. Managed by migrations; do not edit.").defineInRange(VERSION,CURRENT,0,Integer.MAX_VALUE);return b;
 }
}
