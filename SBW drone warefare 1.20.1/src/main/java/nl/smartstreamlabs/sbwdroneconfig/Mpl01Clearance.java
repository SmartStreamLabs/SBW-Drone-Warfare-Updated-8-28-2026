package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.phys.*;import net.minecraft.world.level.*;import net.minecraft.world.entity.*;import org.joml.Vector3d;
public final class Mpl01Clearance {
 /** Samples only the bounded launcher sweep and four actual support feet; never loads chunks. */
 public static String check(Mpl01Entity e,boolean ground){
  var l=e.level();var body=e.getVehicleTransform(1);if(Math.abs(e.getXRot())>10||Math.abs(e.getRoll())>10)return "Ground is too steep";
  if(ground){int i=0;for(double x:new double[]{-2.5,2.5})for(double z:new double[]{-.83,4.65}){var at=Mpl01Frames.nativePoint(body,x,0,z);boolean found=false;for(int d=-1;d<=1;d++){var p=BlockPos.containing(at.add(0,d-.04,0));if(!l.hasChunkAt(p))return "Support area is not loaded";if(l.getBlockState(p).isFaceSturdy(l,p,Direction.UP)){double delta=p.getY()+1-at.y;if(Math.abs(delta)<=.55){e.jackOffset(i,(float)delta);found=true;break;}}}if(!found)return "All four support feet need firm ground";i++;}}
  for(int a=0;a<=6;a++){var m=Mpl01Frames.launcher(body,55d*a/6);Vec3 lo=null,hi=null;for(double x:new double[]{-1.12,1.12})for(double y:new double[]{2.43,4.1})for(double z:new double[]{-1.65,5.12}){var p=Mpl01Frames.point(m,new Vector3d(x,y,z));lo=lo==null?p:new Vec3(Math.min(lo.x,p.x),Math.min(lo.y,p.y),Math.min(lo.z,p.z));hi=hi==null?p:new Vec3(Math.max(hi.x,p.x),Math.max(hi.y,p.y),Math.max(hi.z,p.z));}var box=new AABB(lo,hi).deflate(.035);if(box.maxY>=l.getMaxBuildHeight()||!l.getWorldBorder().isWithinBounds(box)||!l.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))return "Launcher clearance is outside the loaded build area";if(l.getBlockCollisions(e,box).iterator().hasNext())return "Launcher sweep is obstructed";if(!l.getEntities(e,box,v->v.isPickable()&&v.getVehicle()!=e&&!e.getPassengers().contains(v)).isEmpty())return "Move players or vehicles out of the launcher sweep";}
  for(double x:new double[]{-2.5,2.5})for(double z:new double[]{-.83,4.65}){var p=Mpl01Frames.nativePoint(body,x,.8,z);if(l.getBlockCollisions(e,new AABB(p.add(-.25,-.6,-.25),p.add(.25,.8,.25))).iterator().hasNext())return "Outrigger space is obstructed";}
  return "";
 }
 private Mpl01Clearance(){}
}
