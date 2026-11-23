package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class FuelMixerClient {
    private FuelMixerClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(FuelMixerClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
MenuScreens.register(AddonMenus.PLATFORM_INVENTORY.get(),PlatformInventoryScreen::new);MenuScreens.register(AddonMenus.HX_PLATFORM.get(),HxPlatformScreen::new);MenuScreens.register(AddonMenus.DWF01.get(),DwfScreen::new);MenuScreens.register(AddonMenus.FL_LP.get(),Fl01Screen::new);MenuScreens.register(AddonMenus.GAD01.get(),Gad01Screen::new);MenuScreens.register(AddonMenus.MPL_SERVICE.get(),MplServiceScreen::new);MenuScreens.register(AddonMenus.NDC01.get(),Ndc01Screen::new);MenuScreens.register(AddonMenus.DC01.get(),Dc01Screen::new);MenuScreens.register(AddonMenus.CARRIER_FLEET.get(),CarrierFleetScreen::new);MenuScreens.register(AddonMenus.FUEL_MIXER.get(),FuelMixerScreen::new);MenuScreens.register(AddonMenus.LUCAS_PLATFORM.get(),LucasPlatformScreen::new);MenuScreens.register(AddonMenus.GLIDE_PLATFORM.get(),GlidePlatformScreen::new);MenuScreens.register(AddonMenus.GERANIUM_PLATFORM.get(),GeraniumPlatformScreen::new);});
    }
}
