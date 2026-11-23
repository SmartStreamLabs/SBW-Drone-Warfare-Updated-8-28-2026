package nl.smartstreamlabs.sbwdroneconfig;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class VatagMonitorEvents {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void use(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem e){var id=VatagMonitorTag.linked(e.getItemStack());if(id==null)return;e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p){if(Rd01Sessions.linked(e.getItemStack()))Rd01Sessions.start(p,id);else if(Id01Sessions.linked(e.getItemStack()))Id01Sessions.start(p,id);else VatagSessions.start(p,id);}}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)VatagSessions.stop(p);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void dimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)VatagSessions.stop(p);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void death(net.minecraftforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)VatagSessions.stop(p);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void shutdown(net.minecraftforge.event.server.ServerStoppingEvent e){for(var p:e.getServer().getPlayerList().getPlayers())VatagSessions.stop(p);}
}
