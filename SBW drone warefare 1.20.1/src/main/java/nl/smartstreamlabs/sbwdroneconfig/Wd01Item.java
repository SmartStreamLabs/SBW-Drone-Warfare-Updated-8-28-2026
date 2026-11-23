package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.item.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.chat.Component;import java.util.List;

/** New item and the real SBW container share one validated water deployment transaction. */
public class Wd01Item extends Item {
 public Wd01Item(Properties p){super(p);}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){return deploy(level,p,hand,null);}
 @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("wd01.tooltip"));lines.add(Component.translatable("wd01.controls"));}
 public static CompoundTag container(ItemStack stack){var n=BlockItem.getBlockEntityData(stack);return n!=null&&"sbwdroneconfig:water_drone_wd01".equals(n.getString("EntityType"))?n:null;}
 public static InteractionResultHolder<ItemStack> deploy(Level level,Player p,InteractionHand hand,CompoundTag container){
  ItemStack stack=p.getItemInHand(hand);if(level.isClientSide)return InteractionResultHolder.success(stack);
  if(!(p instanceof ServerPlayer sp)||p.isSpectator()||!p.isAlive()||!p.mayBuild())return failed(p,stack,"wd01.denied");
  var hit=getPlayerPOVHitResult(level,p,ClipContext.Fluid.SOURCE_ONLY);if(hit.getType()!=HitResult.Type.BLOCK)return failed(p,stack,"wd01.water");
  var s=Wd01Water.surface(level,hit.getLocation());if(s==null||s.depth()<.75)return failed(p,stack,"wd01.water");
  var at=new Vec3(hit.getLocation().x,s.height(),hit.getLocation().z);var boat=AddonEntities.WD01.get().create(level);if(boat==null)return InteractionResultHolder.fail(stack);
  if(container!=null&&container.contains("Entity",10))boat.load(container.getCompound("Entity").copy());else boat.setEnergy(boat.getMaxEnergy());
  if(!boat.permitted(sp))return failed(p,stack,"wd01.denied");boat.releaseControl();boat.setPos(at);boat.setYRot(p.getYRot());boat.setXRot(0);boat.setZRot(0);boat.setPower(0);boat.setDeltaMovement(Vec3.ZERO);
  // Every footprint sample, including the bow/stern corners, must have pure, deep enough water.
  for(double x:new double[]{-.78,0,.78})for(double z:new double[]{-2.35,-1.2,0,1.2,2.35}){
   var point=Wd01Water.point(at,boat.getYRot(),x,z);var water=Wd01Water.surface(level,point);var pos=BlockPos.containing(point.add(0,-.05,0));
   if(water==null||water.depth()<.75||Math.abs(water.height()-at.y)>.12)return failed(p,stack,"wd01.water");
   if(!level.mayInteract(p,pos)||!p.mayUseItemAt(pos,hit.getDirection(),stack))return failed(p,stack,"wd01.denied");
  }
  var area=boat.getBoundingBoxForCulling();if(!level.hasChunksAt(BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX,area.maxY,area.maxZ))||!level.getWorldBorder().isWithinBounds(area))return failed(p,stack,"wd01.clearance");
  boat.updateOBB();var hull=boat.getOBBs();
  for(var shape:level.getBlockCollisions(boat,boat.getBoundingBox()))for(var box:shape.toAabbs())if(hull.stream().anyMatch(o->com.atsuishio.superbwarfare.tools.OBB.isColliding(o,box)))return failed(p,stack,"wd01.clearance");
  for(var other:level.getEntities(boat,boat.getBoundingBox(),e->e.isPickable()&&!e.isSpectator())){
   boolean overlap=other instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity vehicle?hull.stream().anyMatch(o->vehicle.getOBBs().stream().anyMatch(b->com.atsuishio.superbwarfare.tools.OBB.isColliding(o,b))):hull.stream().anyMatch(o->com.atsuishio.superbwarfare.tools.OBB.isColliding(o,other.getBoundingBox()));
   if(overlap)return failed(p,stack,"wd01.clearance");
  }
  boat.claimOwner(sp);if(!level.addFreshEntity(boat))return failed(p,stack,"wd01.clearance");
  if(!p.getAbilities().instabuild)stack.shrink(1);p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(stack.getItem()));return InteractionResultHolder.consume(stack);
 }
 private static InteractionResultHolder<ItemStack> failed(Player p,ItemStack s,String key){p.displayClientMessage(Component.translatable(key),true);return InteractionResultHolder.fail(s);}
}
