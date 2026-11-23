package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Dil01LeadTest {
 @Test void stationaryFortyBlocksAtFourPerTick(){assertEquals(10,Dil01Lead.time(0,0,40,0,0,0,4),1e-8);}
 @Test void targetClosingAtOneBlockPerTick(){assertEquals(8,Dil01Lead.time(0,0,40,0,0,-1,4),1e-8);}
 @Test void verticalMotionReceivesFullThreeDimensionalLead(){double t=Dil01Lead.time(0,0,40,0,2,0,4);assertEquals(Math.sqrt(1600d/12),t,1e-8);assertEquals(4*t,Math.sqrt(40*40+4*t*t),1e-8);}
 @Test void longDistancePredictionIsBounded(){assertEquals(20,Dil01Lead.time(0,0,10000,2,2,2,4));}
 @Test void equalSpeedAndZeroSeparationStayFinite(){assertTrue(Double.isFinite(Dil01Lead.time(0,0,40,0,0,4,4)));assertEquals(0,Dil01Lead.time(0,0,0,0,0,0,0));}
}
