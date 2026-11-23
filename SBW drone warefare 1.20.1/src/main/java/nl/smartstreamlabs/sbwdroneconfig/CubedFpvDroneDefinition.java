package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;

public final class CubedFpvDroneDefinition {
    public static final String ID = "cubed_fpv_drone";
    public static final String DISPLAY_NAME = "FPV Drone";

    private static final ResourceLocation MODEL = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "geo/cubed_fpv_drone.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "textures/entity/cubed_fpv_drone.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "animations/fpv_drone.animation.json");

    private CubedFpvDroneDefinition() {
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
