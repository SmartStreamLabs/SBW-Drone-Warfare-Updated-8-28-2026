package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticSessionPersistenceTest {
    @Test
    void keepsFiberSessionAliveWhenControllerTemporarilyStopsViewing() {
        assertTrue(
                FiberOpticLinkSystem.shouldKeepSessionWhenControllerMissing(true, DroneLinkMode.FIBER_OPTIC),
                "An active fiber session should keep its server-side cable entities alive even if the player briefly leaves the monitor."
        );
    }

    @Test
    void doesNotKeepMissingControllerSessionsForWirelessOrInactiveDrones() {
        assertFalse(
                FiberOpticLinkSystem.shouldKeepSessionWhenControllerMissing(false, DroneLinkMode.FIBER_OPTIC),
                "Inactive fiber sessions should still clear normally when no controller is present."
        );
        assertFalse(
                FiberOpticLinkSystem.shouldKeepSessionWhenControllerMissing(true, DroneLinkMode.WIRELESS),
                "Wireless drones should not preserve a fiber session when no fiber module is installed."
        );
    }
}
