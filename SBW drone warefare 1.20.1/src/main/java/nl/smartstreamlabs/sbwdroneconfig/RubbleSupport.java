package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Bounded weighted support paths. Unknown boundaries retain structures; they never prove failure. */
public final class RubbleSupport {
    public enum Result { SUPPORTED, UNSUPPORTED, UNKNOWN }
    public interface World { BlockState at(BlockPos p); boolean loaded(BlockPos p); }
    public static final class Budget {public int left; public Budget(int n){left=n;}}
    public static Result check(World world,BlockPos start,BlockPos centre,int radius,Budget budget){return check(world,start,centre,radius,budget,false);}
    public static Result check(World world,BlockPos start,BlockPos centre,int radius,Budget budget,boolean reinforced){
        var b=new RubbleSupportGraph.Budget(budget.left);
        var result=RubbleSupportGraph.check(pos->{var p=new BlockPos(pos.x(),pos.y(),pos.z());if(!world.loaded(p))return null;var state=world.at(p);boolean member=RubbleMaterials.eligible(state,reinforced);boolean anchor=!member&&!state.isAir()&&state.getFluidState().isEmpty()&&!state.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,p).isEmpty();return new RubbleSupportGraph.Cell(member,anchor,member?RubbleMaterials.span(state,reinforced):1);},new RubbleSupportGraph.Pos(start.getX(),start.getY(),start.getZ()),new RubbleSupportGraph.Pos(centre.getX(),centre.getY(),centre.getZ()),radius,b);
        budget.left=b.left;return Result.valueOf(result.name());
    }
    private RubbleSupport(){}
}
