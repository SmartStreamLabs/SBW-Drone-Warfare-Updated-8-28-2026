package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.chat.Component;

public enum DroneConfigCategory {
    DISTANCE_WAR("screen.sbwdroneconfig.config.category.distance_war", "screen.sbwdroneconfig.config.description.distance_war"),
    DRONE("screen.sbwdroneconfig.config.category.drone", "screen.sbwdroneconfig.config.description.drone"),
    LUCAS_FLIGHT("screen.sbwdroneconfig.config.category.lucas_flight", "screen.sbwdroneconfig.config.description.lucas_flight"),
    FUEL("screen.sbwdroneconfig.config.category.fuel", "screen.sbwdroneconfig.config.description.fuel"),
    BATTERY("screen.sbwdroneconfig.config.category.battery", "screen.sbwdroneconfig.config.description.battery"),
    JAMMER("screen.sbwdroneconfig.config.category.jammer", "screen.sbwdroneconfig.config.description.jammer"),
    WEATHER("screen.sbwdroneconfig.config.category.weather", "screen.sbwdroneconfig.config.description.weather"),
    SPOTLIGHT("screen.sbwdroneconfig.config.category.spotlight", "screen.sbwdroneconfig.config.description.spotlight"),
    FPV_DRONE("screen.sbwdroneconfig.config.category.fpv_drone", "screen.sbwdroneconfig.config.description.fpv_drone"),
    DELIVERY_DRONE("screen.sbwdroneconfig.config.category.delivery_drone", "screen.sbwdroneconfig.config.description.delivery_drone"),
    GLIDE_BOMB("screen.sbwdroneconfig.config.category.glide_bomb", "screen.sbwdroneconfig.config.description.glide_bomb"),
    HEAVY_BOMBER_DRONE("screen.sbwdroneconfig.config.category.heavy_bomber_drone", "screen.sbwdroneconfig.config.description.heavy_bomber_drone"),
    GERANIUM_2("screen.sbwdroneconfig.config.category.geranium_2", "screen.sbwdroneconfig.config.description.geranium_2"),
    BAYRAKTAR_TB2("screen.sbwdroneconfig.config.category.bayraktar_tb2", "screen.sbwdroneconfig.config.description.bayraktar_tb2");

    private final String titleKey;
    private final String descriptionKey;

    DroneConfigCategory(String titleKey, String descriptionKey) {
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
    }

    public Component title() {
        return Component.translatable(titleKey);
    }

    public Component description() {
        return Component.translatable(descriptionKey);
    }
}
