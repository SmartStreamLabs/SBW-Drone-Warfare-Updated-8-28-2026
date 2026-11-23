package nl.smartstreamlabs.sbwdroneconfig;
/** Pure, testable propagation and adjacent-layer crossfade. */
public final class ExplosionAudioMath {
    /** Continuous distance boost, leaving the supplied close recording unchanged. */
    public static double distanceBoost(double distance) {
        double t=Math.max(0,Math.min(1,(distance-45)/115));
        return 1+3*t*t*(3-2*t);
    }

    public static double delay(double distance,double speed,boolean enabled) {return enabled?Math.max(0,distance)/speed:0;}
    public static long arrival(long emitted,double distance,double ticksPerBlock){return emitted+(long)Math.ceil(Math.max(0,distance)*ticksPerBlock);}
    public static double gain(double distance,double range) {
        double edge=Math.max(0,Math.min(1,1-distance/range));
        return Math.pow(edge,.8)/Math.sqrt(1+distance/300.0);
    }
    public static double layerPosition(double distance) {
        double[] edges={0,45,160,420};
        for(int i=0;i<3;i++) if(distance<edges[i+1]) {
            double t=(distance-edges[i])/(edges[i+1]-edges[i]); return i+t*t*(3-2*t);
        }
        return 3;
    }
}
