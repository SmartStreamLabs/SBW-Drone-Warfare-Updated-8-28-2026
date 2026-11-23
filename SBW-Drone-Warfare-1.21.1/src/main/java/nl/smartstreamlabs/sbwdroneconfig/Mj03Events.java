package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Mj03Events {
 @net.neoforged.bus.api.SubscribeEvent public static void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){Mj03JammerRegistry.join(e.getEntity());}
 @net.neoforged.bus.api.SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent e){Mj03JammerRegistry.leave(e.getEntity());}
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){Mj03JammerRegistry.tick();}
 @net.neoforged.bus.api.SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){if(e.getLevel() instanceof net.minecraft.server.level.ServerLevel l)Mj03JammerRegistry.clear(l);}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){Mj03JammerRegistry.clear();}
}
