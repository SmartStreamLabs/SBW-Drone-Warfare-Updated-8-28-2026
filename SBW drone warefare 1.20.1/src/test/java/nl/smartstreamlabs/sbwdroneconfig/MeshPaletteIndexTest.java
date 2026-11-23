package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class MeshPaletteIndexTest {
 @Test void allIndicesRoundTripThroughNormalizedColorBytes(){for(int i=0;i<MeshPaletteIndex.MAX_PARTS;i++){float r=MeshPaletteIndex.low(i)/255f,g=MeshPaletteIndex.high(i)/255f;assertEquals(i,(int)(r*255+.5)+(int)(g*255+.5)*256);}}
 @Test void oldBoundaryAndRoverAreDistinct(){assertEquals(255,MeshPaletteIndex.low(255));assertEquals(0,MeshPaletteIndex.high(255));assertEquals(0,MeshPaletteIndex.low(256));assertEquals(1,MeshPaletteIndex.high(256));assertEquals(434,MeshPaletteIndex.low(434)+256*MeshPaletteIndex.high(434));}
 @Test void normalFlagsAreIndependentOfBothIndexBytes(){for(int f=0;f<8;f++){int bits=MeshPaletteIndex.flags((f&1)!=0,(f&2)!=0,(f&4)!=0);assertEquals(f,bits);for(int i:new int[]{0,255,256,434,4095}){assertEquals(i,MeshPaletteIndex.low(i)+MeshPaletteIndex.high(i)*256);assertEquals((f&1)!=0,(bits&1)!=0);assertEquals((f&2)!=0,(bits&2)!=0);assertEquals((f&4)!=0,(bits&4)!=0);}}}
 @Test void rejectsOutOfBudgetIndices(){assertThrows(IllegalArgumentException.class,()->MeshPaletteIndex.low(-1));assertThrows(IllegalArgumentException.class,()->MeshPaletteIndex.high(4096));}
}
