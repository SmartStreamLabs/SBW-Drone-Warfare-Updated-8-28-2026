package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Hx2KinematicsTest {
 @Test void releaseMatchesLastCarriagePose(){assertEquals(0,Hx2Kinematics.carriage(0),1e-12);assertEquals(0,Hx2Kinematics.carriage(8),1e-12);assertEquals(-.5,Hx2Kinematics.carriage(20),1e-12);assertEquals(0,Hx2Kinematics.carriage(40),1e-12);}
 @Test void outwardCarriageNeverReversesBeforeDetach(){double previous=0;for(int i=0;i<=240;i++){double value=Hx2Kinematics.carriage(i/12.);assertTrue(value<=previous+1e-10);previous=value;}}
 @Test void carriageReturnsWithoutOvershoot(){double previous=-.5;for(int i=240;i<=480;i++){double value=Hx2Kinematics.carriage(i/12.);assertTrue(value>=previous-1e-10&&value<=0);previous=value;}}
 @Test void accelerationDoesNotReverseInstantly(){double[] next=Hx2Kinematics.accelerate(new double[]{0,0,1.8},new double[]{0,0,-1.8},.065);assertTrue(next[2]>1.7);}
 @Test void vectorAccelerationIsBoundedDuringTurn(){double[] next=Hx2Kinematics.accelerate(new double[]{0,0,1},new double[]{1,1,0},.065);double x=next[0],y=next[1],z=next[2]-1;assertEquals(.065,Math.sqrt(x*x+y*y+z*z),1e-10);}
 @Test void accelerationStopsAtTarget(){assertArrayEquals(new double[]{.01,.02,.03},Hx2Kinematics.accelerate(new double[]{0,0,0},new double[]{.01,.02,.03},.065),1e-10);}
 @Test void zeroVectorRemainsFinite(){assertArrayEquals(new double[]{0,0,0},Hx2Kinematics.accelerate(new double[]{0,0,0},new double[]{0,0,0},.065));}
}
