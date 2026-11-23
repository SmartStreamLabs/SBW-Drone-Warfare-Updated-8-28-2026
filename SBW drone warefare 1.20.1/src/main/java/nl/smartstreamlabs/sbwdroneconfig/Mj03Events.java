package nl.smartstreamlabs.sbwdroneconfig;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Mj03Events {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void join(net.minecraftforge.event.entity.EntityJoinLevelEvent e){Mj03JammerRegistry.join(e.getEntity());}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void leave(net.minecraftforge.event.entity.EntityLeaveLevelEvent e){Mj03JammerRegistry.leave(e.getEntity());}
 @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST) public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END)Mj03JammerRegistry.tick();}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload e){if(e.getLevel() instanceof net.minecraft.server.level.ServerLevel l)Mj03JammerRegistry.clear(l);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppedEvent e){Mj03JammerRegistry.clear();}
}
