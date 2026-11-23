package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Qd01PoseClockTest {
 @Test void trackedPoseContinuesWhenEntityTickDoesNot(){var p=new Qd01PoseClock();assertEquals(-.35f,p.sample(100,-.35f,1));assertEquals(0,p.sample(101,0,1),1e-6);assertEquals(0,p.sample(102,0,.25f),1e-6);}
 @Test void interpolatesOncePerWorldTickAndResetsAfterAbsence(){var p=new Qd01PoseClock();p.sample(100,0,1);assertEquals(-.15,p.sample(101,-.3f,.5f),1e-6);assertEquals(0,p.sample(101,-.3f,0),1e-6);assertEquals(-.3,p.sample(101,-.3f,1),1e-6);assertEquals(.06,p.sample(1000,.06f,.1f),1e-6);}
}
