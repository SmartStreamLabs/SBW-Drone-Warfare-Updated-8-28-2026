package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Id01Sessions {
    private static final TicketType<UUID> LOAD=TicketType.create("id01_connect",Comparator.comparing(UUID::toString),120);
    private static final Map<UUID,Session> ACTIVE=new HashMap<>();
    private static final Map<UUID,Pending> PENDING=new HashMap<>();
    private static final class Session {
        final ServerPlayer player;final Id01Entity drone;final UUID token=UUID.randomUUID();final Vec3 body;
        long sequence=-1,lastAction=-10;Session(ServerPlayer p,Id01Entity d){player=p;drone=d;body=p.position();}
    }
    private record Pending(ServerPlayer player,UUID drone,ServerLevel level,ChunkPos chunk,long tick){}
    public static boolean controlling(ServerPlayer p,Entity e){var s=ACTIVE.get(p.getUUID());return s!=null&&s.drone==e;}
    public static boolean active(ServerPlayer p){return ACTIVE.containsKey(p.getUUID());}
    public static boolean fleetSlot(ServerPlayer p){return AddonConfig.enableDroneRemoteChunkTracking()&&(ACTIVE.containsKey(p.getUUID())||ACTIVE.size()+PENDING.size()<4);}
    public static void start(ServerPlayer p,UUID id){
        if(InjuryServer.cannotOperate(p))return;
        stop(p);Rd01Sessions.stop(p);VatagSessions.stop(p);
        if(!AddonConfig.enableDroneRemoteChunkTracking()||ACTIVE.size()+PENDING.size()>=4||!VatagMonitorTag.matches(p.getMainHandItem(),id))return;
        String dimension=VatagMonitorTag.dimension(p.getMainHandItem());if(dimension!=null&&!dimension.equals(p.level().dimension().location().toString()))return;
        var entity=p.serverLevel().getEntity(id);if(entity instanceof Id01Entity d){connect(p,d);return;}
        var pos=VatagMonitorTag.position(p.getMainHandItem());
        if(pos==null||!p.serverLevel().getWorldBorder().isWithinBounds(pos)||p.position().distanceToSqr(Vec3.atCenterOf(pos))>range()*range())return;
        var chunk=new ChunkPos(pos);p.serverLevel().getChunkSource().addRegionTicket(LOAD,chunk,2,p.getUUID(),true);
        PENDING.put(p.getUUID(),new Pending(p,id,p.serverLevel(),chunk,p.serverLevel().getGameTime()));
    }
    private static double range(){return AddonConfig.droneMaxRange();}
    private static void connect(ServerPlayer p,Id01Entity d){
        if(InjuryServer.cannotOperate(p)||!p.isAlive()||p.isSpectator()||p.hasDisconnected()||p.level()!=d.level()||!d.isAlive()||d.isRemoved()||d.isWreck()||p.distanceToSqr(d)>range()*range()||!d.takeControl(p))return;
        var s=new Session(p,d);ACTIVE.put(p.getUUID(),s);DroneClientChunkSyncManager.touch(p,d,1);ChunkTicketManager.touchDrone(d,1);send(s);
    }
    private static boolean valid(Session s){return !InjuryServer.cannotOperate(s.player)&&s.drone.operator()==s.player&&s.player.isAlive()&&!s.player.hasDisconnected()&&s.player.level()==s.drone.level()&&s.drone.isAlive()&&!s.drone.isRemoved()&&!s.drone.isWreck()&&AddonConfig.enableDroneRemoteChunkTracking()&&VatagMonitorTag.matches(s.player.getMainHandItem(),s.drone.getUUID())&&s.player.position().distanceToSqr(s.body)<9&&s.player.distanceToSqr(s.drone)<=range()*range();}
    public static void request(ServerPlayer p,Id01Input m){
        var s=ACTIVE.get(p.getUUID());if(s==null||!s.token.equals(m.token())||m.sequence()<=s.sequence||!valid(s))return;
        s.sequence=m.sequence();if(m.action()==2){stop(p);return;}
        if(m.action()==1){s.drone.input(p,m.keys(),m.yaw());return;}
        long now=p.serverLevel().getGameTime();if(now-s.lastAction<4)return;s.lastAction=now;
        switch(m.action()){
            case 3->{} // old launch-without-target packet is refused
            case 4->send(s);
            case 5->{if(!s.drone.intercept(m.target()))p.displayClientMessage(Component.literal("ID-01: target unavailable, friendly, unknown, or out of range."),true);}
            case 6->s.drone.requestReturn();
            case 7->{} // old piloting packet refused
            default->{}
        }
    }
    private static void send(Session s){
        Entity target=s.drone.targetId()==null?null:s.player.serverLevel().getEntity(s.drone.targetId());
        AddonNetwork.id01State(s.player,new Id01State(s.drone.getId(),s.token,true,target==null?"":target.getName().getString(),target==null?0:s.drone.distanceTo(target),Id01Radar.scan(s.drone,s.player)));
    }
    public static void stop(ServerPlayer p){
        var pending=PENDING.remove(p.getUUID());if(pending!=null)pending.level.getChunkSource().removeRegionTicket(LOAD,pending.chunk,2,p.getUUID(),true);
        var s=ACTIVE.remove(p.getUUID());if(s==null)return;
        if(VatagMonitorTag.matches(p.getMainHandItem(),s.drone.getUUID())){VatagMonitorTag.link(p.getMainHandItem(),s.drone.getUUID(),s.drone.blockPosition(),s.drone.level().dimension().location().toString());mark(p.getMainHandItem());}
        s.drone.releaseControl();DroneClientChunkSyncManager.release(p,true);ChunkTicketManager.releaseDrone(s.drone);
        if(!p.hasDisconnected())AddonNetwork.id01State(p,new Id01State(-1,s.token,false,"",0,List.of()));
    }
    public static void mark(net.minecraft.world.item.ItemStack stack){stack.getOrCreateTag().putBoolean("Id01Linked",true);}
    public static boolean linked(net.minecraft.world.item.ItemStack stack){var n=stack.getTag();return VatagMonitorTag.linked(stack)!=null&&n!=null&&n.getBoolean("Id01Linked");}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){if(e.phase!=TickEvent.Phase.END)return;
        for(var p:List.copyOf(PENDING.values())){
            if(p.player.hasDisconnected()||p.player.level()!=p.level||!p.player.isAlive()||!VatagMonitorTag.matches(p.player.getMainHandItem(),p.drone)||p.level.getGameTime()-p.tick>100){stop(p.player);continue;}
            if(p.level.getEntity(p.drone) instanceof Id01Entity d){PENDING.remove(p.player.getUUID());connect(p.player,d);p.level.getChunkSource().removeRegionTicket(LOAD,p.chunk,2,p.player.getUUID(),true);}
        }
        for(var s:List.copyOf(ACTIVE.values())){
            if(!valid(s)){stop(s.player);continue;}
            DroneClientChunkSyncManager.touch(s.player,s.drone,1);
            if(s.drone.mode()!=Id01Entity.Mode.STANDBY&&s.drone.mode()!=Id01Entity.Mode.DISABLED)ChunkTicketManager.touchDrone(s.drone,1);
            if(s.player.tickCount%10==0)send(s);
            if(s.player.tickCount%20==0){VatagMonitorTag.link(s.player.getMainHandItem(),s.drone.getUUID(),s.drone.blockPosition(),s.drone.level().dimension().location().toString());mark(s.player.getMainHandItem());}
        }
    }
    @SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)stop(p);}
    @SubscribeEvent public static void dimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)stop(p);}
    @SubscribeEvent public static void death(net.minecraftforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)stop(p);}
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST,receiveCanceled=true) public static void explosion(net.minecraftforge.event.level.ExplosionEvent.Start e){if(e.getExplosion().getExploder() instanceof Id01Entity d)d.explosionBlocked(e.isCanceled());}
    @SubscribeEvent public static void shutdown(net.minecraftforge.event.server.ServerStoppingEvent e){for(var s:List.copyOf(ACTIVE.values()))stop(s.player);for(var p:List.copyOf(PENDING.values()))stop(p.player);}
}
