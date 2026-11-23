package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class AdcReload{
 @net.neoforged.bus.api.SubscribeEvent public static void register(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent e){e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)r->{AdcModel.clear();Hb01PayloadRenderer.clear();Mpl01Materials.clear();DroneAudioManager.stopAll();});}
}
