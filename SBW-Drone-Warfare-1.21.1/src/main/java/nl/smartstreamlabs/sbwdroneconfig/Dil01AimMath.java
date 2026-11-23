package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.Vec3;import net.minecraft.world.entity.player.Player;import net.minecraft.world.entity.HumanoidArm;
/** All values already in blocks; native model front is -Z. Shared by hands, item and projectile muzzle. */
public final class Dil01AimMath {
 public static final Vec3 MUZZLE=new Vec3(0,.16,-.72),MAIN=new Vec3(0,-.11,.235),SUPPORT=new Vec3(0,-.07,-.285);
 public static Vec3 origin(boolean aim){return aim?new Vec3(.129,-.35,-.432):new Vec3(.32,-.4,-.44);}
 public static org.joml.Matrix4f local(boolean aim,boolean right){var p=origin(aim);return new org.joml.Matrix4f().scaling(right?1:-1,1,1).translate((float)p.x,(float)p.y,(float)p.z);}
 public static Vec3 muzzle(Player p,boolean aim){var v=Dil01HeldPose.relative(p,1,aim).transformPosition(new org.joml.Vector3f(0,.16f,-.72f));return p.getPosition(1).add(v.x,v.y,v.z);}

 private Dil01AimMath(){}
}
