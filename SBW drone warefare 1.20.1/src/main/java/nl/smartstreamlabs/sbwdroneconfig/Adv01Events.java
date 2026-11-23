package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerLevel;

/** Friendly protection belongs to this new flak source; other explosions are untouched. */
@Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Adv01Events {
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST) public static void blast(ExplosionEvent.Detonate event) {
        if(!(event.getLevel() instanceof ServerLevel l)||!(event.getExplosion().getDirectSourceEntity() instanceof Adv01Shell shell))return;
        // SBW damages vehicles inside its NORMAL-priority Detonate handler, before the usual entity loop.
        event.getAffectedBlocks().clear();
        event.getAffectedEntities().removeIf(e->{
            if(shell.sourceVehicle()!=null&&(shell.sourceVehicle().equals(e.getUUID())||e.getVehicle()!=null&&shell.sourceVehicle().equals(e.getVehicle().getUUID())))return true;
            if(Adv01Targets.drone(e)){int relation=Adv01Targets.relation(l,shell.principal(),shell.principalName(),e);return relation==Adv01Targets.FRIEND||shell.automaticShot()&&relation!=Adv01Targets.HOSTILE;}
            if(e instanceof net.minecraft.world.entity.player.Player p&&shell.principal()!=null){
                var owner=l.getServer().getPlayerList().getPlayer(shell.principal());return shell.principal().equals(p.getUUID())||owner!=null&&owner.isAlliedTo(p);
            }
            return false;
        });
    }
    private Adv01Events() {}
}
