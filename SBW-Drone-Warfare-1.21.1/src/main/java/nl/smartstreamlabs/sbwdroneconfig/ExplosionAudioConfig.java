package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Client only; existing engine-audio controls retain their original scope. */
public final class ExplosionAudioConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue VOLUME, RANGE, SPEED;
    public static final ModConfigSpec.BooleanValue DELAY;
    public static final ModConfigSpec.IntValue EFFECTS;
    static {
        var b=ConfigSchema.builder(); b.push("explosionAudio");
        VOLUME=b.comment("Explosion volume; also respects Master and Blocks. Distance explosions remain audible with a gradual falloff.").defineInRange("volume",0.9,0,2);
        RANGE=b.comment("Maximum hearing distance in blocks. No chunks are loaded.").defineInRange("maxDistance",1024.0,16,2048);
        DELAY=b.comment("Enable sound propagation delay from the active camera.").define("distanceDelay",true);
        SPEED=b.comment("Sound propagation in blocks per second.").defineInRange("propagationSpeed",343.0,50,2000);
        EFFECTS=b.comment("Simultaneous explosion effects; each uses at most two voices.").defineInRange("maxEffects",8,1,16);
        b.pop();SPEC=b.build();
    }
}
