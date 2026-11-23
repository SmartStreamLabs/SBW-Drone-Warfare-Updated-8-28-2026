package nl.smartstreamlabs.sbwdroneconfig;

public final class BayraktarTb2Definition {
    public static final String ID = "ukrainian_bayraktar_tb2";
    public static final String DISPLAY_NAME = "Bayraktar TB2";

    public static net.minecraft.resources.ResourceLocation model(){return new net.minecraft.resources.ResourceLocation(SbwDroneRangeConfig.MOD_ID,"geo/ukrainian_bayraktar_tb2.geo.json");}
    public static net.minecraft.resources.ResourceLocation texture(){return new net.minecraft.resources.ResourceLocation(SbwDroneRangeConfig.MOD_ID,"textures/item/ukrainian_bayraktar_tb2.png");}
    public static net.minecraft.resources.ResourceLocation animation(){return new net.minecraft.resources.ResourceLocation(SbwDroneRangeConfig.MOD_ID,"animations/bayraktar_tb2_bt01.animation.json");}

    private BayraktarTb2Definition() {
    }
}
