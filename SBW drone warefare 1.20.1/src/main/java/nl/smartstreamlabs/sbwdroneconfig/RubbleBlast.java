package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Environmental pressure only. No entity damage, native explosions or unbounded ray casts. */
public final class RubbleBlast {
    public record Offset(int x,int y,int z,int distance){}
    public static final List<Offset> OFFSETS;
    static {var list=new ArrayList<Offset>();for(int x=-16;x<=16;x++)for(int y=-16;y<=16;y++)for(int z=-16;z<=16;z++){int d=x*x+y*y+z*z;if(d<=256)list.add(new Offset(x,y,z,d));}list.sort(Comparator.comparingInt(Offset::distance).thenComparingInt(o->o.x*73428767^o.y*912931^o.z*438289));OFFSETS=List.copyOf(list);}
    public static boolean exposed(ServerLevel l,RubbleTasks.Job job,BlockPos target,BlockState state,RubbleSupport.Budget budget){
        Vec3 end=Vec3.atCenterOf(target),delta=end.subtract(job.origin);double distance=delta.length();boolean glass=RubbleMaterials.kind(state,job.securitycraft)==RubbleMaterials.Kind.GLASS;double range=glass?Math.min(RubbleConfig.RADIUS.get(),Math.ceil(job.core*RubbleConfig.GLASS.get())):job.core;
        if(distance>range+.1)return false;double energy=job.core-distance*(glass?.35:.55);var visited=new HashSet<BlockPos>();int steps=Math.min(64,Math.max(1,(int)Math.ceil(distance*4)));
        for(int i=0;i<steps;i++){BlockPos p=BlockPos.containing(job.origin.add(delta.scale(i/(double)steps)));if(p.equals(target)||!visited.add(p))continue;if(budget.left--<=0||!l.hasChunkAt(p))return false;var s=job.before.getOrDefault(p,l.getBlockState(p));if(s.isAir()||s.getCollisionShape(l,p).isEmpty())continue;energy-=.3+SecurityCraftBlastCompat.resistance(s,job.securitycraft)*.12;if(energy<=0)return false;}
        return energy>=Math.max(.02,SecurityCraftBlastCompat.resistance(state,job.securitycraft)*.12);
    }
    private RubbleBlast(){}
}
