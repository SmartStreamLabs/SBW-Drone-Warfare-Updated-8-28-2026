package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.Commands;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class DistanceWarServer {
    private static final DistanceWarPlanner PLANNER=new DistanceWarPlanner(new Random());
    private static boolean running;private static long lastTick=Long.MIN_VALUE/2,emitted;private static DistanceWarPlanner.Settings previous;private static List<? extends String> dimensions=List.of();
    private static DistanceWarPlanner.Listener listener(ServerPlayer p){return new DistanceWarPlanner.Listener(p.getUUID(),p.serverLevel().dimension().location().toString(),p.getX(),p.getY(),p.getZ());}
    private static List<DistanceWarPlanner.Listener> listeners(MinecraftServer s){return s.getPlayerList().getPlayers().stream().filter(p->!p.isSpectator()&&DistanceWarConfig.DIMENSIONS.get().contains(p.serverLevel().dimension().location().toString())).map(DistanceWarServer::listener).toList();}
    private static void send(MinecraftServer server,DistanceWarPlanner.Event e,DistanceWarPlanner.Settings settings){
        if(e==null)return;for(var id:e.recipients()){ServerPlayer p=server.getPlayerList().getPlayer(id);if(p==null)continue;AddonNetwork.distanceWar(p,new DistanceWarPacket(false,e.id(),p.serverLevel().dimension().location(),p.serverLevel().getGameTime()+4,e.x(),e.y(),e.z(),e.clip(),e.volume(),e.pitch(),(float)settings.radius()));}emitted++;
    }
    private static void clear(MinecraftServer s){PLANNER.clear();for(var p:s.getPlayerList().getPlayers())AddonNetwork.distanceWar(p,new DistanceWarPacket(true,0,p.serverLevel().dimension().location(),0,0,0,0,0,0,1,4096));}
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var s=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(s==null)return;long now=s.overworld().getGameTime();if(now-lastTick<20)return;lastTick=now;
        boolean enabled=DistanceWarConfig.ENABLED.get();var settings=DistanceWarConfig.settings();var dims=DistanceWarConfig.DIMENSIONS.get();
        if(enabled!=running||!settings.equals(previous)||!dims.equals(dimensions)){clear(s);running=enabled;previous=settings;dimensions=List.copyOf(dims);}
        if(!enabled||settings.volume()==0)return;for(var event:PLANNER.tick(now,listeners(s),settings))send(s,event,settings);
    }
    @SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){PLANNER.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void changedDimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){PLANNER.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopping(net.minecraftforge.event.server.ServerStoppingEvent e){PLANNER.resetWorld();running=false;lastTick=Long.MIN_VALUE/2;previous=null;dimensions=List.of();emitted=0;}
    @SubscribeEvent public static void commands(net.minecraftforge.event.RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("sbwdrone").then(Commands.literal("distancewar").requires(s->s.hasPermission(2))
        .then(Commands.literal("on").executes(c->toggle(c.getSource(),true)))
        .then(Commands.literal("off").executes(c->toggle(c.getSource(),false)))
        .then(Commands.literal("status").executes(c->{var settings=DistanceWarConfig.settings();c.getSource().sendSuccess(()->Component.literal("Distance War Sounds: "+(DistanceWarConfig.ENABLED.get()?"ON":"OFF")+"; pause "+settings.minSeconds()+"–"+settings.maxSeconds()+"s; volume "+settings.volume()+"; areas "+PLANNER.areas()+"/64; listeners "+PLANNER.tracked()+"; events "+emitted),false);return 1;}))
        .then(Commands.literal("test").executes(c->{var p=c.getSource().getPlayerOrException();if(!DistanceWarConfig.DIMENSIONS.get().contains(p.serverLevel().dimension().location().toString())){c.getSource().sendFailure(Component.literal("Distance War Sounds is not configured for this dimension."));return 0;}var settings=DistanceWarConfig.settings();var event=PLANNER.example(p.getServer().overworld().getGameTime(),listener(p),listeners(p.getServer()),settings);if(event==null){c.getSource().sendFailure(Component.literal("Wait for the previous tail and its configured quiet period to finish."));return 0;}send(p.getServer(),event,settings);c.getSource().sendSuccess(()->Component.literal("Played one distant example for nearby players; normal ambience remains "+(DistanceWarConfig.ENABLED.get()?"ON":"OFF")+"."),false);return 1;}))));}
    private static int toggle(net.minecraft.commands.CommandSourceStack source,boolean value){DistanceWarConfig.ENABLED.set(value);DistanceWarConfig.SPEC.save();clear(source.getServer());running=value;previous=DistanceWarConfig.settings();lastTick=Long.MIN_VALUE/2;source.sendSuccess(()->Component.literal("Distance War Sounds "+(value?"enabled; first boom follows a fresh random pause.":"disabled; active and queued sounds cleared.")),true);return 1;}
    public static String diagnostics(){return "running="+running+",areas="+PLANNER.areas()+",listeners="+PLANNER.tracked()+",events="+emitted;}
    private DistanceWarServer(){}
}
