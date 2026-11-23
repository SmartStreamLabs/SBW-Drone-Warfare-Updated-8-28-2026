package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import org.joml.Matrix4d;import org.joml.Vector3f;import org.joml.Vector3d;import static org.junit.jupiter.api.Assertions.*;
class PatientPoseTest {
 @Test void facePointsUpAndHeadPointsForwardOnEveryVehicleHeading(){for(double yaw:new double[]{0,Math.PI/2,Math.PI,3*Math.PI/2}){var vehicle=new Matrix4d().rotateY(yaw).rotateX(.18).rotateZ(.09);var pose=PatientPose.orientation(vehicle);var head=pose.transform(new Vector3f(0,1,0));var face=pose.transform(new Vector3f(0,0,-1));var expectedHead=vehicle.transformDirection(new Vector3d(0,0,1));var expectedFace=vehicle.transformDirection(new Vector3d(0,1,0));assertEquals(1,head.x*expectedHead.x+head.y*expectedHead.y+head.z*expectedHead.z,1e-5);assertEquals(1,face.x*expectedFace.x+face.y*expectedFace.y+face.z*expectedFace.z,1e-5);}}
 @Test void lyingBoundsContainPatientHeadAndUseLowHeight(){var box=PatientPose.bounds(new Matrix4d());assertTrue(box.contains(0,1.34,.875+MedicalTrailerEntity.AXLE_Z));assertTrue(box.getYsize()<.7);assertTrue(box.getZsize()>1.8);}
 @Test void backOfHeadRestsOnSuppliedPillow(){double height=1.0875-.25*.9375+PatientPose.HEAD_LIFT_PIXELS*.9375/16;assertEquals(16.4/16,height,1e-6);}
}
