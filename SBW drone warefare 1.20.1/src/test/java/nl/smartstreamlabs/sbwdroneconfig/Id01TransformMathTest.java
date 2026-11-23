package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Id01TransformMathTest {
 @Test void uprightLensPointsUp(){assertArrayEquals(new double[]{0,1,0},Id01TransformMath.direction(0,0,1,0,0,90),1e-9);}
 @Test void horizontalLensPointsForward(){assertArrayEquals(new double[]{0,0,1},Id01TransformMath.direction(0,0,1,0,0,0),1e-9);}
 @Test void uprightAnchorMatchesR3(){assertArrayEquals(new double[]{.522/16,25.56/16,1.856/16},Id01TransformMath.direction(.522/16,-1.856/16,25.56/16,0,0,90),1e-9);}
 @Test void yawIsAppliedOnce(){assertArrayEquals(new double[]{-1,0,0},Id01TransformMath.direction(0,0,1,90,0,0),1e-9);}
 @Test void pitchAndBindTiltShareOneAxis(){assertArrayEquals(Id01TransformMath.direction(0,0,1,25,-15,0),Id01TransformMath.direction(0,0,1,25,30,45),1e-9);}
}
