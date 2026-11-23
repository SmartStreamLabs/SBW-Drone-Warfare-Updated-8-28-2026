package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class InterferenceMathTest {
 @Test void onlyAuthoritativeProgressBeyondExistingThresholdCreatesDistortion(){assertEquals(0,InterferenceMath.strength(.1f,.15));assertEquals(0,InterferenceMath.strength(.15f,.15));assertEquals(1,InterferenceMath.strength(1,.15));assertTrue(InterferenceMath.strength(.8f,.15)>InterferenceMath.strength(.3f,.15));}
 @Test void attackAndRecoveryAreSmoothAndBounded(){float n=0;for(int i=0;i<30;i++){float old=n;n=InterferenceMath.smooth(n,1,.05);assertTrue(n>=old&&n<=1);}assertTrue(n>.99);for(int i=0;i<60;i++){float old=n;n=InterferenceMath.smooth(n,0,.05);assertTrue(n<=old&&n>=0);}assertTrue(n<.001);}
 @Test void subdivisionKeepsSmoothingIndependentOfRenderFrameRate(){float a=0,b=0;for(int i=0;i<20;i++)a=InterferenceMath.smooth(a,.7f,.05);for(int i=0;i<100;i++)b=InterferenceMath.smooth(b,.7f,.01);assertEquals(a,b,1e-6);}
 @Test void seedSeparatesFeedsWithoutUnboundedNoise(){assertNotEquals(InterferenceMath.event(123,10),InterferenceMath.event(321,10));for(int i=0;i<1000;i++){float n=InterferenceMath.event(71,i);assertTrue(n>=0&&n<1);assertEquals(n,InterferenceMath.event(71,i));}}
 @Test void malformedValuesDoNotReachShaderUniforms(){assertEquals(0,InterferenceMath.strength(Float.NaN,.15));assertEquals(0,InterferenceMath.clamp(Float.NaN));assertEquals(0,InterferenceMath.smooth(Float.NaN,0,.05));}
}
