package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DwfVisualTest {
 @Test void onlyActiveProductionExtrapolatesProgress(){
  var active=new DwfVisualState.Snapshot(1,100,40,200,DwfVisualState.Status.ACTIVE,0,0);
  var paused=new DwfVisualState.Snapshot(2,100,40,200,DwfVisualState.Status.PAUSED,0,0);
  assertEquals(.4,active.fraction(140),1e-6);assertEquals(.2,paused.fraction(10000),1e-6);assertEquals(1,active.fraction(10000),1e-6);
  assertTrue(active.motor());assertFalse(paused.motor());
 }
 @Test void amberBlinksTwicePerSecondAndRedSlowly(){
  assertEquals(1,DwfVisualState.lamp(DwfVisualState.Status.ACTIVE,0).brightness());
  assertEquals(.12f,DwfVisualState.lamp(DwfVisualState.Status.ACTIVE,5).brightness());
  assertEquals(1,DwfVisualState.lamp(DwfVisualState.Status.ACTIVE,10).brightness());
  assertEquals(.12f,DwfVisualState.lamp(DwfVisualState.Status.ACTIVE,15).brightness());
  assertEquals(1,DwfVisualState.lamp(DwfVisualState.Status.ERROR,19).brightness());assertEquals(.12f,DwfVisualState.lamp(DwfVisualState.Status.ERROR,20).brightness());assertEquals(-1,DwfVisualState.lamp(DwfVisualState.Status.OFF,0).band());
 }
 @Test void lidRemainsOpenForOtherViewerAndReversesWithoutJump(){
  var lid=new DwfVisualState.Lid();lid.viewers(2,0);assertEquals(.5,lid.value(7),1e-6);lid.viewers(1,7);assertEquals(1,lid.value(14),1e-6);lid.viewers(0,14);assertEquals(1,lid.value(14),1e-6);assertEquals(0,lid.value(28),1e-6);
  lid.viewers(1,30);double half=lid.value(37);lid.viewers(0,37);assertEquals(half,lid.value(37),1e-6);assertEquals(0,lid.value(51),1e-6);
 }
}
