package nl.smartstreamlabs.sbwdroneconfig;
/** Slab intersection in relative coordinates. Both moving entities are sampled at the same endpoints. */
public final class Id01ContactMath {
    public static boolean segment(double[] start,double[] end,double[] low,double[] high){
        double enter=0,leave=1;
        for(int axis=0;axis<3;axis++){
            double a=start[axis],delta=end[axis]-a;
            if(!Double.isFinite(a)||!Double.isFinite(delta)||!Double.isFinite(low[axis])||!Double.isFinite(high[axis]))return false;
            if(Math.abs(delta)<1e-10){if(a<low[axis]||a>high[axis])return false;continue;}
            double t0=(low[axis]-a)/delta,t1=(high[axis]-a)/delta;
            enter=Math.max(enter,Math.min(t0,t1));leave=Math.min(leave,Math.max(t0,t1));if(enter>leave)return false;
        }
        return true;
    }
}
