package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.eventbus.api.*;import net.minecraftforge.event.level.ExplosionEvent;import net.minecraft.server.level.*;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Pm01Events {
 @SubscribeEvent(priority=EventPriority.HIGHEST)public static void blast(ExplosionEvent.Detonate e){if(!(e.getLevel()instanceof ServerLevel l)||!(e.getExplosion().getDirectSourceEntity()instanceof Pm01Entity m))return;if(!Mpl01Config.BLOCK_DAMAGE.get())e.getAffectedBlocks().clear();e.getAffectedEntities().removeIf(t->{if(m.vehicle()!=null&&(m.vehicle().equals(t.getUUID())||t.getVehicle()!=null&&m.vehicle().equals(t.getVehicle().getUUID())))return true;if(Adv01Targets.drone(t))return Adv01Targets.relation(l,m.principal(),m.principalName(),t)!=Adv01Targets.HOSTILE;if(t instanceof net.minecraft.world.entity.player.Player p&&m.principal()!=null){var owner=l.getServer().getPlayerList().getPlayer(m.principal());var ownerTeam=l.getScoreboard().getPlayersTeam(m.principalName());var targetTeam=l.getScoreboard().getPlayersTeam(p.getScoreboardName());return p.getUUID().equals(m.principal())||owner!=null&&owner.isAlliedTo(p)||ownerTeam!=null&&targetTeam!=null&&ownerTeam.isAlliedTo(targetTeam);}return false;});}
 @SubscribeEvent public static void join(net.minecraftforge.event.entity.EntityJoinLevelEvent e){if(e.getLevel()instanceof ServerLevel l&&e.getEntity()instanceof Pm01Entity)Pm01Entity.track(l,e.getEntity().getUUID(),true);}
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){Pm01Entity.reset();}
 private Pm01Events(){}
}
