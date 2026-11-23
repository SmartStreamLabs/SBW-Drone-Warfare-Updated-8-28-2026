package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class MissionEvents {
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){if(event.phase==TickEvent.Phase.END){var s=ServerLifecycleHooks.getCurrentServer();if(s!=null)MissionManager.get(s).tick();}}
    @SubscribeEvent public static void join(EntityJoinLevelEvent event){if(event.getLevel() instanceof ServerLevel l)MissionManager.get(l.getServer()).joined(event.getEntity());}
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){if(event.getLevel() instanceof ServerLevel l)MissionManager.get(l.getServer()).removed(event.getEntity());}
    @SubscribeEvent public static void stop(ServerStoppingEvent event){MissionManager.shutdown(event.getServer());}
    @SubscribeEvent public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event){MissionManager.stopped(event.getServer());}
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event){if(event.getLevel() instanceof ServerLevel l && l.getServer().isRunning())MissionManager.get(l.getServer()).unload(l);}
}
