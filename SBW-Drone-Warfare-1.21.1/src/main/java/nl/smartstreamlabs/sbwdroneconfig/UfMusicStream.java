package nl.smartstreamlabs.sbwdroneconfig;

import java.io.*;import java.nio.ByteBuffer;import java.util.*;import java.util.function.*;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.*;

/** Native Vorbis decoder with bounded PCM discard for seeking; preserves partial-frame overshoot. */
public final class UfMusicStream implements AudioStream {
    @FunctionalInterface public interface Input {InputStream open()throws IOException;}
    private final Input input;private final BooleanSupplier repeat;private final AudioFormat format;
    private AudioStream decoder;private ByteBuffer pending;private boolean closed;
    private final ArrayDeque<Integer> queuedFrames=new ArrayDeque<>();private long consumedFrames;
    private final long originMs;
    public UfMusicStream(Input input,long milliseconds,BooleanSupplier repeat)throws IOException {
        this.input=input;this.repeat=repeat;decoder=open();format=decoder.getFormat();
        if(format.getChannels()!=1){decoder.close();throw new IOException("UF01 requires a mono Vorbis recording for positional playback");}
        originMs=milliseconds;long bytes=(long)(milliseconds*format.getSampleRate()/1000)*format.getFrameSize();
        while(bytes>0){ByteBuffer b=decoder.read((int)Math.min(32768,bytes));if(b==null||!b.hasRemaining())break;
            int skip=(int)Math.min(bytes,b.remaining());b.position(b.position()+skip);bytes-=skip;if(b.hasRemaining())pending=b.slice();}
    }
    private AudioStream open()throws IOException {InputStream s=input.open();try{return new JOrbisAudioStream(s);}catch(IOException|RuntimeException e){s.close();throw e;}}
    @Override public AudioFormat getFormat(){return format;}
    @Override public synchronized ByteBuffer read(int bytes)throws IOException {
        if(closed)return null;ByteBuffer b=pending;pending=null;
        if(b==null||!b.hasRemaining())b=decoder.read(bytes);
        if((b==null||!b.hasRemaining())&&repeat.getAsBoolean()){decoder.close();decoder=open();b=decoder.read(bytes);}
        if(b==null||!b.hasRemaining())return null;
        queuedFrames.addLast(b.remaining()/format.getFrameSize());return b;
    }
    /** Called exclusively by our native channel hook after OpenAL unqueues its processed buffers. */
    public synchronized void processed(int buffers){while(buffers-->0&&!queuedFrames.isEmpty())consumedFrames+=queuedFrames.removeFirst();}
    public synchronized double cursor(int processed,float bufferSeconds){long frames=consumedFrames;int n=0;for(int f:queuedFrames){if(n++>=processed)break;frames+=f;}return originMs+1000d*frames/format.getSampleRate()+bufferSeconds*1000d;}
    @Override public synchronized void close()throws IOException {if(!closed){closed=true;decoder.close();queuedFrames.clear();pending=null;}}
}
