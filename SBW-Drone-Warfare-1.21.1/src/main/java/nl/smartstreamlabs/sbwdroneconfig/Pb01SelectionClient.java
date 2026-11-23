package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)public final class Pb01SelectionClient{
 @net.neoforged.bus.api.SubscribeEvent public static void outline(net.neoforged.neoforge.client.event.RenderHighlightEvent.Block e){var l=net.minecraft.client.Minecraft.getInstance().level;if(l==null)return;var b=l.getBlockState(e.getTarget().getBlockPos()).getBlock();if(b instanceof Pb01Block||b instanceof Pb01DoorBlock)e.setCanceled(true);}
}
