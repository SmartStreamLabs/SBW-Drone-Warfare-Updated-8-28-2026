package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.core.*;import net.minecraft.world.phys.*;import net.minecraft.world.phys.shapes.*;
class LaunchClearanceTest {
 @Test void emptyGapInsideCompoundBoundsIsClear(){var shape=Shapes.or(Shapes.box(0,0,0,.2,1,1),Shapes.box(.8,0,0,1,1,1));assertFalse(LaunchClearance.intersects(shape,BlockPos.ZERO,new AABB(.3,.2,.3,.7,.8,.7)));}
 @Test void realObstacleAndTranslatedPositionsAreBlocked(){assertTrue(LaunchClearance.intersects(Shapes.block(),new BlockPos(12,4,-7),new AABB(12.2,4.2,-6.8,12.8,4.8,-6.2)));assertFalse(LaunchClearance.intersects(Shapes.block(),new BlockPos(12,4,-7),new AABB(0,0,0,1,1,1)));}
 @Test void rotatedCorridorKeepsWingsAcrossTravel(){for(var d:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){var b=LaunchClearance.lucas(Vec3.ZERO,new Vec3(d.getStepX()*5,1,d.getStepZ()*5),d);assertEquals(d.getAxis()==Direction.Axis.X?9.2:7,b.getXsize(),1e-8);assertEquals(d.getAxis()==Direction.Axis.X?7:9.2,b.getZsize(),1e-8);}}
}
