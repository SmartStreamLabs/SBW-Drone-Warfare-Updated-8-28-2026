package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class Dj01ConeTest {
 @Test void forwardRearAndRange(){assertTrue(Dj01Cone.contains(0,0,30,0,0,1,128,24));assertFalse(Dj01Cone.contains(0,0,-2,0,0,1,128,24));assertFalse(Dj01Cone.contains(0,0,128,0,0,1,128,24));assertTrue(Dj01Cone.contains(0,0,127.99,0,0,1,128,24));}
 @Test void fullAngleMeansHalfOnEitherSide(){double r=30,a=Math.toRadians(12);assertTrue(Dj01Cone.contains(r*Math.sin(a),0,r*Math.cos(a),0,0,1,128,24));assertTrue(Dj01Cone.contains(-r*Math.sin(a),0,r*Math.cos(a),0,0,1,128,24));assertFalse(Dj01Cone.contains(30*Math.sin(a+.01),0,30*Math.cos(a+.01),0,0,1,128,24));}
 @Test void arbitraryLookDirectionAndPitch(){assertTrue(Dj01Cone.contains(20,20,0,2,2,0,128,24));assertFalse(Dj01Cone.contains(20,0,20,2,2,0,128,24));assertTrue(Dj01Cone.contains(0,-20,0,0,-10,0,128,24));}
 @Test void degenerateAndInvalidInputs(){assertFalse(Dj01Cone.contains(0,0,1,0,0,0,128,24));assertFalse(Dj01Cone.contains(Double.NaN,0,1,0,0,1,128,24));assertFalse(Dj01Cone.contains(0,0,1,0,0,1,0,24));assertFalse(Dj01Cone.contains(0,0,1,0,0,1,128,Double.NaN));}
}
