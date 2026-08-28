package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;

public final class LucasDroneDefinition {
    public static final String ID = "lucas_drone";
    public static final String DISPLAY_NAME = "LUCAS Drone";

    private static final ResourceLocation MODEL = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "geo/lucas_drone.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "textures/entity/lucas_drone.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "animations/fpv_drone.animation.json");

    private LucasDroneDefinition() {
    }

    public static ResourceLocation model() {
        return MODEL;
    }

    public static ResourceLocation texture() {
        return TEXTURE;
    }

    public static ResourceLocation animation() {
        return ANIMATION;
    }
}
