package nl.smartstreamlabs.sbwdroneconfig;

/** Geometry only. No world state, client supplied targets, or chunk loading. */
public final class Dj01Cone {
    public static boolean contains(double x,double y,double z,double fx,double fy,double fz,double range,double fullDegrees) {
        double length2=x*x+y*y+z*z,forward2=fx*fx+fy*fy+fz*fz;
        if(!Double.isFinite(length2)||!Double.isFinite(forward2)||!(range>0)||!(fullDegrees>0)||fullDegrees>180||length2>=range*range||forward2<1e-12)return false;
        if(length2<1e-12)return true;
        return (x*fx+y*fy+z*fz)/Math.sqrt(length2*forward2)>=Math.cos(Math.toRadians(fullDegrees*.5))-1e-12;
    }
    private Dj01Cone(){}
}
