package nl.smartstreamlabs.sbwdroneconfig;
/** R_y(-yaw) R_x(pitch) R_x(-tilt), operating in native vehicle coordinates. */
public final class Id01TransformMath {
    public static double[] direction(double x,double y,double z,double yaw,double pitch,double tilt){
        double r=Math.toRadians(pitch-tilt),a=Math.toRadians(-yaw);
        double ry=y*Math.cos(r)-z*Math.sin(r),rz=y*Math.sin(r)+z*Math.cos(r);
        return new double[]{x*Math.cos(a)+rz*Math.sin(a),ry,-x*Math.sin(a)+rz*Math.cos(a)};
    }
}
