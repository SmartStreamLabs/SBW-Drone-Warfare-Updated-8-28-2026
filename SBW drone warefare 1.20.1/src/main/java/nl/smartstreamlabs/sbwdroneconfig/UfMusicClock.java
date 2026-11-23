package nl.smartstreamlabs.sbwdroneconfig;

/** Server-owned monotonic transport. Saved settings deliberately exclude this transient clock. */
public final class UfMusicClock {
    public static final int STOPPED=0, PLAYING=1, PAUSED=2;
    private int status;
    private long offset, started;
    public int status(){return status;}
    public long position(long now,long duration,boolean repeat){
        long p=offset+(status==PLAYING?Math.max(0,(now-started)/1_000_000):0);
        return duration<=0?0:repeat?p%duration:Math.min(duration,p);
    }
    public void play(long now){if(status==PLAYING)return;if(status==STOPPED)offset=0;started=now;status=PLAYING;}
    public void pause(long now,long duration,boolean repeat){if(status!=PLAYING)return;offset=position(now,duration,repeat);status=PAUSED;}
    public void stop(){status=STOPPED;offset=0;started=0;}
    public void rebase(long now,long duration,boolean repeat){offset=position(now,duration,repeat);started=now;}
    public boolean finish(long now,long duration,boolean repeat){if(status==PLAYING&&!repeat&&position(now,duration,false)>=duration){stop();return true;}return false;}
    /** The audible radius does not change with source volume. Smoothstep reaches silence at its edge. */
    public static float gain(double distance,double radius,int volume){double t=Math.max(0,Math.min(1,distance/Math.max(1,radius)));return(float)(Math.max(0,Math.min(100,volume))*.01*(1-t*t*(3-2*t)));}
}
