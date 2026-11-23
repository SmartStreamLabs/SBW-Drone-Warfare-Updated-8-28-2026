package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class RemoteExitLatchTest {
 @Test void openingHeldClickDoesNotImmediatelyExit(){var l=new RemoteExitLatch();assertFalse(l.update(true));assertFalse(l.update(true));}
 @Test void subTickReleasePressReleaseIsDetected(){var l=new RemoteExitLatch();assertFalse(l.update(false));assertTrue(l.update(true));assertFalse(l.update(false));}
 @Test void heldAndDuplicateEventsProduceOneExit(){var l=new RemoteExitLatch();l.update(false);assertTrue(l.update(true));for(int i=0;i<100;i++)assertFalse(l.update(true));}
 @Test void reconnectRequiresNewRelease(){var l=new RemoteExitLatch();l.update(false);assertTrue(l.update(true));l.reset();assertFalse(l.update(true));l.update(false);assertTrue(l.update(true));}
}
