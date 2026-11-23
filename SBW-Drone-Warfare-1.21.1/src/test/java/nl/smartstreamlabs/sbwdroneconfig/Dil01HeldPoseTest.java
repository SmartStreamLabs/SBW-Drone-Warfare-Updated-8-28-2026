package nl.smartstreamlabs.sbwdroneconfig;
import java.lang.Math;import org.junit.jupiter.api.Test;import org.joml.*;import static org.junit.jupiter.api.Assertions.*;
class Dil01HeldPoseTest {
 @Test void authoredScaleAndGripSpacingSurviveViewRotation(){for(float yaw:new float[]{0,1.2f,-2.4f})for(float pitch:new float[]{-.8f,0,.7f}){var p=Dil01HeldPose.solve(yaw,pitch,true,true,new Vector3f(-5,2,0),new Vector3f(5,2,0),0);assertEquals(Math.sqrt(.04*.04+.52*.52)/Dil01HeldPose.PLAYER_SCALE,p.main().distance(p.support())/16d,1e-5);var a=p.item().transformPosition(new Vector3f(0,.16f,-.72f));var b=p.item().transformPosition(new Vector3f(0,.16f,.28f));assertEquals(1/Dil01HeldPose.PLAYER_SCALE,a.distance(b),1e-5);}}
 @Test void leftHandMirrorsRightWithoutChangingMuzzleHeightOrDepth(){var a=Dil01HeldPose.solve(0,0,true,true,new Vector3f(-5,2,0),new Vector3f(5,2,0),0).item().transformPosition(new Vector3f(0,.16f,-.72f));var b=Dil01HeldPose.solve(0,0,false,true,new Vector3f(5,2,0),new Vector3f(-5,2,0),0).item().transformPosition(new Vector3f(0,.16f,-.72f));assertEquals(-a.x,b.x,1e-5);assertEquals(a.y,b.y,1e-5);assertEquals(a.z,b.z,1e-5);}
}
