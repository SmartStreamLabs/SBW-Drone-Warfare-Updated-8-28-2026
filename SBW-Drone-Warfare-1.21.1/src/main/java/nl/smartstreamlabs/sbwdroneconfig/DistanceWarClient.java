package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class DistanceWarClient {
    private static final DistanceWarPlaybackGate gate=new DistanceWarPlaybackGate();
    private static ClientLevel world;private static final PriorityQueue<DistanceWarPacket> pending=new PriorityQueue<>(Comparator.comparingLong(DistanceWarPacket::start));
    private static final LinkedHashSet<Long> seen=new LinkedHashSet<>();private static Voice active;private static long received,played,discarded;
    private static Vec3 ear(){var m=Minecraft.getInstance();var c=m.gameRenderer.getMainCamera();return c.isInitialized()?c.getPosition():m.player==null?Vec3.ZERO:m.player.getEyePosition();}
    public static void clear(){if(active!=null)active.cancel();active=null;pending.clear();seen.clear();gate.finish(world==null?0:world.getGameTime());}
    private static void sync(){var next=Minecraft.getInstance().level;if(next!=world){clear();gate.resetWorld();world=next;}}
    public static void receive(DistanceWarPacket m){sync();if(m.reset()){clear();return;}if(world==null||!world.dimension().location().equals(m.dimension())||DistanceWarClientConfig.VOLUME.get()==0||m.volume()==0)return;
        if(seen.contains(m.id()))return;seen.add(m.id());while(seen.size()>64)seen.remove(seen.iterator().next());
        long age=world.getGameTime()-m.start();if(age>40||age< -80||!pending.isEmpty()){discarded++;return;}if(!gate.reserve(m.start())){discarded++;return;}pending.add(m);received++;
    }
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){sync();var game=Minecraft.getInstance();if(world==null||game.isPaused())return;
        if(DistanceWarClientConfig.VOLUME.get()==0){clear();return;}if(active!=null){boolean live=game.getSoundManager().isActive(active);active.started|=live;if(!active.started&&world.getGameTime()-active.born>active.sampleTicks+200)active.cancel();if(active.isStopped()||active.started&&!live){active.cancel();active=null;gate.finish(world.getGameTime());}}
        while(!pending.isEmpty()&&pending.peek().start()<=world.getGameTime()){
            var p=pending.remove();if(world.getGameTime()-p.start()>40||active!=null){discarded++;gate.finish(world.getGameTime());continue;}
            active=new Voice(p);active.updateGain();if(active.volumeValue()<=0){active=null;gate.finish(world.getGameTime());discarded++;continue;}game.getSoundManager().play(active);played++;
        }
    }
    private static final class Voice extends AbstractTickableSoundInstance {
        final ClientLevel owner;final Vec3 origin;final float strength,range;final long born;final int sampleTicks;boolean started;int age;
        Voice(DistanceWarPacket p){super(AddonSounds.DISTANCE_WAR_CLIPS.get(p.clip()).get(),SoundSource.AMBIENT,RandomSource.create());owner=world;born=world.getGameTime();sampleTicks=DistanceWarPlanner.sampleTicks(p.clip());origin=new Vec3(p.x(),p.y(),p.z());strength=p.volume();range=p.range();x=p.x();y=p.y();z=p.z();pitch=1;relative=false;looping=false;attenuation=Attenuation.NONE;}
        float volumeValue(){return volume;}
        void updateGain(){Vec3 listener=ear();double environment=1;var pos=BlockPos.containing(listener);if(owner.hasChunkAt(pos)){if(owner.getBrightness(LightLayer.SKY,pos)<8)environment*=.7;if(listener.y<32)environment*=.65;}
            // Retain true world coordinates for mono OpenAL direction. Apply distance gain once here.
            volume=(float)(strength*DistanceWarClientConfig.VOLUME.get()*DistanceWarPlanner.gain(listener.distanceTo(origin),range)*environment);
        }
        void cancel(){stop();Minecraft.getInstance().getSoundManager().stop(this);}
        @Override public void tick(){++age;if(owner!=Minecraft.getInstance().level||DistanceWarClientConfig.VOLUME.get()==0){cancel();return;}updateGain();}
        @Override public boolean canStartSilent(){return true;}
        @Override public boolean canPlaySound(){return !isStopped()&&owner==Minecraft.getInstance().level;}
    }
    public static String diagnostics(){return "pending="+pending.size()+",active="+(active==null?0:1)+",received="+received+",played="+played+",discarded="+discarded;}
    private DistanceWarClient(){}
}
