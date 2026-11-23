package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;

/** Loaded roots only. Never asks the chunk source to load anything, and sends no audio/frame packets. */
public final class UfMusicServer {
    public static final int WATCH=0,PLAY=1,PAUSE=2,STOP=3,VOLUME=4,REPEAT=5,RANGE=6,CLOSE=7;
    private static final Set<Uf01BlockEntity> ROOTS=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<Uf01BlockEntity,Set<UUID>> LISTENERS=new IdentityHashMap<>();
    private static final Map<UUID,Watch> WATCHERS=new HashMap<>();
    private static final Map<UUID,long[]> RATES=new HashMap<>();
    private static volatile boolean reload;
    private static int ticks;
    private record Watch(Uf01BlockEntity root,long time){}
    public static void loaded(Uf01BlockEntity b){ROOTS.add(b);}
    public static void unloaded(Uf01BlockEntity b){b.music.stop();notifyListeners(b,true);ROOTS.remove(b);LISTENERS.remove(b);WATCHERS.values().removeIf(w->w.root==b);}
    public static void trackChanged(){reload=true;}
    public static boolean allowed(ServerPlayer p,Uf01BlockEntity b){return p.hasPermissions(2)||UfMusicConfig.PUBLIC.get()||b.owner!=null&&DefenseData.allowed(p.serverLevel(),b.owner,b.ownerName,p);}
    /** A click on any existing physical helper is routed to its loaded root, without a remote-control loophole. */
    public static boolean near(ServerPlayer p,Uf01BlockEntity b){
        var l=p.serverLevel();if(b.getLevel()!=l||!p.isAlive()||p.isSpectator()||InjuryHooks.downed(p)||b.isRemoved()||!l.hasChunkAt(b.getBlockPos())||l.getBlockEntity(b.getBlockPos())!=b)return false;
        var f=b.getBlockState().getValue(Uf01Block.FACING);
        for(int i=0;i<Uf01Layout.CELLS.length;i++){var cell=Uf01Layout.cell(b.getBlockPos(),f,i);if(p.distanceToSqr(cell.getX()+.5,cell.getY()+.5,cell.getZ()+.5)>64||!l.hasChunkAt(cell))continue;
            var s=l.getBlockState(cell);if(s.getBlock() instanceof Uf01Block&&s.getValue(Uf01Block.PART)==i&&s.getValue(Uf01Block.FACING)==f&&l.mayInteract(p,cell))return true;}
        return false;
    }
    public static void open(ServerPlayer p,BlockPos root){if(p.serverLevel().hasChunkAt(root)&&p.serverLevel().getBlockEntity(root)instanceof Uf01BlockEntity b&&near(p,b)){WATCHERS.put(p.getUUID(),new Watch(b,System.nanoTime()));send(p,b,true,false,"");}}
    public static void request(ServerPlayer p,UfMusicRequest m){
        long now=System.nanoTime();var rate=RATES.computeIfAbsent(p.getUUID(),k->new long[]{now,0});if(now-rate[0]>=1_000_000_000L){rate[0]=now;rate[1]=0;}if(++rate[1]>12)return;
        if(m.action()==CLOSE){var w=WATCHERS.get(p.getUUID());if(w!=null&&w.root.musicId.equals(m.identity())&&w.root.getBlockPos().equals(m.pos()))WATCHERS.remove(p.getUUID());return;}
        var l=p.serverLevel();if(!l.hasChunkAt(m.pos())||!(l.getBlockEntity(m.pos())instanceof Uf01BlockEntity b)||!b.musicId.equals(m.identity())||!near(p,b))return;
        if(m.action()==WATCH){WATCHERS.put(p.getUUID(),new Watch(b,now));send(p,b,false,false,"");return;}
        if(!allowed(p,b)){send(p,b,false,false,"ufmusic.denied");return;}
        String notice="";
        switch(m.action()){
            case PLAY -> {b.repeat=true;if(!UfMusicTrack.ready()){notice="ufmusic.missing";break;}if(b.music.status()==UfMusicClock.STOPPED)b.session=UUID.randomUUID();b.music.play(now);}
            case PAUSE -> {if(b.music.status()==UfMusicClock.PLAYING)b.music.pause(now,UfMusicTrack.duration,b.repeat);else if(b.music.status()==UfMusicClock.PAUSED&&UfMusicTrack.ready())b.music.play(now);}
            case STOP -> b.music.stop();
            case VOLUME -> {b.volume=Math.max(0,Math.min(100,m.value()));b.setChanged();}
            case REPEAT -> {b.music.rebase(now,UfMusicTrack.duration,b.repeat);b.repeat=true;b.setChanged();}
            case RANGE -> {if(!p.hasPermissions(2)){notice="ufmusic.denied";break;}b.range=Math.max(8,Math.min(256,m.value()));b.setChanged();}
            default -> {return;}
        }
        notifyListeners(b,false);send(p,b,false,false,notice);
    }
    private static boolean audible(ServerPlayer p,Uf01BlockEntity b){var at=b.getBlockPos();return p.serverLevel()==b.getLevel()&&p.distanceToSqr(at.getX()+.5,at.getY()+.25,at.getZ()+.5)<(double)b.range*b.range;}
    private static void send(ServerPlayer p,Uf01BlockEntity b,boolean show,boolean removed,String notice){
        AddonNetwork.ufMusicReply(p,new UfMusicReply(b.getBlockPos(),b.getLevel().dimension().location().toString(),b.musicId,b.session,show,removed,allowed(p,b),p.hasPermissions(2),b.music.status(),b.music.position(System.nanoTime(),UfMusicTrack.duration,b.repeat),UfMusicTrack.duration,b.volume,b.range,b.repeat,UfMusicTrack.title,notice));
    }
    private static void notifyListeners(Uf01BlockEntity b,boolean remove){
        if(!(b.getLevel()instanceof ServerLevel l))return;
        var old=LISTENERS.computeIfAbsent(b,k->new HashSet<>());var next=new HashSet<UUID>();
        for(var p:l.getServer().getPlayerList().getPlayers()){
            var w=WATCHERS.get(p.getUUID());boolean watching=w!=null&&w.root==b&&near(p,b);
            boolean listen=!remove&&b.music.status()!=UfMusicClock.STOPPED&&audible(p,b);
            if(listen||watching)next.add(p.getUUID());
            if(listen||watching||old.contains(p.getUUID()))send(p,b,false,remove||!listen&&!watching,"");
        }
        old.clear();old.addAll(next);
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
        boolean reset=reload;reload=false;long now=System.nanoTime();ticks++;
        WATCHERS.entrySet().removeIf(w->now-w.getValue().time>5_000_000_000L);
        for(var b:List.copyOf(ROOTS)){
            if(b.isRemoved()||b.getLevel()==null){unloaded(b);continue;}
            b.repeat=true;boolean ended=reset||b.music.finish(now,UfMusicTrack.duration,b.repeat);if(reset)b.music.stop();
            if(ended||ticks%20==0&&(b.music.status()!=UfMusicClock.STOPPED||LISTENERS.containsKey(b)&&!LISTENERS.get(b).isEmpty()))notifyListeners(b,false);
        }
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void reload(net.minecraftforge.event.AddReloadListenerEvent e){e.addListener(new UfMusicTrack());}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().getUUID();WATCHERS.remove(id);RATES.remove(id);LISTENERS.values().forEach(s->s.remove(id));}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){for(var b:List.copyOf(ROOTS))unloaded(b);ROOTS.clear();LISTENERS.clear();WATCHERS.clear();RATES.clear();ticks=0;}
    private UfMusicServer(){}
}
