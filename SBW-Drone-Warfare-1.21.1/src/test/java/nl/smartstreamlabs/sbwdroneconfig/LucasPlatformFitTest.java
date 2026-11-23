package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.Vec3;import net.minecraft.core.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class LucasPlatformFitTest {
 @Test void actualFitSceneVertexMatchesPublishedTransform(){
  // v0 from main_body in the supplied Fit Scene: (27.14688,13.917648,-16.045984).
  Vec3 actual=LucasPlatformLayout.fitPoint(new Vec3(.84834,-.016636,-.501437));
  assertEquals(27.14688/16,actual.x,1e-7);
  assertEquals(0.5479639319676362,actual.y,1e-7);assertEquals(-1.0294535908466749,actual.z,1e-7);
 }
 @Test void mountAndLongitudinalAxisFollowRailInEveryFacing(){
  for(Direction d:new Direction[]{Direction.SOUTH,Direction.WEST,Direction.NORTH,Direction.EAST}){
   var origin=LucasPlatformLayout.mount(BlockPos.ZERO,d);var tip=LucasPlatformLayout.center(BlockPos.ZERO,d).add(LucasPlatformLayout.orient(LucasPlatformLayout.fitPoint(new Vec3(0,0,1)),d));
   var axis=tip.subtract(origin);assertEquals(2,axis.length(),1e-9);
   assertEquals(Math.tan(Math.toRadians(18)),axis.y/axis.horizontalDistance(),1e-9);
   assertTrue(axis.dot(LucasPlatformLayout.forward(d))>0);
   assertEquals(LucasPlatformLayout.fitPoint(Vec3.ZERO).z,origin.subtract(LucasPlatformLayout.center(BlockPos.ZERO,d)).dot(LucasPlatformLayout.forward(d)),1e-9);
  }
 }
}