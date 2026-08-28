package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneRadarMathTest {
    @Test
    void beepIntervalsMatchDetectorDistanceBands() {
        assertEquals(40, DroneRadarMath.beepIntervalTicks(220.0D, 5, 40));
        assertEquals(25, DroneRadarMath.beepIntervalTicks(100.0D, 5, 40));
        assertEquals(12, DroneRadarMath.beepIntervalTicks(49.9D, 5, 40));
        assertEquals(5, DroneRadarMath.beepIntervalTicks(19.9D, 5, 40));
    }

    @Test
    void signalStrengthGetsStrongerWhenTargetIsCloser() {
        assertEquals(100, DroneRadarMath.signalStrengthPercent(0.0D, 256.0D));
        assertEquals(50, DroneRadarMath.signalStrengthPercent(128.0D, 256.0D));
        assertEquals(0, DroneRadarMath.signalStrengthPercent(256.0D, 256.0D));
    }

    @Test
    void blipsAreRelativeToPlayerHeading() {
        DroneRadarMath.Blip ahead = DroneRadarMath.blip(0.0D, 64.0D, 0.0D, 256.0D, 40);
        DroneRadarMath.Blip right = DroneRadarMath.blip(64.0D, 0.0D, 0.0D, 256.0D, 40);
        DroneRadarMath.Blip behind = DroneRadarMath.blip(0.0D, -64.0D, 0.0D, 256.0D, 40);

        assertTrue(ahead.y() < 0, "A drone in front should render toward the top of the radar.");
        assertTrue(right.x() > 0, "A drone to the right should render on the right side of the radar.");
        assertTrue(behind.y() > 0, "A drone behind should render toward the bottom of the radar.");
    }
}
