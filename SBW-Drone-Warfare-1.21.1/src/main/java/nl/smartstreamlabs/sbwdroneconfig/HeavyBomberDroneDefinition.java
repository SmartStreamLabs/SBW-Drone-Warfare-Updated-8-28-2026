package nl.smartstreamlabs.sbwdroneconfig;

public final class HeavyBomberDroneDefinition {
    public static final String ID = "heavy_bomber_drone";
    public static final String DISPLAY_NAME = "Heavy Bomber Drone";

    public static net.minecraft.resources.ResourceLocation model(){return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"geo/heavy_bomber_drone.geo.json");}
    public static net.minecraft.resources.ResourceLocation texture(){return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"textures/item/heavy_bomber_drone.png");}
    public static net.minecraft.resources.ResourceLocation animation(){return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"animations/heavy_bomber_hb01.animation.json");}

    private HeavyBomberDroneDefinition() {
    }
}
