package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.bus.api.*;import net.neoforged.neoforge.event.level.*;import net.minecraft.server.level.*;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Sk01Events {
 @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){Sk01Deaths.consider(e.getEntity(),e::isCanceled);}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){{var s=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(s!=null)Sk01Server.tick(s);}}
 @SubscribeEvent public static void watch(ChunkWatchEvent.Sent e){Sk01Server.watch(e.getPlayer(),e.getLevel(),e.getPos());}
 @SubscribeEvent public static void unwatch(ChunkWatchEvent.UnWatch e){Sk01Server.unwatch(e.getPlayer(),e.getLevel(),e.getPos());}
 @SubscribeEvent public static void load(ChunkEvent.Load e){if(e.getLevel() instanceof ServerLevel l)Sk01Server.loaded(l,e.getChunk().getPos());}
 @SubscribeEvent public static void unload(LevelEvent.Unload e){if(e.getLevel() instanceof ServerLevel l)Sk01Server.unloaded(l);}
 @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)Sk01Server.logout(p);}
 @SubscribeEvent public static void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(e.getLevel().isClientSide())return;if(e.getEntity() instanceof net.minecraft.world.entity.LivingEntity living&&living.isAlive())Sk01Deaths.recovered(living);if(e.getEntity() instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.getHealth()>0&&!v.isWreck())v.getPersistentData().remove(Sk01Server.DEATH_TAG);}
 @SubscribeEvent public static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent e){Sk01Server.commands(e.getDispatcher());}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){Sk01Deaths.stop(e.getServer());Sk01Server.stop(e.getServer());}
}
