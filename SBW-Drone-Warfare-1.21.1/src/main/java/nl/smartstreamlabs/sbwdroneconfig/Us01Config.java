package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Server-synced cosmetic choices. Equipment, teams, inventory and armor values are unaffected. */
public final class Us01Config{
 public static final ModConfigSpec SPEC;public static final ModConfigSpec.BooleanValue SCARF,HOLSTER,PACK;
 static{var b=ConfigSchema.builder();b.push("US01_REV02");SCARF=b.define("showNeckScarf",true);HOLSTER=b.define("showClosedChestHolster",true);PACK=b.define("showHydrationPack",true);b.pop();SPEC=b.build();}
 public static boolean visible(int group){return switch(group){case 0->true;case 1->SCARF.get();case 2->HOLSTER.get();case 3->PACK.get();default->false;};}
}
