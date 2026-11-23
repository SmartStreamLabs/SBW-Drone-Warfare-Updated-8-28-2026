package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.Matrix4d;import org.joml.Vector3d;import net.minecraft.world.phys.Vec3;
/** Native R03 and PM-01 coordinates; the 16-unit conversion is performed only in constants. */
public final class Mpl01Frames {
 public static final Vector3d PIVOT=new Vector3d(0,33.92/16,78.08/16);
 public static Matrix4d launcher(Matrix4d body,double degrees){return new Matrix4d(body).rotateY(Math.PI).translate(PIVOT).rotateX(Math.toRadians(degrees)).translate(-PIVOT.x,-PIVOT.y,-PIVOT.z);}
 public static Vector3d mount(int slot){return new Vector3d(slot%2==0?-.55:.55,slot<2?2.85:3.71,1.74);}
 public static Vec3 point(Matrix4d m,Vector3d p){var v=m.transformPosition(new Vector3d(p));return new Vec3(v.x,v.y,v.z);}
 public static Vec3 forward(Matrix4d m){var v=m.transformDirection(new Vector3d(0,0,-1)).normalize();return new Vec3(v.x,v.y,v.z);}
 public static Vec3 nativePoint(Matrix4d body,double x,double y,double z){return point(new Matrix4d(body).rotateY(Math.PI),new Vector3d(x,y,z));}
 public static float yaw(Vec3 v){return(float)Math.toDegrees(-Math.atan2(v.x,v.z));}public static float pitch(Vec3 v){return(float)-Math.toDegrees(Math.atan2(v.y,v.horizontalDistance()));}
 public static double angle(double pose){return 55*Math.max(0,Math.min(1,(pose*4-1.5)/2.5));}
 private Mpl01Frames(){}
}
