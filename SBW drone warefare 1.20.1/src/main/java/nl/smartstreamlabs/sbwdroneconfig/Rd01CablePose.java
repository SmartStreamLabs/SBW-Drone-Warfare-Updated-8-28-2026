package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.*;
/** Fits the supplied cable mesh (Y=37..42.5) between the fairlead and moving eye, in model units. */
public final class Rd01CablePose {
    public record Fit(Vector3d offset,Vector3d rotation,double scaleY){}
    public static Fit fit(Vector3dc eye){
        Vector3d delta=new Vector3d(eye).sub(0,42.7,1);
        double length=delta.length(),scale=java.lang.Math.max(.001,length)/5.5;
        Quaterniond q=length<1e-8?new Quaterniond():new Quaterniond().rotationTo(new Vector3d(0,-1,0),delta.div(length));
        // The native top is .2 units below its bone pivot; keep it precisely at the fairlead after stretch.
        return new Fit(q.transform(new Vector3d(0,.2*scale,0)),q.getEulerAnglesZYX(new Vector3d()),scale);
    }
    private Rd01CablePose(){}
}
