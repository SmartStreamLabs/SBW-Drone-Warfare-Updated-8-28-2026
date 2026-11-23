package nl.smartstreamlabs.sbwdroneconfig;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)public final class Ps01SelectionClient{
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void outline(net.minecraftforge.client.event.RenderHighlightEvent.Block e){var l=net.minecraft.client.Minecraft.getInstance().level;if(l==null)return;var b=l.getBlockState(e.getTarget().getBlockPos()).getBlock();if(b instanceof Ps01Block||b instanceof Ps01DoorBlock)e.setCanceled(true);}
}
