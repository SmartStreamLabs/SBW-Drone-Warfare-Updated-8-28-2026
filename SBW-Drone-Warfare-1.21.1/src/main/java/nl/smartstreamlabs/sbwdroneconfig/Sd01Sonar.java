package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.*;import net.minecraft.world.level.*;import net.minecraft.core.BlockPos;import java.util.*;
/** 26 first-surface rays per deliberate pulse. No entities, ores, containers, or behind-wall queries. */
public final class Sd01Sonar {
 public static String scan(Sd01Entity d){int range=Sd01Config.SONAR_RANGE.get();var out=new StringBuilder().append(range);var m=d.getVehicleTransform(1);for(int i=0;i<12;i++){double a=i*Math.PI/6;double best=-1;for(double y:new double[]{-.25,.25}){var dir=m.transformDirection(new org.joml.Vector3d(Math.sin(a),y,Math.cos(a))).normalize();double r=ray(d,d.position(),new Vec3(dir.x,dir.y,dir.z),range);if(r>=0)best=best<0?r:Math.min(best,r);}out.append(',').append(best<0?-1:Math.round(best*10));}for(int sign:new int[]{-1,1}){double r=ray(d,d.position(),new Vec3(0,sign,0),range);out.append(',').append(r<0?-1:Math.round(r*10));}return out.toString();}
 public static double ray(Sd01Entity d,Vec3 at,Vec3 direction,double range){for(double r=0;r<=range;r+=1)if(!d.level().hasChunkAt(BlockPos.containing(at.add(direction.scale(r)))))return -1;var hit=d.level().clip(new ClipContext(at,at.add(direction.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,d));return hit.getType()==HitResult.Type.BLOCK?at.distanceTo(hit.getLocation()):-1;}
 public static float[] decode(String s){float[] out=new float[15];Arrays.fill(out,-1);var words=s.split(",");if(words.length!=15)return out;try{for(int i=0;i<15;i++)out[i]=Float.parseFloat(words[i])/(i==0?1:10);}catch(NumberFormatException ex){Arrays.fill(out,-1);}return out;}
 private Sd01Sonar(){}
}
