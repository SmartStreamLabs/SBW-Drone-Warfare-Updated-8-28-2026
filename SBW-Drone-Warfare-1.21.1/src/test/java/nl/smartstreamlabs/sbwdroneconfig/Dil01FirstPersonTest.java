package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import org.joml.Vector3f;
class Dil01FirstPersonTest{
 @Test void hipAndFullZoomStayInFrontOfCamera(){for(boolean aim:new boolean[]{false,true})for(boolean crouch:new boolean[]{false,true})for(boolean right:new boolean[]{false,true}){var p=Dil01HeldPose.firstPerson(right,crouch,crouch?1.27f:1.62f,aim).transformPosition(new Vector3f());assertTrue(p.z<-.4f);assertTrue(p.y<-.3f&&p.y>-.6f);assertTrue(Float.isFinite(p.x));}}
 @Test void handednessMirrorsOnlyTheLateralAxis(){for(boolean aim:new boolean[]{false,true}){var r=Dil01HeldPose.firstPerson(true,false,1.62f,aim).transformPosition(new Vector3f(.1f,.2f,-.3f));var l=Dil01HeldPose.firstPerson(false,false,1.62f,aim).transformPosition(new Vector3f(.1f,.2f,-.3f));assertEquals(-r.x,l.x,1e-6);assertEquals(r.y,l.y,1e-6);assertEquals(r.z,l.z,1e-6);}}
 @Test void standingRestPoseKeepsAuthoredShoulderHeightAndNativeScale(){var m=Dil01HeldPose.firstPerson(true,false,1.62f,true);assertEquals(1,m.determinant3x3(),1e-6);var p=m.transformPosition(new Vector3f());assertEquals(.129,p.x,1e-6);assertEquals(-.4503125,p.y,1e-5);assertEquals(-.432,p.z,1e-6);}
}
