package nl.smartstreamlabs.sbwdroneconfig;

@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Dj01Events {
    @net.neoforged.bus.api.SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event){Dj01System.clear();}
    private Dj01Events(){}
}
