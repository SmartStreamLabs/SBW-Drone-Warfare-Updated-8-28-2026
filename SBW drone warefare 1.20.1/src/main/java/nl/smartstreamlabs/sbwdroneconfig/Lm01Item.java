package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;

public final class Lm01Item extends TooltipBlockItem {
 public Lm01Item(){super(AddonBlocks.LM01.get(),new Properties(),"tooltip.sbwdroneconfig.lm01","tooltip.sbwdroneconfig.lm01_recover");}
 @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c){return c instanceof Lm01Placement.RobotContext robot?place(robot):super.useOn(c);}
 @Override public InteractionResult place(BlockPlaceContext c){
  var p=c.getPlayer();var target=c.getClickedPos();
  if(p==null||!p.isAlive()||p.isSpectator()||!p.getAbilities().mayBuild||!p.mayUseItemAt(target,c.getClickedFace(),c.getItemInHand())||!c.getLevel().getWorldBorder().isWithinBounds(target)||c.getLevel().isOutsideBuildHeight(target)||!c.getLevel().getFluidState(target).isEmpty())return InteractionResult.FAIL;
  if(!(c instanceof Lm01Placement.RobotContext)&&p.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(target))>36)return InteractionResult.FAIL;
  if(c.getLevel()instanceof net.minecraft.server.level.ServerLevel l&&!l.mayInteract(p,target))return InteractionResult.FAIL;
  return super.place(c);
 }
}
