package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticAntiDroneNetConfigTest {
    @Test
    void antiDroneNetPolicyOnlyCutsFiberWhenEnabled() {
        assertTrue(
                FiberOpticLinkSystem.shouldAntiDroneNetCutFiber(DroneLinkMode.FIBER_OPTIC, true),
                "Fiber optic mode should be cut when the anti-drone-net fiber option is enabled."
        );
        assertFalse(
                FiberOpticLinkSystem.shouldAntiDroneNetCutFiber(DroneLinkMode.WIRELESS, true),
                "Wireless drones should keep the old anti-drone-net behavior path."
        );
        assertFalse(
                FiberOpticLinkSystem.shouldAntiDroneNetCutFiber(DroneLinkMode.FIBER_OPTIC, false),
                "Disabling the anti-drone-net fiber option should preserve the old disconnect path."
        );
    }
}
