package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class DwfCapabilities{
 @net.neoforged.bus.api.SubscribeEvent public static void register(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e){e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,AddonBlockEntities.DWF01.get(),(b,side)->b.handler());if(net.neoforged.fml.ModList.get().isLoaded("refinedstorage"))DwfRs2.registerCapabilities(e);}
}
