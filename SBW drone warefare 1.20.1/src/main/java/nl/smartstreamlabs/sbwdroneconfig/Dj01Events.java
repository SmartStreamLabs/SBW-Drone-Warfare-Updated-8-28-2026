package nl.smartstreamlabs.sbwdroneconfig;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Dj01Events {
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event){Dj01System.clear();}
    private Dj01Events(){}
}
