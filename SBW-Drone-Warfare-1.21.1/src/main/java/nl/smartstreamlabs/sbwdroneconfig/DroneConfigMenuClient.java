package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class DroneConfigMenuClient {
    private DroneConfigMenuClient() {
    }

    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new DroneConfigScreen(parent, DroneConfigCategory.DRONE, DroneConfigDraft.fromConfig())
                )
        );
    }
}
