package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class LucasPlatformLayoutTest {
 @Test void everyPartHasExactlyOneAnchorInEveryDirection(){
  for(Direction d:new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}){
   var root=new BlockPos(-31,80,47);var cells=new HashSet<BlockPos>();
   for(int p=0;p<LucasPlatformLayout.PARTS;p++){
    var pos=LucasPlatformLayout.cell(root,d,p);assertTrue(cells.add(pos));assertEquals(root,LucasPlatformLayout.anchor(pos,d,p));
    assertTrue(pos.getY()==80 || pos.getY()==81);
   }
   assertEquals(12,cells.size());
  }
 }
 @Test void mountAndRailDirectionRotateTogether(){
  var root=BlockPos.ZERO;
  for(Direction d:new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}){
   var center=LucasPlatformLayout.center(root,d);var mount=LucasPlatformLayout.mount(root,d);
   assertEquals(.5,center.subtract(net.minecraft.world.phys.Vec3.atBottomCenterOf(root)).length(),1e-9);
   assertEquals(LucasPlatformLayout.MOUNT_HEIGHT,mount.y,1e-9);
   assertEquals(1,LucasPlatformLayout.forward(d).length(),1e-9);
   assertEquals(0,LucasPlatformLayout.forward(d).dot(new net.minecraft.world.phys.Vec3(LucasPlatformLayout.right(d).getStepX(),0,LucasPlatformLayout.right(d).getStepZ())),1e-9);
  }
 }
 @Test void suppliedClipDefinesDurationAndOpposedClamps(){
  assertEquals(13,LucasPlatformClampAnimation.TICKS);
  assertEquals(Math.toRadians(32),LucasPlatformClampAnimation.LEFT_RADIANS,1e-6);
  assertEquals(-LucasPlatformClampAnimation.LEFT_RADIANS,LucasPlatformClampAnimation.RIGHT_RADIANS,1e-6);
 }
}
