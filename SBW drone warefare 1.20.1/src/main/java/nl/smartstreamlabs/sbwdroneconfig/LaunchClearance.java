package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
/** Exact occupied voxel intersections: a compound block's empty gaps are not launch obstructions. */
public final class LaunchClearance {
 public static boolean intersects(VoxelShape shape,net.minecraft.core.BlockPos pos,AABB area){
  return !shape.isEmpty()&&Shapes.joinIsNotEmpty(shape.move(pos.getX(),pos.getY(),pos.getZ()),Shapes.create(area),BooleanOp.AND);
 }
 public static AABB lucas(Vec3 start,Vec3 end,Direction facing){
  boolean alongX=facing.getAxis()==Direction.Axis.X;
  return new AABB(start,end).inflate(alongX?2.1:3.5,0,alongX?3.5:2.1).expandTowards(0,1.1,0);
 }
 private LaunchClearance(){}
}
