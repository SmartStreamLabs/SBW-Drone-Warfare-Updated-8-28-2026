package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.neoforge.common.ModConfigSpec;

/** World-local destruction settings; changing modes never deletes previously stored rubble. */
public final class RubbleConfig {
    public enum Mode { VISUAL, DESTRUCTION, COLLAPSE }
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED, RESPECT_SBW;
    public static final ModConfigSpec.EnumValue<Mode> MODE;
    public static final ModConfigSpec.IntValue WORK, BLOCKS, JOBS, PIECES, RADIUS, CHAIN, SPAN, PILE, CLEAR_TICKS, DURABILITY, AREA;
    public static final ModConfigSpec.DoubleValue RECOVERY, IMPACT, GLASS, VISUAL;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> PROFILES;
    static {
        var b = ConfigSchema.builder();
        ENABLED=b.comment("Disables new blast processing and pauses queued destruction; existing rubble is retained.").define("enabled",true);
        MODE=b.comment("VISUAL never changes blocks. DESTRUCTION adds glass/block damage and permanent rubble. COLLAPSE also checks lost support.").defineEnum("mode",Mode.DESTRUCTION);
        RESPECT_SBW=b.comment("Also require Superb Warfare's explosion block destruction setting for its CustomExplosion blasts.").define("respectSbwBlockDestruction",true);
        WORK=b.defineInRange("workBudgetPerLevelTick",2048,512,8192);
        BLOCKS=b.defineInRange("maxBlocksPerBlast",256,8,1024);
        JOBS=b.defineInRange("maxQueuedBlasts",64,1,256);
        PIECES=b.defineInRange("maxMovingPiecesPerLevel",24,0,64);
        RADIUS=b.defineInRange("maxLocalRadius",12,3,16);
        CHAIN=b.defineInRange("maxCollapseChain",4,0,12);
        SPAN=b.comment("Stone span; wood is twice and metal three times this value, within the local search boundary.").defineInRange("stoneSpan",3,1,6);
        PILE=b.defineInRange("maxUnitsPerPile",256,8,1024);
        CLEAR_TICKS=b.defineInRange("ticksToClearOneUnit",24,4,200);
        DURABILITY=b.defineInRange("toolDamagePerUnit",1,0,16);
        AREA=b.defineInRange("clearingSelectionRadius",5,1,12);
        RECOVERY=b.comment("At most one material item per consumed rubble unit; no ordinary block drop is also produced.").defineInRange("recoveryFraction",.25,0,1);
        IMPACT=b.defineInRange("maximumFallingImpactDamage",4d,0,20);
        GLASS=b.defineInRange("glassRadiusMultiplier",1.7,1,3);
        VISUAL=b.defineInRange("visualIntensity",1d,.1,2);
        PROFILES=b.comment("payload or entity ID = block radius multiplier,visual multiplier. The second value is optional. Native entity damage/radius is unchanged.").defineListAllowEmpty("loadProfiles",java.util.List.of("sbwdroneconfig:hand_grenade_hg01=1.0,1.0","sbwdroneconfig:ground_assault_drone_gad01=1.0,1.0"),o->o instanceof String s&&valid(s));
        SPEC=b.build();
    }
    static boolean valid(String s){int i=s.indexOf('=');if(i<1||net.minecraft.resources.ResourceLocation.tryParse(s.substring(0,i).trim())==null)return false;try{String[] values=s.substring(i+1).split(",",-1);if(values.length<1||values.length>2)return false;for(String v:values){double x=Double.parseDouble(v);if(!Double.isFinite(x)||x<0||x>3)return false;}return true;}catch(Exception e){return false;}}
    public static double scale(String id){return profile(id,0);}
    public static boolean hasProfile(String id){for(String s:PROFILES.get())if(s.substring(0,s.indexOf('=')).trim().equals(id))return true;return false;}
    public static double visual(String id){return profile(id,1);}
    private static double profile(String id,int part){for(String s:PROFILES.get()){int i=s.indexOf('=');if(s.substring(0,i).trim().equals(id)){String[] values=s.substring(i+1).split(",");return values.length>part?Double.parseDouble(values[part]):1;}}return 1;}
    private RubbleConfig(){}
}
