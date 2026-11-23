package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Dil01Events {
 @net.neoforged.bus.api.SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)Dil01System.reset(p);}
 @net.neoforged.bus.api.SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){Dil01System.clear(e.getEntity().getUUID());}
 @net.neoforged.bus.api.SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)Dil01System.reset(p);}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){Dil01System.clearAll();}
}
