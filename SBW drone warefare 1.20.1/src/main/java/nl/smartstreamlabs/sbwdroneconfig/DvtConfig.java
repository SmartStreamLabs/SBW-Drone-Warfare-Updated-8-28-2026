package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class DvtConfig {
 public static final ForgeConfigSpec SPEC;public static final ForgeConfigSpec.IntValue WIDTH,FPS,FEEDS;
 static{var b=ConfigSchema.builder();WIDTH=b.comment("Camera texture width; height follows 16:9. Bounded extra world view: two chunks.").defineInRange("width",640,256,1280);FPS=b.defineInRange("framesPerSecond",10,1,30);FEEDS=b.comment("Shared feeds per client, not per terminal.").defineInRange("maxFeeds",2,1,2);SPEC=b.build();}
 private DvtConfig(){}
}
