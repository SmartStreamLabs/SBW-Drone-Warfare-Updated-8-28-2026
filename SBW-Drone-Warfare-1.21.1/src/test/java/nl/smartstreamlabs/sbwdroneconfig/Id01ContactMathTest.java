package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Id01ContactMathTest {
 private final double[] lo={-.5,-.5,-.5},hi={.5,.5,.5};
 @Test void fastRelativeCrossingHitsDespiteSeparatedEndpoints(){assertTrue(Id01ContactMath.segment(new double[]{-18,0,0},new double[]{20,0,0},lo,hi));}
 @Test void nearbyParallelFlightDoesNotDetonate(){assertFalse(Id01ContactMath.segment(new double[]{-18,.8,0},new double[]{20,.8,0},lo,hi));}
 @Test void stationaryOverlapHits(){assertTrue(Id01ContactMath.segment(new double[]{0,0,0},new double[]{0,0,0},lo,hi));}
 @Test void stationarySeparatedDoesNotHit(){assertFalse(Id01ContactMath.segment(new double[]{0,0,2},new double[]{0,0,2},lo,hi));}
 @Test void contactOutsideSampledIntervalDoesNotHit(){assertFalse(Id01ContactMath.segment(new double[]{-18,0,0},new double[]{-2,0,0},lo,hi));}
 @Test void corruptCoordinatesNeverArmContact(){assertFalse(Id01ContactMath.segment(new double[]{Double.NaN,0,0},new double[]{0,0,0},lo,hi));}
}
