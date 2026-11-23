package nl.smartstreamlabs.sbwdroneconfig;

import org.joml.Matrix4d;
import org.joml.Vector3d;

/** Supplied revision-2 anchors, native -Z/16 units converted once to SBW +Z/metres. */
public final class Adv01Frames {
    public static final double SEAT_Y = 1.065 - .75;
    public static Matrix4d turret(Matrix4d body,double yaw) {
        return new Matrix4d(body).translate(0,2.43,-1.32).rotateY(Math.toRadians(yaw));
    }
    public static Matrix4d gun(Matrix4d turret,double nativePitch) {
        return new Matrix4d(turret).translate(0,.55,.92).rotateX(Math.toRadians(nativePitch));
    }
    public static Vector3d muzzle(Matrix4d gun) { return gun.transformPosition(new Vector3d(0,.065,2.63)); }
    public static Matrix4d sensor(Matrix4d turret,double pitch) {
        return new Matrix4d(turret).translate(.15,.73,-.12).translate(0,.19,.02)
            .rotateX(Math.toRadians(pitch)).translate(0,.015,.29);
    }
    public static Matrix4d drive(Matrix4d body) { return new Matrix4d(body).translate(0,1.88,2.39); }
    public static Vector3d seat(Matrix4d body,int index) {
        return new Matrix4d(body).transformPosition(new Vector3d(index==0?.6:-.6,SEAT_Y,1.2));
    }
    /** Closest point on the swept game projectile path; handles zero-length paths. */
    public static double closestFraction(double ax,double ay,double az,double bx,double by,double bz,double x,double y,double z) {
        double dx=bx-ax,dy=by-ay,dz=bz-az,n=dx*dx+dy*dy+dz*dz;
        return n<1e-12?0:Math.max(0,Math.min(1,((x-ax)*dx+(y-ay)*dy+(z-az)*dz)/n));
    }
    private Adv01Frames() {}
}
