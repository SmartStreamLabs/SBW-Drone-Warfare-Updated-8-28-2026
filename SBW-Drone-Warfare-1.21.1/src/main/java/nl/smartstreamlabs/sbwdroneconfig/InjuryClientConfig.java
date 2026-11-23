package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class InjuryClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue EFFECTS, VOLUME;
    public static final ModConfigSpec.BooleanValue HUD;
    static { var b = ConfigSchema.builder();
        HUD = b.define("showMedicalHud", true);
        EFFECTS = b.comment("Red hit indication strength; zero disables the visual effect, not server rules.").defineInRange("effectStrength", .45, 0, 1);
        VOLUME = b.comment("Medical audio gain, also respects the game's volume controls.").defineInRange("medicalAudioVolume", .35, 0, 1);
        SPEC = b.build();
    }
    private InjuryClientConfig() {}
}
