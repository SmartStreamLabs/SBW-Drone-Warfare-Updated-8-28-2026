package nl.smartstreamlabs.sbwdroneconfig;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class Ds02ClientEvents {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END)Ds02Audio.maintain();}
 @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
 public static final class ModEvents {
  @net.minecraftforge.eventbus.api.SubscribeEvent public static void reload(net.minecraftforge.client.event.RegisterClientReloadListenersEvent e){e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)r->{Ds02Renderer.clear();Fm01Renderer.clear();Rj01Renderer.clear();Ds02Audio.clear();});}
 }
}
