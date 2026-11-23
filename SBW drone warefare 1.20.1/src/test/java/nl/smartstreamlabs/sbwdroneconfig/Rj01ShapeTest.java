package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.core.*;
class Rj01ShapeTest {
 @Test void legacyEmptyCellsHaveNoBox(){assertFalse(RadarJammerTowerPart.WEST_TWO.required());assertFalse(RadarJammerTowerPart.WEST_TWO_NORTH.required());}
 @Test void everyFacingUsesClippedShapesAndInvertiblePartPositions(){for(Direction d:Direction.Plane.HORIZONTAL){var master=new BlockPos(10,90,10);assertFalse(Rj01Shapes.cell(d,BlockPos.ZERO).isEmpty());int upper=0;for(var part:RadarJammerTowerPart.values()){var at=part.partPos(master,d);assertEquals(master,part.masterPos(at,d));var shape=Rj01Shapes.cell(d,at.subtract(master));assertEquals(part.required(),!shape.isEmpty());for(var box:shape.toAabbs()){assertTrue(box.minX>=-1e-8&&box.minY>=-1e-8&&box.minZ>=-1e-8);assertTrue(box.maxX<=1.00000001&&box.maxY<=1.00000001&&box.maxZ<=1.00000001);}if(part.required()&&at.getY()>90)upper++;}assertTrue(upper>0);}}
}
