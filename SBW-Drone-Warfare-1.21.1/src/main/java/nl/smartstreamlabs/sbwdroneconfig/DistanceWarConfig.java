package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;
public final class DistanceWarConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue MIN_INTERVAL,MAX_INTERVAL;
    public static final ModConfigSpec.DoubleValue VOLUME,MIN_DISTANCE,MAX_DISTANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DIMENSIONS;
    static {var b=ConfigSchema.builder();
        ENABLED=b.comment("Ambient distant booms only. Server authority; off by default.").define("enabled",false);
        MIN_INTERVAL=b.defineInRange("minIntervalSeconds",60,60,180);MAX_INTERVAL=b.comment("Effective maximum is at least the minimum. A fresh pause starts after the entire sample tail.").defineInRange("maxIntervalSeconds",180,60,180);
        VOLUME=b.defineInRange("volume",.30,0,1);MIN_DISTANCE=b.defineInRange("minSourceDistance",250d,64,2000);MAX_DISTANCE=b.comment("Effective maximum is at least the minimum. These sources never load chunks.").defineInRange("maxSourceDistance",700d,64,2000);
        DIMENSIONS=b.defineListAllowEmpty("activeDimensions",List.of("minecraft:overworld"),v->v instanceof String s&&net.minecraft.resources.ResourceLocation.tryParse(s)!=null);
        SPEC=b.build();
    }
    public static DistanceWarPlanner.Settings settings(){return new DistanceWarPlanner.Settings(MIN_INTERVAL.get(),MAX_INTERVAL.get(),VOLUME.get(),MIN_DISTANCE.get(),MAX_DISTANCE.get());}
    private DistanceWarConfig(){}
}
