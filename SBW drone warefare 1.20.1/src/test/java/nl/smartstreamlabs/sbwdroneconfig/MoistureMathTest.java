package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
final class MoistureMathTest {
 @Test void briefRainCannotJumpToFailure(){assertEquals(.1,MoistureMath.step(0,.1,1),.0001);assertEquals(0,MoistureMath.stage(.1,25,50,80,90));}
 @Test void unloadedTimeDoesNotCatchUp(){assertEquals(.2,MoistureMath.step(0,.2,100000),.0001);}
 @Test void dryingIsGradualAndBounded(){assertEquals(99.95,MoistureMath.step(100,-.05,1),.0001);assertEquals(0,MoistureMath.step(.01,-.05,1));assertEquals(100,MoistureMath.step(99.99,10,1));}
 @Test void invalidStateAndBackwardTimeAreSafe(){assertEquals(0,MoistureMath.step(Double.NaN,0,1));assertEquals(35,MoistureMath.step(35,1,-5));}
 @Test void thresholdsStayOrdered(){assertEquals(0,MoistureMath.stage(30,80,20,10,5));assertEquals(4,MoistureMath.stage(90,25,50,80,90));}
 @Test void motorLossIsContinuousAndLimited(){assertEquals(0,MoistureMath.severity(79,80));assertEquals(.5,MoistureMath.severity(90,80));assertEquals(1,MoistureMath.severity(100,80));}
}
