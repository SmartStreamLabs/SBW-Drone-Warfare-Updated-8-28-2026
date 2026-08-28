package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableOwnerProtectionTest {
    @Test
    void ownerOrControllerCannotBreakTheirOwnFiberCable() {
        UUID ownerUuid = UUID.randomUUID();
        UUID controllerUuid = UUID.randomUUID();

        assertTrue(
                FiberOpticLinkSystem.isProtectedCableBreaker(ownerUuid, ownerUuid, controllerUuid),
                "The stored drone owner should never be able to sever their own fiber cable."
        );
        assertTrue(
                FiberOpticLinkSystem.isProtectedCableBreaker(controllerUuid, ownerUuid, controllerUuid),
                "The active drone controller should also be protected from cutting the live fiber link."
        );
    }

    @Test
    void storedFiberSessionControllerStaysProtectedAfterLeavingTheMonitor() {
        UUID previousFiberControllerUuid = UUID.randomUUID();

        assertTrue(
                FiberOpticLinkSystem.isProtectedCableBreaker(previousFiberControllerUuid, null, null, previousFiberControllerUuid),
                "The stored fiber session controller UUID should still protect the cable after SBW clears the live controller."
        );
    }

    @Test
    void droneEntityCannotSeverItsOwnFiberCable() {
        UUID droneUuid = UUID.randomUUID();

        assertTrue(
                FiberOpticLinkSystem.isProtectedCableBreaker(droneUuid, null, null, null, droneUuid),
                "The tracked drone entity should never be able to damage and sever its own fiber cable while flying through it."
        );
    }

    @Test
    void otherPlayersCanStillCutFiberCable() {
        UUID attackerUuid = UUID.randomUUID();
        UUID ownerUuid = UUID.randomUUID();
        UUID controllerUuid = UUID.randomUUID();

        assertFalse(
                FiberOpticLinkSystem.isProtectedCableBreaker(attackerUuid, ownerUuid, controllerUuid),
                "Non-owners and non-controllers should still be able to destroy the fiber cable."
        );
        assertFalse(
                FiberOpticLinkSystem.isProtectedCableBreaker(null, ownerUuid, controllerUuid),
                "Missing attacker data should not incorrectly protect hostile cable damage."
        );
    }
}
