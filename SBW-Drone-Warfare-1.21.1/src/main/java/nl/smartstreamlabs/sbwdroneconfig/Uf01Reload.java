package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Uf01Reload{
 @net.neoforged.bus.api.SubscribeEvent public static void reload(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent e){e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)r->{UfMusicClient.reload();Uf01Model.clear();DvtClient.clear();Dvt01Model.clear();});}
}
