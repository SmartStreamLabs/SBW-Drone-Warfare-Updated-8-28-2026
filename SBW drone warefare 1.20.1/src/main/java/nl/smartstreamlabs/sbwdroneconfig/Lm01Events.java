package nl.smartstreamlabs.sbwdroneconfig;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Lm01Events {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){Lm01Budget.stop(e.getServer());}
 private Lm01Events(){}
}
