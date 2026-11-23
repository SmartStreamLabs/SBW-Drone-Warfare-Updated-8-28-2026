package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;

/** Bounded probes in already loaded columns; never requests chunks. All heights are world water surfaces. */
public final class Wd01Water {
 public record Surface(double height,double depth){}
 public record Support(int count,double height,float pitch,float roll) {public boolean floating(){return count>=3;}}
 public static Surface surface(Level level,Vec3 point){
  var column=BlockPos.containing(point);if(!level.hasChunkAt(column))return null;
  for(int y=column.getY()+2;y>=column.getY()-3;y--){
   var p=new BlockPos(column.getX(),y,column.getZ());var fluid=level.getFluidState(p);
   if(!fluid.is(FluidTags.WATER)||!(level.getBlockState(p).getBlock() instanceof LiquidBlock)||level.getFluidState(p.above()).is(FluidTags.WATER))continue;
   double height=y+fluid.getHeight(level,p);if(Math.abs(height-point.y)>2.1)continue;
   double bottom=y;for(int d=1;d<=4;d++){var below=p.below(d);if(!level.getFluidState(below).is(FluidTags.WATER)||!(level.getBlockState(below).getBlock() instanceof LiquidBlock))break;bottom=y-d;}
   return new Surface(height,height-bottom);
  }return null;
 }
 public static Vec3 point(Vec3 origin,float yaw,double x,double z){return origin.add(new Vec3(x,0,z).yRot(-yaw*net.minecraft.util.Mth.DEG_TO_RAD));}
 public static Support sample(Level level,Vec3 origin,float yaw){
  double[][] probes={{0,0},{0,1.7},{0,-1.7},{-.65,0},{.65,0}};double[] heights=new double[5];int n=0;double sum=0;
  for(int i=0;i<5;i++){var s=surface(level,point(origin,yaw,probes[i][0],probes[i][1]));heights[i]=s==null||s.depth()<.50?Double.NaN:s.height();if(Double.isFinite(heights[i])){sum+=heights[i];n++;}}
  float pitch=Double.isFinite(heights[1]+heights[2])?(float)Math.toDegrees(Math.atan2(heights[2]-heights[1],3.4)):0;
  float roll=Double.isFinite(heights[3]+heights[4])?(float)Math.toDegrees(Math.atan2(heights[4]-heights[3],1.3)):0;
  return new Support(n,n==0?origin.y:sum/n,net.minecraft.util.Mth.clamp(pitch,-7,7),net.minecraft.util.Mth.clamp(roll,-7,7));
 }
 private Wd01Water(){}
}
