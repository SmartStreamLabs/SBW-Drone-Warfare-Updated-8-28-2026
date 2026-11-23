package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import org.joml.Matrix4d;import org.joml.Vector3d;import static org.junit.jupiter.api.Assertions.*;
class Dc01LayoutTest {
 @Test void threeSmallOrOneHeavyLayouts(){
  for(int mask=0;mask<8;mask++)for(int slot=0;slot<3;slot++)for(int profile=1;profile<=3;profile++){
   if(Dc01Layout.fits(slot,profile,mask)){int result=mask|1<<slot;if(profile==3)assertEquals(0,mask,"heavy airframe reserves the entire empty deck");if(profile==3)assertEquals(0,slot,"heavy props need the whole deck");assertEquals(0,mask&(1<<slot),"occupied bay cannot accept a second entity");}
  }
  assertTrue(Dc01Layout.fits(2,1,3));assertTrue(Dc01Layout.fits(1,1,1));assertFalse(Dc01Layout.fits(0,3,2));assertTrue(Dc01Layout.fits(2,1,2));assertTrue(Dc01Layout.fits(1,2,4));assertFalse(Dc01Layout.fits(0,99,0));assertFalse(Dc01Layout.fits(-1,1,0));
 }
 @Test void landingFeetFollowDeckOnYawPitchAndRoll(){
  for(double yaw:new double[]{0,Math.PI/2,Math.PI,Math.PI*1.5}){
   Matrix4d frame=new Matrix4d().translate(103,67,-48).rotateY(yaw).rotateX(.2).rotateZ(-.1);
   for(int slot=0;slot<3;slot++)for(int profile=1;profile<=3;profile++){
    var mount=Dc01Layout.mount(frame,slot,profile);var feet=mount.transformPosition(new Vector3d(0,Dc01Layout.floor(profile),0));
    var deck=frame.transformPosition(new Vector3d(0,Dc01Layout.DECK,Dc01Layout.z(slot)));assertEquals(0,feet.distance(deck),1e-9);
   }
  }
 }
 @Test void pairedPropellerEnvelopesHaveClearance(){
  // Current F01 model's measured maximum swept half-depth is 0.405 blocks.
  assertTrue(Math.abs(Dc01Layout.z(1)-Dc01Layout.z(2))>2*.405+.2);
  // Central HB01 swept width fits the real deck; end antennas sit beyond its depth.
  assertTrue(Hb01VisualPose.MAX_X-Hb01VisualPose.MIN_X<2.95);
  assertTrue(Hb01VisualPose.MAX_Z<1.6);
 }
}
