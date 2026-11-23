package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.tools.OBB;
import net.minecraft.core.*;import net.minecraft.tags.FluidTags;import net.minecraft.world.entity.Entity;import net.minecraft.world.level.Level;import net.minecraft.world.phys.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4d;import org.joml.Vector3d;import org.joml.Quaterniond;import java.util.*;
/** Native SBW OBB narrow phase for the authored hull, fins, masts and lens. No centre-only clearance. */
public final class Sd01Hull {
 public record Part(double x,double y,double z,double hx,double hy,double hz){}
 // SBW coordinates: model -Z becomes +Z, Y=0 is the authored hull centre (9.6 native units).
 public static final Part[] PARTS={
  new Part(0,0,.25,.45,.425,2.80),new Part(0,0,3.17,.30,.30,.54),new Part(0,-.02,-3.12,.31,.30,.68),
  new Part(0,.735,-.025,.08,.35,.81),
  new Part(-.905,-.225,-2.28,.725,.18,1.04),new Part(.905,-.225,-2.28,.725,.18,1.04),
  new Part(0,.32,-3.295,.025,.19,.38),new Part(0,-.315,-3.25,.025,.19,.40),
  new Part(0,.555,2.58,.14,.165,.14),new Part(0,.555,-2.40,.14,.165,.14),
  new Part(0,-.01,-3.70,.12,.12,.09),new Part(0,.11,3.78,.12,.12,.12)
 };
 public static Matrix4d frame(Vec3 at,float yaw,float pitch){return new Matrix4d().translate(at.x,at.y,at.z).rotateY(-yaw*Math.PI/180).rotateX(pitch*Math.PI/180);}
 public static List<OBB> parts(Vec3 at,float yaw,float pitch){var m=frame(at,yaw,pitch);var q=m.getUnnormalizedRotation(new Quaterniond());var result=new ArrayList<OBB>(PARTS.length);for(var p:PARTS)result.add(new OBB(m.transformPosition(new Vector3d(p.x,p.y,p.z)),new Vector3d(p.hx,p.hy,p.hz),new Quaterniond(q),OBB.Part.BODY));return result;}
 public static AABB bounds(Vec3 at,float yaw,float pitch){var m=frame(at,yaw,pitch);double lx=Double.POSITIVE_INFINITY,ly=lx,lz=lx,hx=Double.NEGATIVE_INFINITY,hy=hx,hz=hx;for(var p:PARTS)for(int i=0;i<8;i++){var v=m.transformPosition(new Vector3d(p.x+((i&1)==0?-p.hx:p.hx),p.y+((i&2)==0?-p.hy:p.hy),p.z+((i&4)==0?-p.hz:p.hz)));lx=Math.min(lx,v.x);ly=Math.min(ly,v.y);lz=Math.min(lz,v.z);hx=Math.max(hx,v.x);hy=Math.max(hy,v.y);hz=Math.max(hz,v.z);}return new AABB(lx,ly,lz,hx,hy,hz);}
 public static boolean loaded(Level l,AABB b){return b.minY>=l.getMinBuildHeight()&&b.maxY<l.getMaxBuildHeight()&&l.getWorldBorder().isWithinBounds(b)&&l.hasChunksAt(BlockPos.containing(b.minX,b.minY,b.minZ),BlockPos.containing(b.maxX,b.maxY,b.maxZ));}
 public static boolean free(Sd01Entity d,Vec3 at,float yaw,float pitch){var l=d.level();var b=bounds(at,yaw,pitch);if(!loaded(l,b))return false;var parts=parts(at,yaw,pitch);int checked=0;for(var shape:l.getBlockCollisions(d,b))for(var block:shape.toAabbs()){if(++checked>256)return false;for(var part:parts)if(OBB.isColliding(part,block.deflate(.0001)))return false;}for(var e:l.getEntities(d,b,e->e.isPickable()&&!e.isSpectator())){if(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity vehicle){for(var p:parts)for(var other:vehicle.getOBBs())if(OBB.isColliding(p,other))return false;}else for(var p:parts)if(OBB.isColliding(p,e.getBoundingBox().deflate(.0001)))return false;}return true;}
 /** Check the actual probe and its small surface margin in their respective fluid cells.
  * The top face of a full water voxel is not a surface when water continues above it. */
 public static boolean waterAt(Level l,Vector3d v){return waterSample(l,v.x,v.y,v.z)&&waterSample(l,v.x,v.y+.015,v.z);}
 private static boolean waterSample(Level l,double x,double y,double z){var p=BlockPos.containing(x,y,z);if(!l.hasChunkAt(p))return false;var f=l.getFluidState(p);return f.is(FluidTags.WATER)&&p.getY()+f.getHeight(l,p)>y;}
 public static boolean waterPose(Sd01Entity d,Vec3 at,float yaw,float pitch){var m=frame(at,yaw,pitch);for(double z:new double[]{-3.4,0,3.4})for(double y:new double[]{-.25,.38})if(!waterAt(d.level(),m.transformPosition(new Vector3d(0,y,z))))return false;for(double x:new double[]{-1.5,1.5})if(!waterAt(d.level(),m.transformPosition(new Vector3d(x,-.20,-2.2))))return false;return true;}
 public static boolean deployable(Sd01Entity d,Player p,ItemStack stack){var b=bounds(d.position(),d.getYRot(),0);if(!free(d,d.position(),d.getYRot(),0)||!waterPose(d,d.position(),d.getYRot(),0))return false;var parts=parts(d.position(),d.getYRot(),0);int count=0;for(var pos:BlockPos.betweenClosed(BlockPos.containing(b.minX,b.minY,b.minZ),BlockPos.containing(b.maxX,b.maxY,b.maxZ))){if(++count>512)return false;var block=new AABB(pos).deflate(.002);boolean occupied=false;for(var o:parts)if(OBB.isColliding(o,block)){occupied=true;break;}if(!occupied)continue;var fluid=d.level().getFluidState(pos);if(!fluid.is(FluidTags.WATER)||!d.level().mayInteract(p,pos)||!p.mayUseItemAt(pos,Direction.UP,stack))return false;double top=pos.getY()+fluid.getHeight(d.level(),pos);if(top<pos.getY()+.998){var dry=new AABB(pos.getX(),top,pos.getZ(),pos.getX()+1,pos.getY()+1,pos.getZ()+1).deflate(.002);for(var o:parts)if(OBB.isColliding(o,dry))return false;}}return true;}
 /** At most 8 translational substeps, then four bisections. Native SBW still resolves the final move. */
 public static Vec3 swept(Sd01Entity d,Vec3 v,boolean stayWet){if(v.lengthSqr()<1e-12)return Vec3.ZERO;if(v.lengthSqr()>1)v=v.normalize();int steps=Math.min(8,Math.max(1,(int)Math.ceil(v.length()/.12)));double safe=0;for(int i=1;i<=steps;i++){double t=i/(double)steps;Vec3 at=d.position().add(v.scale(t));if(!free(d,at,d.getYRot(),d.getXRot())||stayWet&&!waterPose(d,at,d.getYRot(),d.getXRot())){double hi=t;for(int k=0;k<4;k++){double mid=(safe+hi)*.5;at=d.position().add(v.scale(mid));if(free(d,at,d.getYRot(),d.getXRot())&&(!stayWet||waterPose(d,at,d.getYRot(),d.getXRot())))safe=mid;else hi=mid;}return v.scale(Math.max(0,safe-.002));}safe=t;}return v;}
 private Sd01Hull(){}
}
