package nl.smartstreamlabs.sbwdroneconfig;

@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Lm01Events {
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){Lm01Budget.stop(e.getServer());}
 private Lm01Events(){}
}
