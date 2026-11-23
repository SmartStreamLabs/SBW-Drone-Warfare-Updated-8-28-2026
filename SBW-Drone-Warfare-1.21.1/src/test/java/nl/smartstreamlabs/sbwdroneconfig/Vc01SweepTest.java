package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import net.minecraft.world.phys.*;
import org.joml.Matrix4d;
import static org.junit.jupiter.api.Assertions.*;

class Vc01SweepTest {
 private final NtGeometry.Triangle wall=new NtGeometry.Triangle(new Vec3(0,-2,-2),new Vec3(0,2,-2),new Vec3(0,0,2));
 @Test void fastVolumeHitsWithoutEitherEndpointInside(){var box=new AABB(-32.2,-.2,-.2,-31.8,.2,.2);double t=Vc01Sweep.hit(box,new Vec3(64,0,0),wall,new Matrix4d(),new Matrix4d());assertTrue(t>.49&&t<.51);}
 @Test void actualVolumeCanGrazeWhenCentreRayMisses(){var box=new AABB(-2,-.05,1.95,-1.6,.15,2.35);assertTrue(Double.isFinite(Vc01Sweep.hit(box,new Vec3(4,0,0),wall,new Matrix4d(),new Matrix4d())));}
 @Test void openingBeyondPanelStaysOpen(){assertTrue(Double.isNaN(Vc01Sweep.hit(new AABB(-2,4,4,-1.6,4.4,4.4),new Vec3(4,0,0),wall,new Matrix4d(),new Matrix4d())));}
 @Test void translatingCageHitsStationaryDrone(){var box=new AABB(-.2,-.2,-.2,.2,.2,.2);double t=Vc01Sweep.hit(box,Vec3.ZERO,wall,new Matrix4d().translation(-5,0,0),new Matrix4d().translation(5,0,0));assertTrue(t>.47&&t<.53);}
 @Test void rotatingPanelCatchesMidTickWithoutEndpointContact(){var panel=new NtGeometry.Triangle(new Vec3(2,0,-1),new Vec3(2,1,-1),new Vec3(2,0,1));var box=new AABB(-.1,.1,-2.1,.1,.3,-1.9);assertTrue(Double.isFinite(Vc01Sweep.hit(box,Vec3.ZERO,panel,new Matrix4d(),new Matrix4d().rotateY(Math.PI))));}
 @Test void narrowGapIsNotAnEnclosingBox(){var left=new NtGeometry.Triangle(new Vec3(-2,0,0),new Vec3(-.6,0,0),new Vec3(-2,2,0));var right=new NtGeometry.Triangle(new Vec3(.6,0,0),new Vec3(2,0,0),new Vec3(2,2,0));var box=new AABB(-.1,.3,-2,.1,.5,-1.8);for(var p:new NtGeometry.Triangle[]{left,right})assertTrue(Double.isNaN(Vc01Sweep.hit(box,new Vec3(0,0,4),p,new Matrix4d(),new Matrix4d())));}
}
