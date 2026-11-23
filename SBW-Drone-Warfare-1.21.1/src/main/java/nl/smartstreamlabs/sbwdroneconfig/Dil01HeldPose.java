package nl.smartstreamlabs.sbwdroneconfig;
import org.joml.*;import java.lang.Math;import net.minecraft.world.entity.player.Player;import net.minecraft.world.entity.HumanoidArm;import net.minecraft.util.Mth;
/** One native-scale shoulder frame shared by player arms, held item and real muzzle; renderer and muzzle share it. */
public final class Dil01HeldPose {
 public static final float PLAYER_SCALE=.9375f;
 public record Pose(Matrix4f item,Vector3f main,Vector3f support){}
 public static Pose solve(float yaw,float pitch,boolean right,boolean aim,Vector3f mainRoot,Vector3f supportRoot,float headY){
  float sign=right?1:-1;var orient=new Matrix4f().rotateY(yaw).rotateX(pitch).rotateZ((float)Math.PI).scale(sign/PLAYER_SCALE,1/PLAYER_SCALE,1/PLAYER_SCALE);
  var origin=Dil01AimMath.origin(aim);var frame=new Matrix4f().translation(0,(headY-1.92f)/16,0).mul(orient).translate((float)origin.x,(float)origin.y,(float)origin.z);
  return new Pose(frame,frame.transformPosition(new Vector3f(0,-.11f,.235f)).mul(16),frame.transformPosition(new Vector3f(0,-.07f,-.285f)).mul(16));
 }
 /** First-person geometry is expressed in camera space; interpolated avatar head/body yaw must not turn it behind the camera. */
 public static Matrix4f firstPerson(boolean right,boolean crouch,float eyeHeight,boolean aim){var origin=Dil01AimMath.origin(aim);float sign=right?1:-1;float shoulder=(1.501f-(crouch?(4.2f-1.92f)/16f:-1.92f/16f))*PLAYER_SCALE;return new Matrix4f().translation(sign*(float)origin.x,shoulder-eyeHeight+(float)origin.y,(float)origin.z).scale(sign,1,1);}
 public static Matrix4f relative(Player p,float partial,boolean aim){boolean right=p.getMainArm()==HumanoidArm.RIGHT;boolean crouch=p.isCrouching();float body=Mth.rotLerp(partial,p.yBodyRotO,p.yBodyRot),head=Mth.rotLerp(partial,p.yHeadRotO,p.yHeadRot),pitch=Mth.lerp(partial,p.xRotO,p.getXRot());float y=crouch?5.2f:2,z=crouch?4:0,sign=right?1:-1;var pose=solve((float)Math.toRadians(head-body),(float)Math.toRadians(pitch),right,aim,new Vector3f(-5*sign,y,z),new Vector3f(5*sign,y,z),crouch?4.2f:0);var at=p.getPosition(partial);return new Matrix4f().rotateY((float)Math.toRadians(180-body)).scale(-PLAYER_SCALE,-PLAYER_SCALE,PLAYER_SCALE).translate(0,-1.501f,0).mul(pose.item());}
 private Dil01HeldPose(){}
}
