package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

public final class InjuryEvents {
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e) {
        if(e.phase==net.minecraftforge.event.TickEvent.Phase.END){var s=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(s!=null)InjuryServer.tick(s);}
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
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(net.minecraftforge.event.entity.living.LivingDeathEvent e){if(e.getEntity()instanceof ServerPlayer p)InjuryServer.clear(p);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void mount(net.minecraftforge.event.entity.EntityMountEvent e) {
        if(e.isMounting()&&e.getEntityMounting()instanceof ServerPlayer p&&InjuryHooks.downed(p)&&!(e.getEntityBeingMounted()instanceof Rd01Entity)&&!(e.getEntityBeingMounted()instanceof MedicalTrailerEntity))e.setCanceled(true);
    }
    @SubscribeEvent public static void sleep(net.minecraftforge.event.entity.player.PlayerSleepInBedEvent e){if(InjuryHooks.downed(e.getEntity()))e.setResult(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM);}
    @SubscribeEvent(priority=EventPriority.HIGH) public static void rescue(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract e) {
        if(!(e.getEntity()instanceof ServerPlayer rescuer)||!(e.getTarget()instanceof ServerPlayer patient)||!InjuryHooks.downed(patient)||!rescuer.isShiftKeyDown()||!e.getItemStack().isEmpty()||InjuryHooks.downed(rescuer))return;
        var drones=rescuer.serverLevel().getEntitiesOfClass(Rd01Entity.class,patient.getBoundingBox().inflate(4),d->d.permitted(rescuer)&&!d.occupied()&&d.isAlive()&&!d.isWreck());
        drones.sort(java.util.Comparator.comparingDouble(patient::distanceToSqr));
        if(!drones.isEmpty()){e.setCanceled(true);e.setCancellationResult(drones.get(0).board(patient));}
    }
    @SubscribeEvent public static void stopping(net.minecraftforge.event.server.ServerStoppingEvent e){for(var p:e.getServer().getPlayerList().getPlayers())InjuryServer.logout(p);InjuryServer.shutdown();}
}
