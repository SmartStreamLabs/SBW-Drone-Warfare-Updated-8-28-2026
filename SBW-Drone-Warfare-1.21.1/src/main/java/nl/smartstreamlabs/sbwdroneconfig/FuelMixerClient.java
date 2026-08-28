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
        event.enqueueWork(() -> MenuScreens.register(AddonMenus.FUEL_MIXER.get(), FuelMixerScreen::new));
    }
}
