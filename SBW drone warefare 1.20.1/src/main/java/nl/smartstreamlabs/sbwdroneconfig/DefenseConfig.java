package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class DefenseConfig{
 public static final ForgeConfigSpec SPEC;public static final ForgeConfigSpec.IntValue SCAN,CONTACTS,LINKS,AUTO_INTERVAL,ASSIGNMENT;public static final ForgeConfigSpec.DoubleValue RANGE,LINK_RANGE;
 static{var b=ConfigSchema.builder();RANGE=b.defineInRange("radarRange",384d,32,1024);SCAN=b.defineInRange("scanIntervalTicks",20,5,200);CONTACTS=b.defineInRange("maximumContacts",32,1,64);LINK_RANGE=b.defineInRange("linkRange",256d,16,1024);LINKS=b.defineInRange("maximumLaunchersPerRadar",8,1,32);AUTO_INTERVAL=b.defineInRange("automaticAssignmentIntervalTicks",20,5,200);ASSIGNMENT=b.defineInRange("maximumConcurrentMissilesPerTarget",1,1,4);SPEC=b.build();}
}
