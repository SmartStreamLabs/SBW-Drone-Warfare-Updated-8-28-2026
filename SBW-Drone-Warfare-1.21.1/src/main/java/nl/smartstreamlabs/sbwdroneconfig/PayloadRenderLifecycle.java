package nl.smartstreamlabs.sbwdroneconfig;
/** Detached visual proxies must not retain a discarded client level. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class PayloadRenderLifecycle {
 @net.neoforged.bus.api.SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){if(e.getLevel().isClientSide())Hb01PayloadRenderer.clear();}
 private PayloadRenderLifecycle(){}
}
