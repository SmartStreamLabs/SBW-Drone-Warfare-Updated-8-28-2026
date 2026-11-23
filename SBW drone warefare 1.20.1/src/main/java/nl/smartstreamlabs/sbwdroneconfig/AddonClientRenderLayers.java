package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class AddonClientRenderLayers {
    private AddonClientRenderLayers() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(AddonClientRenderLayers::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(AddonBlocks.ANTI_DRONE_NET.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(AddonBlocks.ANTI_DRONE_NET_CARPET.get(), net.minecraft.client.renderer.RenderType.cutout());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(AddonBlocks.ANTI_DRONE_NET_PANEL.get(), net.minecraft.client.renderer.RenderType.cutout());
        });
    }
}
