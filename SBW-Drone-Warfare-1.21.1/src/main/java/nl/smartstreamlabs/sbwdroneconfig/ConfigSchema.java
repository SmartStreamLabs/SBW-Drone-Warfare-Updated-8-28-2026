package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class ConfigSchema {
 public static final int CURRENT=1;
 public static final String VERSION="_configVersion";
 private ConfigSchema(){}
 public static ModConfigSpec.Builder builder(){
  var b=new ModConfigSpec.Builder();b.comment("Drone Warfare configuration schema version. Managed by migrations; do not edit.").defineInRange(VERSION,CURRENT,0,Integer.MAX_VALUE);return b;
 }
}
