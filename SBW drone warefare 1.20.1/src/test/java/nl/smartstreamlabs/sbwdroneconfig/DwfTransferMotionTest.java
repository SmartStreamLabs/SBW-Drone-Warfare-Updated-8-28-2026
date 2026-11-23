package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DwfTransferMotionTest {
 @Test void buildsAndInspectsAtStation(){for(double p:new double[]{0,.2,.5,.65,.85})assertEquals(0,DwfVisualState.transferX(p));}
 @Test void travelsRightOnlyDuringTransfer(){assertEquals(1.275,DwfVisualState.transferX(.925),.0001);assertEquals(2.55,DwfVisualState.transferX(1),.0001);float previous=0;for(int i=850;i<=1000;i++){float x=DwfVisualState.transferX(i/1000d);assertTrue(x>=previous);previous=x;}}
 @Test void clampsEndpoints(){assertEquals(0,DwfVisualState.transferX(-1));assertEquals(2.55,DwfVisualState.transferX(2),.0001);}
 @Test void pausedSnapshotDoesNotMove(){var s=new DwfVisualState.Snapshot(1,0,925,1000,DwfVisualState.Status.PAUSED,0,0);assertEquals(DwfVisualState.transferX(s.fraction(1)),DwfVisualState.transferX(s.fraction(2000)));}
}
