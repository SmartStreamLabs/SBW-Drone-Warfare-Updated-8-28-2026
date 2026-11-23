package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Reuses the existing SBW-monitor tags, bounded ticket loader and remote chunk sender. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Rd01Sessions {
    private static final TicketType<UUID> LOAD=TicketType.create("rd01_connect",Comparator.comparing(UUID::toString),120);
    private static final Map<UUID,Session> ACTIVE=new HashMap<>();
    private static final Map<UUID,Pending> PENDING=new HashMap<>();
    private static final class Session {
        final ServerPlayer player;final Rd01Entity drone;final UUID token=UUID.randomUUID();final net.minecraft.world.phys.Vec3 body;
        long sequence=-1,lastInput,lastAction=-10;
        Session(ServerPlayer p,Rd01Entity d){player=p;drone=d;body=p.position();lastInput=p.serverLevel().getGameTime();}
    }
    private record Pending(ServerPlayer player,UUID drone,ServerLevel level,ChunkPos chunk,long tick){}
    public static boolean controlling(ServerPlayer p,Entity d){Session s=ACTIVE.get(p.getUUID());return s!=null&&s.drone==d;}
    public static int count(){return ACTIVE.size()+PENDING.size();}
    private static double range(){return AddonConfig.droneMaxRange();}
    public static void mark(ItemStack stack){var n=AddonStackTags.getOrCreate(stack);n.putBoolean("Rd01Linked",true);AddonStackTags.save(stack,n);}
    public static boolean linked(ItemStack stack){var n=AddonStackTags.get(stack);return VatagMonitorTag.linked(stack)!=null&&n!=null&&n.getBoolean("Rd01Linked");}
    public static void start(ServerPlayer p,UUID id) {
        if(InjuryServer.cannotOperate(p))return;
        stop(p);VatagSessions.stop(p);Id01Sessions.stop(p);
        if(!linked(p.getMainHandItem())||!VatagMonitorTag.matches(p.getMainHandItem(),id)||!AddonConfig.enableDroneRemoteChunkTracking()||count()>=4)return;
        String dimension=VatagMonitorTag.dimension(p.getMainHandItem());
        if(dimension!=null&&!dimension.equals(p.level().dimension().location().toString()))return;
        if(p.serverLevel().getEntity(id) instanceof Rd01Entity d){connect(p,d);return;}
        var pos=VatagMonitorTag.position(p.getMainHandItem());
        if(pos==null||!p.serverLevel().getWorldBorder().isWithinBounds(pos)||p.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))>range()*range())return;
        ChunkPos chunk=new ChunkPos(pos);p.serverLevel().getChunkSource().addRegionTicket(LOAD,chunk,2,p.getUUID(),true);
        PENDING.put(p.getUUID(),new Pending(p,id,p.serverLevel(),chunk,p.serverLevel().getGameTime()));
    }
    private static void connect(ServerPlayer p,Rd01Entity d) {
        if(InjuryServer.cannotOperate(p)||!p.isAlive()||p.isSpectator()||p.hasDisconnected()||p.isPassenger()||p.level()!=d.level()||!d.isAlive()||d.isRemoved()||d.isWreck()||p.distanceToSqr(d)>range()*range()||!d.takeControl(p))return;
        Session s=new Session(p,d);ACTIVE.put(p.getUUID(),s);DroneClientChunkSyncManager.touch(p,d,1);ChunkTicketManager.touchDrone(d,1);send(s);
    }
    private static boolean valid(Session s) {
        return !InjuryServer.cannotOperate(s.player)&&s.drone.operator()==s.player&&s.player.isAlive()&&!s.player.hasDisconnected()&&!s.player.isPassenger()&&s.player.level()==s.drone.level()&&s.drone.isAlive()&&!s.drone.isRemoved()&&!s.drone.isWreck()&&AddonConfig.enableDroneRemoteChunkTracking()&&linked(s.player.getMainHandItem())&&VatagMonitorTag.matches(s.player.getMainHandItem(),s.drone.getUUID())&&s.player.position().distanceToSqr(s.body)<9&&s.player.distanceToSqr(s.drone)<=range()*range();
    }
    public static void request(ServerPlayer p,Rd01Input m) {
        Session s=ACTIVE.get(p.getUUID());if(s==null||!s.token.equals(m.token())||m.sequence()<=s.sequence||!valid(s)||!Float.isFinite(m.yaw()))return;
        s.sequence=m.sequence();s.lastInput=p.serverLevel().getGameTime();
        if(m.action()==2){stop(p);return;}
        if(m.action()==1){s.drone.input(p,m.keys()&255,m.yaw());return;}
        if(m.action()==3&&s.lastInput-s.lastAction>=5){s.lastAction=s.lastInput;s.drone.releasePassenger();}
    }
    private static void send(Session s){AddonNetwork.rd01State(s.player,new Rd01State(s.drone.getId(),s.token,true));}
    public static void stop(ServerPlayer p) {
        Pending pending=PENDING.remove(p.getUUID());if(pending!=null)pending.level.getChunkSource().removeRegionTicket(LOAD,pending.chunk,2,p.getUUID(),true);
        Session s=ACTIVE.remove(p.getUUID());if(s==null)return;
        if(VatagMonitorTag.matches(p.getMainHandItem(),s.drone.getUUID())){VatagMonitorTag.link(p.getMainHandItem(),s.drone.getUUID(),s.drone.blockPosition(),s.drone.level().dimension().location().toString());mark(p.getMainHandItem());}
        s.drone.releaseControl();DroneClientChunkSyncManager.release(p,true);
        if(!s.drone.occupied())ChunkTicketManager.releaseDrone(s.drone);
        if(!p.hasDisconnected())AddonNetwork.rd01State(p,new Rd01State(-1,s.token,false));
    }
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
        for(Pending pending:List.copyOf(PENDING.values())) {
            if(pending.player.hasDisconnected()||pending.player.level()!=pending.level||!pending.player.isAlive()||!linked(pending.player.getMainHandItem())||!VatagMonitorTag.matches(pending.player.getMainHandItem(),pending.drone)||pending.level.getGameTime()-pending.tick>100){stop(pending.player);continue;}
            if(pending.level.getEntity(pending.drone) instanceof Rd01Entity d){PENDING.remove(pending.player.getUUID());connect(pending.player,d);pending.level.getChunkSource().removeRegionTicket(LOAD,pending.chunk,2,pending.player.getUUID(),true);}
        }
        for(Session s:List.copyOf(ACTIVE.values())) {
            if(!valid(s)||s.player.serverLevel().getGameTime()-s.lastInput>30){stop(s.player);continue;}
            DroneClientChunkSyncManager.touch(s.player,s.drone,1);ChunkTicketManager.touchDrone(s.drone,1);
            if(s.player.tickCount%10==0)send(s);
            if(s.player.tickCount%20==0){VatagMonitorTag.link(s.player.getMainHandItem(),s.drone.getUUID(),s.drone.blockPosition(),s.drone.level().dimension().location().toString());mark(s.player.getMainHandItem());}
        }
    }
    public static void clearPassenger(ServerPlayer p){if(p.getVehicle() instanceof Rd01Entity d)d.emergencyRelease();}
    private static void lostPlayer(ServerPlayer p){Session s=ACTIVE.get(p.getUUID());if(s!=null)s.drone.emergencyRelease();stop(p);clearPassenger(p);}
    @net.neoforged.bus.api.SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)lostPlayer(p);}
    @net.neoforged.bus.api.SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){stop(p);clearPassenger(p);}}
    @net.neoforged.bus.api.SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)lostPlayer(p);else if(e.getEntity() instanceof Rd01Entity d){d.emergencyRelease();if(d.operator()!=null)stop(d.operator());}}
    @net.neoforged.bus.api.SubscribeEvent public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)lostPlayer(p);}
    @net.neoforged.bus.api.SubscribeEvent public static void shutdown(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var p:e.getServer().getPlayerList().getPlayers()){stop(p);clearPassenger(p);}for(var pending:List.copyOf(PENDING.values()))stop(pending.player);}
    @net.neoforged.bus.api.SubscribeEvent public static void mount(net.neoforged.neoforge.event.entity.EntityMountEvent e) {
        if(e.isDismounting()&&e.getEntityMounting() instanceof ServerPlayer p&&e.getEntityBeingMounted() instanceof Rd01Entity d&&!d.forcedExit()&&p.isAlive()&&!p.hasDisconnected()&&d.isAlive()&&!d.isRemoved()&&d.safeExit(p).isEmpty()) {
            e.setCanceled(true);p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(d));p.displayClientMessage(net.minecraft.network.chat.Component.translatable("rd01.no_ground"),true);
        }
    }
    @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void packing(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract e) {
        if(e.getTarget() instanceof Rd01Entity d&&d.occupied()&&e.getItemStack().is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)) {
            e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);if(!e.getLevel().isClientSide())e.getEntity().displayClientMessage(net.minecraft.network.chat.Component.translatable("rd01.pack_blocked"),true);
        }
    }
}
