package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;

public final class DroneConfigMenuClient {
    private DroneConfigMenuClient() {
    }

    public static void init(ModContainer modContainer) {
        Supplier<IConfigScreenFactory> factory = () -> (container, parent) ->
                new DroneConfigScreen(parent, DroneConfigCategory.DRONE, DroneConfigDraft.fromConfig());
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }
}
