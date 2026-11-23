package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class RubbleSupportGraphTest {
 private static final RubbleSupportGraph.Cell AIR=new RubbleSupportGraph.Cell(false,false,1),STONE=new RubbleSupportGraph.Cell(true,false,3),WOOD=new RubbleSupportGraph.Cell(true,false,6),ANCHOR=new RubbleSupportGraph.Cell(false,true,1);
 private static RubbleSupportGraph.Pos p(int x,int y){return new RubbleSupportGraph.Pos(x,y,0);}
 private static RubbleSupportGraph.Result check(Map<RubbleSupportGraph.Pos,RubbleSupportGraph.Cell> map,RubbleSupportGraph.Pos start){return RubbleSupportGraph.check(v->map.getOrDefault(v,AIR),start,p(0,0),12,new RubbleSupportGraph.Budget(4096));}
 @Test void beamCanCarryAHorizontallyUnsupportedOverhang(){var m=new HashMap<RubbleSupportGraph.Pos,RubbleSupportGraph.Cell>();for(int x=0;x<=6;x++)m.put(p(x,2),WOOD);m.put(p(0,1),WOOD);m.put(p(0,0),ANCHOR);assertEquals(RubbleSupportGraph.Result.SUPPORTED,check(m,p(6,2)));}
 @Test void cuttingTheOnlyColumnActuallyRemovesTheLoadPath(){var m=new HashMap<RubbleSupportGraph.Pos,RubbleSupportGraph.Cell>();for(int x=0;x<=4;x++)m.put(p(x,2),WOOD);m.put(p(0,1),WOOD);m.put(p(0,0),ANCHOR);assertEquals(RubbleSupportGraph.Result.SUPPORTED,check(m,p(4,2)));m.remove(p(0,1));assertEquals(RubbleSupportGraph.Result.UNSUPPORTED,check(m,p(4,2)));}
 @Test void stoneDoesNotCarryAnArbitrarilyLongBeam(){var m=new HashMap<RubbleSupportGraph.Pos,RubbleSupportGraph.Cell>();for(int x=0;x<=7;x++)m.put(p(x,2),STONE);m.put(p(0,1),STONE);m.put(p(0,0),ANCHOR);assertEquals(RubbleSupportGraph.Result.UNSUPPORTED,check(m,p(7,2)));}
 @Test void unknownChunkIsNotEvidenceOfCollapse(){assertEquals(RubbleSupportGraph.Result.UNKNOWN,RubbleSupportGraph.check(v->v.equals(p(0,0))?STONE:null,p(0,0),p(0,0),12,new RubbleSupportGraph.Budget(100)));}
 @Test void exhaustedWorkBudgetDoesNotDestroyTheStructure(){var b=new RubbleSupportGraph.Budget(1);assertEquals(RubbleSupportGraph.Result.UNKNOWN,RubbleSupportGraph.check(v->STONE,p(0,0),p(0,0),12,b));assertTrue(b.left<=0);}
 @Test void connectionBeyondTheLocalBoundaryIsConservativeSupport(){var m=new HashMap<RubbleSupportGraph.Pos,RubbleSupportGraph.Cell>();m.put(p(12,1),STONE);assertEquals(RubbleSupportGraph.Result.SUPPORTED,check(m,p(12,1)));}
 @Test void floatingBeforeAndAfterDoesNotMeetLostSupportCondition(){var m=new HashMap<RubbleSupportGraph.Pos,RubbleSupportGraph.Cell>();m.put(p(0,4),WOOD);assertEquals(RubbleSupportGraph.Result.UNSUPPORTED,check(m,p(0,4)));}
}
