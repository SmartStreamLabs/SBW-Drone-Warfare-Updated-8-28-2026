package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class MissionEvents {
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){{var s=ServerLifecycleHooks.getCurrentServer();if(s!=null)MissionManager.get(s).tick();}}
    @SubscribeEvent public static void join(EntityJoinLevelEvent event){if(event.getLevel() instanceof ServerLevel l)MissionManager.get(l.getServer()).joined(event.getEntity());}
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){if(event.getLevel() instanceof ServerLevel l)MissionManager.get(l.getServer()).removed(event.getEntity());}
    @SubscribeEvent public static void stop(ServerStoppingEvent event){MissionManager.shutdown(event.getServer());}
    @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event){MissionManager.stopped(event.getServer());}
    @SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload event){if(event.getLevel() instanceof ServerLevel l && l.getServer().isRunning())MissionManager.get(l.getServer()).unload(l);}
}
