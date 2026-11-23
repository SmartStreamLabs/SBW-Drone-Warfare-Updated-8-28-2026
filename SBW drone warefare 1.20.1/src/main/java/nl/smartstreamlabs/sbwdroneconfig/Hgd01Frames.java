package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.*;
/** Current R3 anchors, converted once from native -Z/16 units to SBW's +Z vehicle space. */
public final class Hgd01Frames {
 public static Matrix4d turret(Matrix4d hull,double yaw){return new Matrix4d(hull).translate(0,2.14,-.04).rotateY(java.lang.Math.toRadians(yaw));}
 public static Matrix4d gun(Matrix4d turret,double pitch){return new Matrix4d(turret).translate(0,.60,.46).rotateX(java.lang.Math.toRadians(pitch));}
 public static Matrix4d laser(Matrix4d turret,double pitch,int side){return new Matrix4d(turret).translate(side==0?.76:-.76,.8,-.34).rotateX(java.lang.Math.toRadians(pitch));}
 public static Matrix4d sensor(Matrix4d turret,double pitch){return new Matrix4d(turret).translate(.22,.74,-.28).translate(0,.14,.02).rotateX(java.lang.Math.toRadians(pitch)).translate(0,.01,.2);}
 public static Vector3d point(Matrix4d frame,double x,double y,double z){return frame.transformPosition(new Vector3d(x,y,z));}
 public static float maximumPitch(float yaw){double a=java.lang.Math.IEEEremainder(yaw,360);return java.lang.Math.abs(a)>105?-12:5;}
 public static double seatX(int i){return i<2?.8:-.8;}public static double seatZ(int i){return i%2==0?-.9858:-2.1624;}
 /** Minecraft seated pelvis is 0.75 above the player entity origin; no second riding offset. */
 public static final double SEAT_ENTITY_Y=1.1975-.75;
 private Hgd01Frames(){}
}
