package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneSoundOcclusionTest {
    @Test
    void leavesOpenAirUnchangedAndMufflesSolidTerrainExponentially() {
        assertEquals(1.0D, DroneSoundOcclusion.multiplierForSamples(0, 0), 0.0001D);
        assertTrue(DroneSoundOcclusion.multiplierForSamples(1, 0) < 1.0D);
        assertTrue(DroneSoundOcclusion.multiplierForSamples(8, 0) < 0.05D);
        assertEquals(0.01D, DroneSoundOcclusion.multiplierForSamples(100, 0), 0.0001D);
    }
}
