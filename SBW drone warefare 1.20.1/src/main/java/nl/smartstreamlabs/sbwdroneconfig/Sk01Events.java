package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.eventbus.api.*;import net.minecraftforge.event.level.*;import net.minecraft.server.level.*;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Sk01Events {
 @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)public static void death(net.minecraftforge.event.entity.living.LivingDeathEvent e){Sk01Deaths.consider(e.getEntity(),e::isCanceled);}
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END){var s=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(s!=null)Sk01Server.tick(s);}}
 @SubscribeEvent public static void watch(ChunkWatchEvent.Watch e){Sk01Server.watch(e.getPlayer(),e.getLevel(),e.getPos());}
 @SubscribeEvent public static void unwatch(ChunkWatchEvent.UnWatch e){Sk01Server.unwatch(e.getPlayer(),e.getLevel(),e.getPos());}
 @SubscribeEvent public static void load(ChunkEvent.Load e){if(e.getLevel() instanceof ServerLevel l)Sk01Server.loaded(l,e.getChunk().getPos());}
 @SubscribeEvent public static void unload(LevelEvent.Unload e){if(e.getLevel() instanceof ServerLevel l)Sk01Server.unloaded(l);}
 @SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)Sk01Server.logout(p);}
 @SubscribeEvent public static void join(net.minecraftforge.event.entity.EntityJoinLevelEvent e){if(e.getLevel().isClientSide())return;if(e.getEntity() instanceof net.minecraft.world.entity.LivingEntity living&&living.isAlive())Sk01Deaths.recovered(living);if(e.getEntity() instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.getHealth()>0&&!v.isWreck())v.getPersistentData().remove(Sk01Server.DEATH_TAG);}
 @SubscribeEvent public static void commands(net.minecraftforge.event.RegisterCommandsEvent e){Sk01Server.commands(e.getDispatcher());}
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){Sk01Deaths.stop(e.getServer());Sk01Server.stop(e.getServer());}
}
