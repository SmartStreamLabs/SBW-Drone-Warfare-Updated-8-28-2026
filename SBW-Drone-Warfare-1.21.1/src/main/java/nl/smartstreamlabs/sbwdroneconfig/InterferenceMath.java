package nl.smartstreamlabs.sbwdroneconfig;

/** Presentation math only. Input is the existing authoritative jammer progress. */
public final class InterferenceMath {
    public static float strength(float progress, double threshold) {
        if (!Float.isFinite(progress) || !Double.isFinite(threshold) || progress <= (float)threshold) return 0;
        return clamp((float)((progress-threshold)/Math.max(.01,1-threshold)));
    }
    public static float smooth(float current,float target,double seconds) {
        if (!Float.isFinite(current)) current=0;
        target=clamp(target);
        double tau=target>current?.12:.28;
        return clamp((float)(current+(target-current)*(1-Math.exp(-Math.max(0,Math.min(.25,seconds))/tau))));
    }
    public static float event(int seed,int step) {
        int x=seed^step*0x9e3779b9;x^=x>>>16;x*=0x7feb352d;x^=x>>>15;x*=0x846ca68b;x^=x>>>16;
        return (x>>>8)/16777216f;
    }
    public static float clamp(float n){return Float.isFinite(n)?Math.max(0,Math.min(1,n)):0;}
    private InterferenceMath(){}
}
