package nl.smartstreamlabs.sbwdroneconfig;

import java.io.*;import java.util.concurrent.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.resources.sounds.*;import net.minecraft.client.sounds.*;import net.minecraft.sounds.SoundSource;import net.minecraft.util.RandomSource;
import nl.smartstreamlabs.sbwdroneconfig.mixin.*;

/** Exactly one native streaming voice per audible flag, with its own pausable OpenAL channel. */
public final class UfMusicSound extends AbstractTickableSoundInstance {
    public final UfMusicClient.Entry entry;
    public volatile double cursor=-1;
    public volatile String failure="";
    public final long created=System.nanoTime();
    public boolean ready;
    public UfMusicSound(UfMusicClient.Entry e){super(AddonSounds.UF01_MUSIC.get(),SoundSource.RECORDS,RandomSource.create());entry=e;var p=e.state.pos();x=p.getX()+.5;y=p.getY()+.25;z=p.getZ()+.5;looping=false;relative=false;attenuation=Attenuation.NONE;volume=0;}
    @Override public boolean canStartSilent(){return true;}
    @Override public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers,Sound sound,boolean looping){
        var resources=Minecraft.getInstance().getResourceManager();
        return CompletableFuture.supplyAsync(()->{try{
            var stream=new UfMusicStream(()->resources.open(UfMusicTrack.AUDIO),entry.position(),()->entry.state.repeat());
            if(isStopped()){stream.close();throw new IOException("UF01 source cancelled while decoding");}
            ready=true;return stream;
        }catch(IOException|RuntimeException e){failure="ufmusic.client_missing";SbwDroneRangeConfig.LOGGER.warn("UF01 music could not open/seek the mono OGG",e);throw new CompletionException(e);}},net.minecraft.Util.backgroundExecutor());
    }
    @Override public void tick(){
        var m=Minecraft.getInstance();if(m.level==null||m.player==null||entry.state.removed()||entry.state.state()==UfMusicClock.STOPPED){stop();return;}
        var camera=m.gameRenderer.getMainCamera().getPosition();var at=new net.minecraft.world.phys.Vec3(x,y,z);
        volume=entry.state.state()==UfMusicClock.PAUSED?0:UfMusicClock.gain(camera.distanceTo(at),entry.state.range(),entry.state.volume());
        var engine=((UfMusicManagerAccess)m.getSoundManager()).ufMusic$engine();var handle=((UfMusicEngineAccess)engine).ufMusic$channels().get(this);
        if(handle!=null)handle.execute(c->{if(entry.state.state()==UfMusicClock.PAUSED)c.pause();else c.unpause();cursor=((UfMusicChannel)c).ufMusic$cursor();});
    }
    public void end(){stop();Minecraft.getInstance().getSoundManager().stop(this);}
}
