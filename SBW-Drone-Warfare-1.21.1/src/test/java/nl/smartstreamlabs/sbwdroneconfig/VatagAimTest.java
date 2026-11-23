package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import org.joml.*;import static org.junit.jupiter.api.Assertions.*;
class VatagAimTest {
 @Test void worldMouseRightNeedsNegativeNativeYaw(){var a=VatagAim.local(new Matrix4d(),30,0);assertEquals(-30,a.x,.0001);assertEquals(0,a.y,.0001);}
 @Test void directionSurvivesHullYawPitchRollAndTranslation(){for(int yaw:new int[]{-179,-90,0,90,179})for(int pitch:new int[]{-20,0,20})for(int roll:new int[]{-15,0,15})for(int turret:new int[]{-170,-45,45,170}){
  var hull=new Matrix4d().translate(244,200,120).rotateY(java.lang.Math.toRadians(-yaw)).rotateX(java.lang.Math.toRadians(pitch)).rotateZ(java.lang.Math.toRadians(roll));var desired=new Matrix4d(hull).rotateY(java.lang.Math.toRadians(turret)).rotateX(java.lang.Math.toRadians(-10)).transformDirection(new Vector3d(0,0,1)).normalize();float worldYaw=(float)java.lang.Math.toDegrees(-java.lang.Math.atan2(desired.x,desired.z)),worldPitch=(float)java.lang.Math.toDegrees(-java.lang.Math.asin(desired.y));var local=VatagAim.local(hull,worldYaw,worldPitch);var actual=new Matrix4d(hull).rotateY(java.lang.Math.toRadians(local.x)).rotateX(java.lang.Math.toRadians(local.y)).transformDirection(new Vector3d(0,0,1)).normalize();assertEquals(0,actual.distance(desired),.000001);assertEquals(-10,local.y,.0001);
 }}
 @Test void cameraUpFollowsTurretAcrossPitchedHull(){assertEquals(20,VatagAim.roll(new Matrix4d().rotateX(java.lang.Math.toRadians(20)).rotateY(java.lang.Math.PI/2)),.0001);assertEquals(-15,VatagAim.roll(new Matrix4d().rotateZ(java.lang.Math.toRadians(-15))),.0001);assertEquals(0,VatagAim.roll(new Matrix4d().rotateX(java.lang.Math.toRadians(20))),.0001);}
}
