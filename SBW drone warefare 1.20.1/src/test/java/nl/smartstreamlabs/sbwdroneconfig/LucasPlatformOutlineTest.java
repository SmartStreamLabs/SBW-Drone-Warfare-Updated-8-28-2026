package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LucasPlatformOutlineTest {
 @Test void everySelectableCellHasBoundedOutlineAndContainsItsCollisionEnvelope(){
  for(Direction d:new Direction[]{Direction.SOUTH,Direction.WEST,Direction.NORTH,Direction.EAST})for(int part=0;part<12;part++){
   var shape=LucasPlatformShapes.selection(d,part);int[] edges={0};
   shape.forAllEdges((a,b,c,x,y,z)->edges[0]++);
   assertTrue(edges[0]<=12,"Outline traversal must remain bounded: "+d+" part "+part);
   var collision=LucasPlatformShapes.collision(d,part);
   if(!collision.isEmpty()){
    assertFalse(shape.isEmpty());var a=shape.bounds();var b=collision.bounds();
    assertTrue(a.minX<=b.minX+1e-9 && a.minY<=b.minY+1e-9 && a.minZ<=b.minZ+1e-9);
    assertTrue(a.maxX>=b.maxX-1e-9 && a.maxY>=b.maxY-1e-9 && a.maxZ>=b.maxZ-1e-9);
   }
  }
 }
}