package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class DroneOsdMathTest {
    @Test void minecraftCardinals(){assertEquals(0,DroneOsdMath.heading(0,-1),1e-9);assertEquals(90,DroneOsdMath.heading(1,0),1e-9);assertEquals(180,DroneOsdMath.heading(0,1),1e-9);assertEquals(270,DroneOsdMath.heading(-1,0),1e-9);assertEquals("N",DroneOsdMath.cardinal(359.9));}
    @Test void compassCrossesNorthByTwoDegrees(){assertEquals(2,DroneOsdMath.difference(1,359),1e-9);assertEquals(-2,DroneOsdMath.difference(359,1),1e-9);assertEquals(0,DroneOsdMath.normalize(720),1e-9);}
    @Test void displacementUsesTicksNotFramesOrMouse(){assertEquals(10,DroneOsdMath.speed(3,4,10),1e-9);assertEquals(0,DroneOsdMath.speed(0,0,1),1e-9);assertTrue(Double.isNaN(DroneOsdMath.speed(0,0,0)));assertEquals(10,DroneOsdMath.speed(30,40,100),1e-9);}
    @Test void horizonMatchesPerspectiveElevation(){assertEquals(0,DroneOsdMath.horizonY(0,0,100),1e-8);assertEquals(-100,DroneOsdMath.horizonY(45,0,100),1e-8);assertEquals(0,DroneOsdMath.horizonY(30,-30,100),1e-8);assertTrue(DroneOsdMath.horizonY(-30,0,100)>0);}
    @Test void layoutFitsAllRequestedShapesAndGuiScales(){for(int[] resolution:new int[][]{{1280,720},{1280,800},{1024,768},{3440,1440}})for(int gui=1;gui<=4;gui++)for(double request:new double[]{.6,1,1.5}){int w=resolution[0]/gui,h=resolution[1]/gui;float s=DroneOsdMath.scale(w,h,request,false);assertTrue(w/s>=399.99);assertTrue(h/s>=249.99);assertTrue(s>0);}}
    @Test void smallMonitorStillHasACompleteLogicalCanvas(){for(int[] r:new int[][]{{73,41},{121,68},{179,100},{320,180}}){float s=DroneOsdMath.scale(r[0],r[1],1,true);assertTrue(r[0]/s>=199.99);assertTrue(r[1]/s>=129.99);}}
}
