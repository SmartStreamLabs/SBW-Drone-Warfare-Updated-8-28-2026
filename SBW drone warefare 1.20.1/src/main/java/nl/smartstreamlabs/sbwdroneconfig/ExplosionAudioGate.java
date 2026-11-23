package nl.smartstreamlabs.sbwdroneconfig;
/** Exact synchronous scope of a positively attributed vanilla explosion packet. */
public final class ExplosionAudioGate {
    public static boolean muted;
    public static void run(boolean replace,Runnable action) {
        boolean previous=muted;muted=previous||replace;
        try {action.run();} finally {muted=previous;}
    }
}
