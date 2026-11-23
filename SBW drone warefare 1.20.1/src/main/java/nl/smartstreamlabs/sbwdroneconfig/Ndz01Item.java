package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Validate both cells before vanilla BlockItem changes either cell or consumes the stack. */
public final class Ndz01Item extends DoubleHighBlockItem {
    public Ndz01Item(Block block,Properties p){super(block,p);}
    @Override protected boolean canPlace(BlockPlaceContext c,BlockState state){
        var level=c.getLevel();var root=c.getClickedPos();var player=c.getPlayer();
        if(c.getClickedFace()!=Direction.UP)return false;
        for(BlockPos p:new BlockPos[]{root,root.above()}){
            if(level.isOutsideBuildHeight(p)||!level.getWorldBorder().isWithinBounds(p)||!level.hasChunkAt(p)||!level.getBlockState(p).canBeReplaced(c)||!level.getFluidState(p).isEmpty())return false;
            if(player!=null&&(!level.mayInteract(player,p)||!player.mayUseItemAt(p,Direction.UP,c.getItemInHand())))return false;
        }
        var upper=state.setValue(Ndz01Block.HALF,DoubleBlockHalf.UPPER);
        return level.isUnobstructed(upper,root.above(),player==null?CollisionContext.empty():CollisionContext.of(player))&&super.canPlace(c,state);
    }
}
