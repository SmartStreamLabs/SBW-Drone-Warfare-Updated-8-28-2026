package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ExplosionVfxProfileTest {
    @Test void strongerPayloadsProduceMoreSmoke() {
        float previousScale=0, previousDensity=0, previousVolume=0;
        int previousLife=0;
        for(float radius:new float[]{1.8F,5,8.5F,17,58}) {
            var p=ExplosionVfxProfile.of(radius,radius>=6?2:1);
            float smokeVolume=p.smokeDensity()*p.scale()*p.scale()*p.scale()*p.emissionTicks();
            assertTrue(p.scale()>previousScale);
            assertTrue(p.smokeDensity()>previousDensity,"more smoke lobes per emission");
            assertTrue(p.smokeLife()>=previousLife);
            assertTrue(smokeVolume>previousVolume);
            assertTrue(p.smokeLife()+60+p.emissionTicks()<=600,"entire heavy aftermath within 30 seconds");
            previousScale=p.scale();previousDensity=p.smokeDensity();previousLife=p.smokeLife();previousVolume=smokeVolume;
        }
    }
    @Test void worldSpaceDimensionsAndStrengthDetermineClass() {
        var small=ExplosionVfxProfile.of(1.8F,2);
        var middle=ExplosionVfxProfile.of(5,1);
        var heavy=ExplosionVfxProfile.of(58,1);
        assertEquals(5.7F,small.fireDiameter(),0.01);
        assertEquals(19.5F,middle.fireDiameter(),0.01);
        assertEquals(64,heavy.fireDiameter(),0.01);
        assertTrue(heavy.plumeHeight()>=100 && heavy.plumeHeight()<=112);
        assertTrue(heavy.heavy()); assertFalse(small.heavy());
        assertEquals(ExplosionVfxProfile.of(17,1),ExplosionVfxProfile.of(17,2));
    }
    @Test void extremeAndInvalidVisualInputsStayBounded() {
        for(float radius:new float[]{Float.NaN,Float.POSITIVE_INFINITY,-10,0,100000}) {
            var p=ExplosionVfxProfile.of(radius,2);
            assertTrue(Float.isFinite(p.scale()));assertTrue(p.scale()<=32F);
            assertTrue(p.smokeDensity()<=2.5F);assertTrue(p.smokeLife()<=420);
        }
    }
}
