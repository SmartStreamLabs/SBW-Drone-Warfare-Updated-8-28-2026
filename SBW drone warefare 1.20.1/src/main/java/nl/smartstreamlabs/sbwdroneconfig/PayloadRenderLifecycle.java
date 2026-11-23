package nl.smartstreamlabs.sbwdroneconfig;
/** Detached visual proxies must not retain a discarded client level. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class PayloadRenderLifecycle {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload e){if(e.getLevel().isClientSide())Hb01PayloadRenderer.clear();}
 private PayloadRenderLifecycle(){}
}
