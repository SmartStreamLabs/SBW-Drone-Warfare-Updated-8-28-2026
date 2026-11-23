package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class RubbleClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue PARTICLES,DISTANCE,GLOBAL;
    public static final ModConfigSpec.BooleanValue DUST,SHARDS,SMOKE;
    public static final ModConfigSpec.DoubleValue DETAIL;
    static {var b=ConfigSchema.builder();GLOBAL=b.defineInRange("globalExplosionParticleLimit",384,32,1024);PARTICLES=b.defineInRange("maxTemporaryRubbleParticles",192,0,512);DISTANCE=b.defineInRange("debrisCullDistance",96,16,192);DUST=b.define("dust",true);SHARDS=b.define("materialShards",true);SMOKE=b.define("explosionSmoke",true);DETAIL=b.defineInRange("explosionDetail",1d,.1,1);SPEC=b.build();}
    private RubbleClientConfig(){}
}
