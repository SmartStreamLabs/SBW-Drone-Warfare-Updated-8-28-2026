package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.Entity;import java.util.*;
/** Republishes existing gameplay status to actual OSD viewers, including read-only receiver feeds. */
public final class CameraInterferenceStatus {
    private record Last(float amount,boolean lost,long tick){}
    private static final Map<UUID,Map<UUID,Last>> SENT=new HashMap<>();
    public static void send(ServerPlayer player,Entity drone){
        boolean protectedLink=FiberOpticLinkSystem.isImmuneToNormalJammers(drone);
        float amount=protectedLink?0:DroneJamStateManager.getJamProgress(drone);
        boolean lost=!protectedLink&&DroneJamStateManager.isHardJammed(drone);
        long now=player.serverLevel().getGameTime();var feeds=SENT.computeIfAbsent(player.getUUID(),id->new HashMap<>());
        feeds.entrySet().removeIf(e->now-e.getValue().tick>40);
        var old=feeds.get(drone.getUUID());if(old!=null&&Math.abs(old.amount-amount)<.01&&old.lost==lost&&now-old.tick<20)return;
        if(feeds.size()>=2&&!feeds.containsKey(drone.getUUID()))feeds.clear();
        AddonNetwork.sendDroneJam(player,new DroneJamSyncMessage(drone.getUUID(),amount,lost));feeds.put(drone.getUUID(),new Last(amount,lost,now));
    }
    public static void remove(UUID player){SENT.remove(player);}
    public static void clear(){SENT.clear();}
    private CameraInterferenceStatus(){}
}
