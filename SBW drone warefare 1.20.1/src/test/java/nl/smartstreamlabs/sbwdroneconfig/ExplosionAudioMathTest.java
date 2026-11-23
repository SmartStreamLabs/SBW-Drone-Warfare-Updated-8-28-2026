package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ExplosionAudioMathTest {
 @Test void cameraMovementChangesWaveArrivalNotDetonationTime(){
  assertEquals(130,ExplosionAudioMath.arrival(100,500,20.0/343));
  assertEquals(101,ExplosionAudioMath.arrival(100,10,20.0/343));
  assertEquals(100,ExplosionAudioMath.arrival(100,500,0));
 }
 @Test void physicalPropagationAndToggle(){
  assertEquals(500.0/343,ExplosionAudioMath.delay(500,343,true),1e-9);
  assertEquals(0,ExplosionAudioMath.delay(500,343,false));
  assertEquals(5,ExplosionAudioMath.delay(500,100,true));
 }
 @Test void audibleFarFieldRetainsDistanceFalloffAndHardRangeLimit(){
  assertTrue(ExplosionAudioMath.gain(500,1024)>.35,"a distant blast must retain useful gain at 500 blocks");
  assertTrue(ExplosionAudioMath.gain(800,1024)>.15,"the far field must not become inaudible before the configured edge");
  assertTrue(ExplosionAudioMath.gain(100,1024)>ExplosionAudioMath.gain(500,1024));
  assertEquals(1,ExplosionAudioMath.gain(0,1024),1e-9);
  assertEquals(0,ExplosionAudioMath.gain(2048,1024),1e-9);
 }
 @Test void fadesToSilenceWithoutLayerDiscontinuities(){
  double previous=1;
  for(int d=0;d<=1024;d++) {double g=ExplosionAudioMath.gain(d,1024);assertTrue(g>=0 && g<=previous);previous=g;}
  assertEquals(0,ExplosionAudioMath.gain(1024,1024));
  for(double d:new double[]{45,160,420}) assertEquals(ExplosionAudioMath.layerPosition(d-0.001),ExplosionAudioMath.layerPosition(d+0.001),0.001);
 }
}
