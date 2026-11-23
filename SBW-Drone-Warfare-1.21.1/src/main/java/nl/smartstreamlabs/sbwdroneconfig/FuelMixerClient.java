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
        event.register(AddonMenus.PLATFORM_INVENTORY.get(),PlatformInventoryScreen::new);event.register(AddonMenus.HX_PLATFORM.get(),HxPlatformScreen::new);event.register(AddonMenus.DWF01.get(),DwfScreen::new);event.register(AddonMenus.FL_LP.get(),Fl01Screen::new);
        event.register(AddonMenus.GAD01.get(),Gad01Screen::new);
        event.register(AddonMenus.MPL_SERVICE.get(),MplServiceScreen::new);event.register(AddonMenus.NDC01.get(),Ndc01Screen::new);event.register(AddonMenus.DC01.get(),Dc01Screen::new);event.register(AddonMenus.CARRIER_FLEET.get(),CarrierFleetScreen::new);
        event.register(AddonMenus.FUEL_MIXER.get(), FuelMixerScreen::new);
        event.register(AddonMenus.GLIDE_PLATFORM.get(),GlidePlatformScreen::new);
        event.register(AddonMenus.GERANIUM_PLATFORM.get(),GeraniumPlatformScreen::new);
        event.register(AddonMenus.LUCAS_PLATFORM.get(),LucasPlatformScreen::new);
    }
}
