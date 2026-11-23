package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

public final class SecurityCraftBlastConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MATERIALS,BLOCKS,MAPPINGS;
    static {
        var b=ConfigSchema.builder();
        ENABLED=b.comment("Only attributed Drone Warfare terrain jobs. Existing destruction/claim rules still apply.").define("enabled",true);
        MULTIPLIER=b.comment("Multiply the ordinary material's blast resistance. Does not edit registered blocks.").defineInRange("reinforcedResistanceMultiplier",1d,0d,1000d);
        MATERIALS=b.comment("Optional category=base resistance overrides: stone,wood,glass,metal,other. Applied before the multiplier.").defineListAllowEmpty("materialResistanceOverrides",List.of(),o->valid(o,true,false));
        BLOCKS=b.comment("Optional reinforced registry ID=base resistance overrides. Applied before the multiplier; takes precedence over material overrides.").defineListAllowEmpty("blockResistanceOverrides",List.of(),o->valid(o,false,false));
        MAPPINGS=b.comment("Optional reinforced ID=ordinary block ID. Additional families also require the securitycraft/blast_include block tag. Inventory blocks and hard exclusions are never automatic building families.").defineListAllowEmpty("materialMappings",List.of(),o->valid(o,false,true));
        SPEC=b.build();
    }
    public static boolean valid(Object value,boolean category,boolean mapping){
        if(!(value instanceof String s))return false;int at=s.indexOf('=');if(at<1)return false;
        String left=s.substring(0,at).trim(),right=s.substring(at+1).trim();if(left.isEmpty()||right.isEmpty())return false;
        if(category?!Set.of("stone","wood","glass","metal","other").contains(left):ResourceLocation.tryParse(left)==null)return false;
        return mapping?ResourceLocation.tryParse(right)!=null:SecurityCraftBlastPolicy.number(right);
    }
    private SecurityCraftBlastConfig(){}
}
