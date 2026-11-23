package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.*;import net.minecraft.world.level.*;import net.minecraft.core.BlockPos;
public final class RoverViewCollision {
 public static Vec3 clip(net.minecraft.world.entity.Entity rover,Vec3 target,Vec3 desired){Vec3 delta=desired.subtract(target);double length=delta.length();if(length<.001)return target;Vec3 direction=delta.scale(1/length);double allowed=length;
  for(double d=0;d<=length;d+=.5)if(!rover.level().hasChunkAt(BlockPos.containing(target.add(direction.scale(d))))){allowed=Math.max(0,d-.5);break;}
  for(int x:new int[]{-1,1})for(int y:new int[]{-1,1})for(int z:new int[]{-1,1}){Vec3 offset=new Vec3(x*.15,y*.15,z*.15);var hit=rover.level().clip(new ClipContext(target.add(offset),target.add(offset).add(direction.scale(allowed)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,rover));if(hit.getType()!=HitResult.Type.MISS)allowed=Math.min(allowed,Math.max(0,hit.getLocation().distanceTo(target.add(offset))-.2));}
  return target.add(direction.scale(allowed));
 }
}
