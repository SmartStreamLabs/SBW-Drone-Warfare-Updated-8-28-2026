package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
public final class PilotMapConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue HIDE;
    static {
        var builder = ConfigSchema.builder();
        HIDE = builder.comment("Hide UUID-based player markers on supported Xaero maps during a server-confirmed manual drone control session. Does not affect the player entity or map configuration.")
                .define("hideDronePilotsOnXaeroMaps", true);
        SPEC = builder.build();
    }
    private PilotMapConfig() {}
}
