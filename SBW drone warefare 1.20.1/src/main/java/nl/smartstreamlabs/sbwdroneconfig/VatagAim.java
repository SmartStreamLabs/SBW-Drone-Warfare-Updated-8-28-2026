package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.*;
/** Mouse angles use Minecraft world yaw/pitch. SBW turret yaw uses the opposite local sign.
 * Invert the complete hull attitude so aiming stays stable while driving and climbing. */
public final class VatagAim {
 private VatagAim(){}
 public static Vector2f local(Matrix4d hull,float yaw,float pitch){double y=java.lang.Math.toRadians(yaw),p=java.lang.Math.toRadians(pitch);Vector3d world=new Vector3d(-java.lang.Math.sin(y)*java.lang.Math.cos(p),-java.lang.Math.sin(p),java.lang.Math.cos(y)*java.lang.Math.cos(p));Vector3d v=new Matrix4d(hull).invert().transformDirection(world).normalize();return new Vector2f((float)java.lang.Math.toDegrees(java.lang.Math.atan2(v.x,v.z)),(float)java.lang.Math.toDegrees(-java.lang.Math.asin(java.lang.Math.max(-1,java.lang.Math.min(1,v.y)))));}
 public static float roll(Matrix4d frame){Vector3d forward=frame.transformDirection(new Vector3d(0,0,1)).normalize(),up=frame.transformDirection(new Vector3d(0,1,0)).normalize();Vector3d reference=new Vector3d(0,1,0).sub(new Vector3d(forward).mul(forward.y));if(reference.lengthSquared()<1e-8)return 0;reference.normalize();return (float)java.lang.Math.toDegrees(java.lang.Math.atan2(forward.dot(new Vector3d(reference).cross(up)),reference.dot(up)));}
}
