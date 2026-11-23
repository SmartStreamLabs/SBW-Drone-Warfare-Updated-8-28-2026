package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.*;

public final class Lm01Placement {
 public static final class RobotContext extends BlockPlaceContext {
  private final VatagEntity robot;
  RobotContext(VatagEntity robot,ServerPlayer p,ItemStack item,BlockHitResult hit){super(robot.level(),p,InteractionHand.MAIN_HAND,item,hit);this.robot=robot;}
  @Override public Direction getHorizontalDirection(){return robot.getDirection();}
 }
 /** Uses native item use/snapshot rollback so placement-protection events and item consumption remain vanilla. */
 public static boolean deploy(VatagEntity robot,ServerPlayer p,int slots){
  if(!(robot.level()instanceof ServerLevel l)||robot.operator()!=p||!VatagSessions.controlling(p,robot)||!robot.permitted(p)||robot.isWreck()||!robot.isAlive()||robot.getDeltaMovement().horizontalDistance()>.04)return false;
  int slot=-1;for(int i=0;i<slots;i++)if(robot.getItem(i).is(AddonItems.LM01.get())){slot=i;break;}if(slot<0)return false;
  // Existing MR deploy anchor / GAD rear point beyond the native collision envelope, then a bounded ground ray in loaded chunks.
  var transform=robot.getVehicleTransform(1);
  double rear=robot instanceof MineRoverEntity?27.128810753898275/16+.6:-1.15;
  // A previously deployed mine is not a blocked vehicle. Try the six real
  // ground positions behind the rack, without replacing blocks or other mines.
  var visited=new java.util.HashSet<BlockPos>();
  for(double[] offset:new double[][]{{0,0},{-1,0},{1,0},{0,1},{-1,1},{1,1}}){
   double farther=robot instanceof MineRoverEntity?offset[1]:-offset[1];
   var local=transform.transformPosition(new org.joml.Vector3d(offset[0],.7,rear+farther));
   Vec3 start=new Vec3(local.x,local.y+1,local.z),end=start.add(0,-3,0);var probe=BlockPos.containing(end);
   if(l.getChunkSource().getChunkNow(probe.getX()>>4,probe.getZ()>>4)==null)continue;
   var hit=l.clip(new net.minecraft.world.level.ClipContext(start,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,robot));
   if(hit.getType()!=HitResult.Type.BLOCK||hit.getDirection()!=Direction.UP)continue;
   var pos=hit.getBlockPos().above();if(!visited.add(pos)||!l.getWorldBorder().isWithinBounds(pos)||l.isOutsideBuildHeight(pos)||!l.mayInteract(p,pos)||!l.mayInteract(p,hit.getBlockPos())||!p.mayUseItemAt(pos,Direction.UP,robot.getItem(slot))||!l.getBlockState(pos).canBeReplaced()||!l.getFluidState(pos).isEmpty())continue;
   var item=new ItemStack(AddonItems.LM01.get());var result=item.useOn(new RobotContext(robot,p,item,hit));
   if(result.consumesAction()&&l.getBlockState(pos).is(AddonBlocks.LM01.get())){robot.removeItem(slot,1);robot.setChanged();return true;}
  }
  return false;
 }
 private Lm01Placement(){}
}
