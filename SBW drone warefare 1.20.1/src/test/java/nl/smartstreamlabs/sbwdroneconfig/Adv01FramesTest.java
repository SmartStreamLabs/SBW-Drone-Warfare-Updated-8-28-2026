package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.joml.Matrix4d;
final class Adv01FramesTest {
 @Test void suppliedBindMuzzleUses35DegreesOnce(){var p=Adv01Frames.muzzle(Adv01Frames.gun(Adv01Frames.turret(new Matrix4d(),0),-35));assertEquals(0,p.x,1e-8);assertEquals(4.541750910482036,p.y,1e-8);assertEquals(1.7170874081172305,p.z,1e-8);}
 @Test void neutralMuzzleAndRaisedMuzzleAreDistinct(){var neutral=Adv01Frames.muzzle(Adv01Frames.gun(Adv01Frames.turret(new Matrix4d(),0),0));assertEquals(3.045,neutral.y,1e-8);assertEquals(2.23,neutral.z,1e-8);var raised=Adv01Frames.muzzle(Adv01Frames.gun(Adv01Frames.turret(new Matrix4d(),0),-70));assertTrue(raised.y>5.4);}
 @Test void cameraAndSeatsUseNativeUnitsOnce(){var b=new Matrix4d();var camera=Adv01Frames.sensor(Adv01Frames.turret(b,0),0).getTranslation(new org.joml.Vector3d());assertEquals(.15,camera.x,1e-8);assertEquals(3.365,camera.y,1e-8);assertEquals(-1.13,camera.z,1e-8);var left=Adv01Frames.seat(b,0);var right=Adv01Frames.seat(b,1);assertEquals(1.2,left.distance(right),1e-8);assertEquals(.315,left.y,1e-8);}
 @Test void sweptFuseIncludesSmallTargetsBetweenFastEndpoints(){double t=Adv01Frames.closestFraction(0,0,0,0,0,18,.1,.1,9);assertEquals(.5,t,1e-8);assertEquals(0,Adv01Frames.closestFraction(0,0,0,0,0,0,1,2,3),1e-8);assertEquals(1,Adv01Frames.closestFraction(0,0,0,0,0,18,0,0,100),1e-8);}
 @Test void trackPathIsClosedAndNeutralPoseHasNoOffset(){double[] p=new double[3];assertTrue(Adv01TrackPath.length()>10);for(int i=0;i<92;i++){Adv01TrackPath.pose(i,0,p);assertEquals(0,p[0],1e-7);assertEquals(0,p[1],1e-7);assertEquals(0,p[2],1e-7);Adv01TrackPath.pose(i,Adv01TrackPath.length(),p);assertEquals(0,p[0],1e-7);assertEquals(0,p[1],1e-7);}}
}
