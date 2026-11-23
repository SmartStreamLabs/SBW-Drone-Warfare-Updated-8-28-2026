package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.util.Mth;import net.minecraft.world.phys.*;import org.joml.Matrix4d;import org.joml.Vector3d;
/** Native block-space anchors. This exact transform is shared by server, renderer and camera. */
public final class Hx2Pose {
 public static final double RAIL_PITCH=35,RAIL_Y=.88,DOCK_Y=1.025,DOCK_Z=.35,DRONE_DOCK_Y=-.139;
 public static Matrix4d platform(BlockPos p,Direction facing){return new Matrix4d().translation(p.getX()+.5,p.getY(),p.getZ()+.5).rotateY(Math.toRadians(180-facing.toYRot()));}
 public static Matrix4d dock(BlockPos p,Direction facing,double carriage){
  return platform(p,facing).translate(0,RAIL_Y,0).rotateX(Math.toRadians(RAIL_PITCH)).translate(0,-RAIL_Y,0)
    .translate(0,0,carriage).translate(0,DOCK_Y,DOCK_Z).translate(0,-DRONE_DOCK_Y,0);
 }
 public static Matrix4d flight(Hx2Entity e,float partial){Vec3 p=e.getPosition(partial);return new Matrix4d().translation(p.x,p.y,p.z).rotateY(Math.toRadians(180-Mth.rotLerp(partial,e.yRotO,e.getYRot()))).rotateX(Math.toRadians(-Mth.lerp(partial,e.xRotO,e.getXRot()))).rotateZ(Math.toRadians(e.getRoll(partial)));}
 public static Matrix4d frame(Hx2Entity e,float partial){
  var mount=e.mount();if(mount!=null){
   net.minecraft.world.level.block.entity.BlockEntity block;
   if(e.level() instanceof net.minecraft.server.level.ServerLevel server){
    // Collision caches can query a parked drone outside ticking chunks. Never renew UNKNOWN tickets here.
    var chunk=server.getChunkSource().getChunkNow(mount.getX()>>4,mount.getZ()>>4);
    block=chunk==null?null:chunk.getBlockEntity(mount);
   }else block=e.level().getBlockEntity(mount);
   if(block instanceof HxPlatformBlockEntity be&&e.getUUID().equals(be.droneId()))return dock(mount,be.facing(),be.carriage(partial));
  }return flight(e,partial);
 }
 public static Vec3 point(Matrix4d m,Vec3 p){var v=m.transformPosition(new Vector3d(p.x,p.y,p.z));return new Vec3(v.x,v.y,v.z);}
 public static Vec3 direction(Matrix4d m){var v=m.transformDirection(new Vector3d(0,0,-1));return new Vec3(v.x,v.y,v.z).normalize();}
 public static Vec3 camera(Hx2Entity e,float partial){return point(frame(e,partial),new Vec3(0,.024,-.845));}
 public static double carriage(double age){return Hx2Kinematics.carriage(age);}
 public static float latches(double age){return (float)(age<8?Mth.clamp(age/8,0,1):age<22?1:1-Mth.clamp((age-22)/18,0,1));}
 private Hx2Pose(){}
}
