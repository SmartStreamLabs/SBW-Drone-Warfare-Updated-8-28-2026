package nl.smartstreamlabs.sbwdroneconfig;

/** One pending/active voice and a full quiet minute measured from actual completion. */
public final class DistanceWarPlaybackGate {
    private boolean reserved;private long quietUntil;
    public boolean reserve(long scheduledStart){if(reserved||scheduledStart<quietUntil)return false;reserved=true;return true;}
    public void finish(long actualEnd){if(reserved){quietUntil=Math.max(quietUntil,actualEnd+1200);reserved=false;}}
    public void resetWorld(){reserved=false;quietUntil=0;}
    public boolean busy(){return reserved;}
    public long quietUntil(){return quietUntil;}
}
