package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;

public final class DeliveryDroneDefinition {
    public static final String ID = "delivery_drone";
    public static final String DISPLAY_NAME = "Delivery Drone";

    private static final ResourceLocation MODEL = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "geo/delivery_drone.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "textures/entity/delivery_drone.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "animations/fpv_drone.animation.json");

    private DeliveryDroneDefinition() {
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
