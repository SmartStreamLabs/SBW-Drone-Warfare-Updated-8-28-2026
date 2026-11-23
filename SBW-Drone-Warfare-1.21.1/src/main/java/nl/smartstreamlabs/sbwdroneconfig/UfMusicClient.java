package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;import net.minecraft.client.Minecraft;import net.minecraft.core.BlockPos;import net.minecraft.sounds.SoundSource;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class UfMusicClient {
    public static final Map<BlockPos,Entry> ENTRIES=new HashMap<>();
    private static Object world;private static int ticks;private static boolean reloaded;
    public static final class Entry {
        public volatile UfMusicReply state;private volatile long received;public UfMusicSound voice;public long retry;public int starts;
        Entry(UfMusicReply s){state=s;received=System.nanoTime();}
        public long position(){var s=state;long p=s.position()+(s.state()==UfMusicClock.PLAYING?Math.max(0,(System.nanoTime()-received)/1_000_000):0);return s.duration()<=0?0:s.repeat()?p%s.duration():Math.min(s.duration(),p);}
        void update(UfMusicReply s){boolean replaced=!s.identity().equals(state.identity())||!s.session().equals(state.session());state=s;received=System.nanoTime();if(replaced)end();}
        public void end(){if(voice!=null){voice.end();voice=null;}}
    }
    public static void receive(UfMusicReply s){var m=Minecraft.getInstance();if(m.level==null||!m.level.dimension().location().toString().equals(s.dimension()))return;if(world!=m.level){clear();world=m.level;}
        var entry=ENTRIES.computeIfAbsent(s.pos(),p->new Entry(s));entry.update(s);
        if(s.removed()||s.state()==UfMusicClock.STOPPED)entry.end();
        if(s.show()){world=m.level;m.setScreen(new UfMusicScreen(s.pos(),s.identity()));}
        if(s.removed()){ENTRIES.remove(s.pos());if(m.screen instanceof UfMusicScreen screen&&screen.pos.equals(s.pos()))screen.onClose();}
    }
    public static void request(BlockPos pos,UUID id,int action,int value){AddonNetwork.ufMusicRequest(new UfMusicRequest(pos,id,action,value));}
    public static boolean available(){return Minecraft.getInstance().getResourceManager().getResource(UfMusicTrack.AUDIO).isPresent();}
    public static void reload(){reloaded=true;}
    public static void clear(){ENTRIES.values().forEach(Entry::end);ENTRIES.clear();}
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
        var m=Minecraft.getInstance();if(world!=m.level||m.player==null){clear();world=m.level;return;}
        if(reloaded){ENTRIES.values().forEach(Entry::end);reloaded=false;}ticks++;
        for(var it=ENTRIES.values().iterator();it.hasNext();){var n=it.next();var s=n.state;boolean gui=m.screen instanceof UfMusicScreen screen&&screen.pos.equals(s.pos());
            if(System.nanoTime()-n.received>8_000_000_000L){n.end();it.remove();continue;}
            if(!m.level.hasChunkAt(s.pos())){n.end();continue;}
            boolean audible=s.state()!=UfMusicClock.STOPPED&&s.duration()>0&&m.gameRenderer.getMainCamera().getPosition().distanceToSqr(s.pos().getX()+.5,s.pos().getY()+.25,s.pos().getZ()+.5)<(double)s.range()*s.range();
            boolean personal=m.options.getSoundSourceVolume(SoundSource.MASTER)>0&&m.options.getSoundSourceVolume(SoundSource.RECORDS)>0;
            if(!audible||!personal){n.end();if(!gui&&s.state()==UfMusicClock.STOPPED){it.remove();}continue;}
            if(n.voice!=null){var v=n.voice;if(!v.failure.isEmpty()){v.end();n.retry=System.nanoTime()+5_000_000_000L;continue;}
                if(v.isStopped()){n.end();n.retry=System.nanoTime()+500_000_000L;}
                else if(v.cursor>=0&&s.state()==UfMusicClock.PLAYING&&System.nanoTime()-v.created>2_000_000_000L){double d=Math.abs(n.position()-v.cursor%(double)s.duration());if(s.repeat())d=Math.min(d,s.duration()-d);if(d>600){n.end();n.retry=System.nanoTime()+100_000_000L;}}
            }
            if(n.voice==null&&System.nanoTime()>=n.retry&&available()){n.voice=new UfMusicSound(n);n.starts++;m.getSoundManager().play(n.voice);}
        }
    }
    private UfMusicClient(){}
}
