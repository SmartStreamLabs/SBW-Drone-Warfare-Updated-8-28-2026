package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class FuelMixerClient {
    private FuelMixerClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(FuelMixerClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(AddonMenus.FUEL_MIXER.get(), FuelMixerScreen::new);
    }
}
