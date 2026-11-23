package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;

public final class DeliveryDroneDefinition {
    public static final String ID = "delivery_drone";
    public static final String DISPLAY_NAME = "Delivery Drone";

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "geo/delivery_drone.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "textures/entity/delivery_drone.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "animations/delivery_drone_dd01.animation.json");

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
