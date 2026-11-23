package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.network.chat.Component;import net.minecraft.world.item.*;import net.minecraft.world.item.context.BlockPlaceContext;import net.minecraft.world.level.block.Block;import net.minecraft.world.level.block.state.BlockState;import java.util.*;
public final class Uf01Item extends BlockItem {
 public Uf01Item(Block b,Properties p){super(b,p);}
 public static boolean clear(BlockPlaceContext c){
  var l=c.getLevel();var root=c.getClickedPos();var f=c.getHorizontalDirection();var player=c.getPlayer();if(c.getClickedFace()!=Direction.UP||root.getY()+50>l.getMaxBuildHeight()||l.isOutsideBuildHeight(root))return false;
  for(int i=0;i<Uf01Layout.CELLS.length;i++){BlockPos p=Uf01Layout.cell(root,f,i);if(!empty(c,p)||!l.isUnobstructed(AddonBlocks.UF01.get().defaultBlockState().setValue(Uf01Block.FACING,f).setValue(Uf01Block.PART,i),p,player==null?net.minecraft.world.phys.shapes.CollisionContext.empty():net.minecraft.world.phys.shapes.CollisionContext.of(player)))return false;if(Uf01Layout.CELLS[i][1]==0&&!l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP))return false;}
  var bounds=Uf01Layout.box(root,f,-.1,33.4,-3.5,22,47.6,3.5);
  for(BlockPos p:BlockPos.betweenClosed((int)Math.floor(bounds.minX),(int)Math.floor(bounds.minY),(int)Math.floor(bounds.minZ),(int)Math.ceil(bounds.maxX)-1,(int)Math.ceil(bounds.maxY)-1,(int)Math.ceil(bounds.maxZ)-1))if(!empty(c,p))return false;
  return true;
 }
 private static boolean empty(BlockPlaceContext c,BlockPos p){var l=c.getLevel();var player=c.getPlayer();return !l.isOutsideBuildHeight(p)&&l.hasChunkAt(p)&&l.getWorldBorder().isWithinBounds(p)&&l.getBlockState(p).isAir()&&(player==null||l.mayInteract(player,p)&&player.mayUseItemAt(p,Direction.UP,c.getItemInHand()));}
 @Override protected boolean canPlace(BlockPlaceContext c,BlockState s){boolean ok=clear(c)&&super.canPlace(c,s);if(!ok&&c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.translatable("uf01.clearance"),true);return ok;}
 @Override protected boolean placeBlock(BlockPlaceContext c,BlockState state){
  var l=c.getLevel();var root=c.getClickedPos();var f=state.getValue(Uf01Block.FACING);List<BlockState> old=new ArrayList<>();Uf01Block.MUTATING.set(true);
  try{for(int i=0;i<Uf01Layout.CELLS.length;i++){BlockPos p=Uf01Layout.cell(root,f,i);old.add(l.getBlockState(p));if(!l.setBlock(p,state.setValue(Uf01Block.PART,i),3)){for(int j=old.size()-1;j>=0;j--)l.setBlock(Uf01Layout.cell(root,f,j),old.get(j),3);return false;}}return true;}
  finally{Uf01Block.MUTATING.set(false);}
 }
 @Override public void appendHoverText(ItemStack s,TooltipContext l,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("uf01.description"));lines.add(Component.translatable("uf01.controls"));}
}
