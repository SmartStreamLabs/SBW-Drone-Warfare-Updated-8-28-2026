package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

public final class InjuryEvents {
    @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
        if(true){var s=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(s!=null)InjuryServer.tick(s);}
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity()instanceof ServerPlayer p)InjuryServer.login(p);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity()instanceof ServerPlayer p)InjuryServer.logout(p);}
    @SubscribeEvent public static void clone(PlayerEvent.Clone e) {
        if(e.getEntity()instanceof ServerPlayer p){
            if(e.isWasDeath())p.getPersistentData().remove(InjuryServer.KEY);
            else p.getPersistentData().put(InjuryServer.KEY,e.getOriginal().getPersistentData().getCompound(InjuryServer.KEY).copy());
        }
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity()instanceof ServerPlayer p)InjuryServer.login(p);}
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking e){if(e.getEntity()instanceof ServerPlayer viewer&&e.getTarget()instanceof ServerPlayer patient)AddonNetwork.injuryStateTo(viewer,InjuryServer.snapshot(patient));}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(e.getEntity()instanceof ServerPlayer p)InjuryServer.clear(p);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void mount(net.neoforged.neoforge.event.entity.EntityMountEvent e) {
        if(e.isMounting()&&e.getEntityMounting()instanceof ServerPlayer p&&InjuryHooks.downed(p)&&!(e.getEntityBeingMounted()instanceof Rd01Entity)&&!(e.getEntityBeingMounted()instanceof MedicalTrailerEntity))e.setCanceled(true);
    }
    @SubscribeEvent public static void sleep(net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent e){if(InjuryHooks.downed(e.getEntity()))e.setProblem(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM);}
    @SubscribeEvent(priority=EventPriority.HIGH) public static void rescue(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract e) {
        if(!(e.getEntity()instanceof ServerPlayer rescuer)||!(e.getTarget()instanceof ServerPlayer patient)||!InjuryHooks.downed(patient)||!rescuer.isShiftKeyDown()||!e.getItemStack().isEmpty()||InjuryHooks.downed(rescuer))return;
        var drones=rescuer.serverLevel().getEntitiesOfClass(Rd01Entity.class,patient.getBoundingBox().inflate(4),d->d.permitted(rescuer)&&!d.occupied()&&d.isAlive()&&!d.isWreck());
        drones.sort(java.util.Comparator.comparingDouble(patient::distanceToSqr));
        if(!drones.isEmpty()){e.setCanceled(true);e.setCancellationResult(drones.get(0).board(patient));}
    }
    @SubscribeEvent public static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var p:e.getServer().getPlayerList().getPlayers())InjuryServer.logout(p);InjuryServer.shutdown();}
}
