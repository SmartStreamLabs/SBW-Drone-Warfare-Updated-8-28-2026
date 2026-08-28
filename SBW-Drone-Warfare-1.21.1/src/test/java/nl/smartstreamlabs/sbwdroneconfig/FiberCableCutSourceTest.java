package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberCableCutSourceTest {
    @Test
    void clientOnlySendsCutRequestWhenUsingShearsAndDoesNotRenderTargetHighlight() throws Exception {
        String clientSource = readMainSource("FiberCableCutClient.java");
        String rendererSource = readMainSource("FiberOpticCableRenderer.java");

        assertTrue(clientSource.contains("InputEvent.InteractionKeyMappingTriggered"));
        assertTrue(clientSource.contains("event.isUseItem()"));
        assertTrue(clientSource.contains("Items.SHEARS"));
        assertTrue(clientSource.contains("FiberCableHitTest.closestHit"));
        assertTrue(clientSource.contains("AddonNetwork.sendFiberCableCutRequest"));
        assertFalse(rendererSource.contains("fiberCableTargetIndicator"));
        assertFalse(rendererSource.contains("renderTarget"));
        assertFalse(rendererSource.contains("outline"));
    }

    @Test
    void cutRequestIsServerValidatedBeforeDisconnecting() throws Exception {
        String cutSystemSource = readMainSource("FiberCableCutSystem.java");
        String linkSource = readMainSource("FiberOpticLinkSystem.java");

        assertTrue(cutSystemSource.contains("level.getEntity(message.droneEntityId())"));
        assertTrue(cutSystemSource.contains("instanceof CubedFpvDroneEntity"));
        assertTrue(cutSystemSource.contains("FiberCableRouteState.sessionId(drone)"));
        assertTrue(cutSystemSource.contains("playerHoldsAllowedCutTool(sender)"));
        assertTrue(cutSystemSource.contains("mayPlayerCutCable(level, drone, sender)"));
        assertTrue(cutSystemSource.contains("FiberCableHitTest.closestHit"));
        assertTrue(cutSystemSource.contains("ClipContext.Block.COLLIDER"));
        assertTrue(cutSystemSource.contains("FiberOpticLinkSystem.cutFiberCableByShears"));
        assertTrue(linkSource.contains("DroneWaterDisconnectSystem.forceControlDisconnect(level, drone);"));
        assertTrue(linkSource.contains("clearSession(drone);"));
        assertTrue(linkSource.contains("AddonNetwork.sendFiberCableCutEffect"));
    }

    @Test
    void manualCuttingPreservesZeroEntityCableArchitecture() throws Exception {
        String cutSystemSource = readMainSource("FiberCableCutSystem.java");
        String clientSource = readMainSource("FiberCableCutClient.java");

        assertFalse(cutSystemSource.contains("addFreshEntity"));
        assertFalse(clientSource.contains("addFreshEntity"));
        assertFalse(cutSystemSource.contains("FiberOpticCableSegmentEntity"));
        assertFalse(clientSource.contains("FiberOpticCableSegmentEntity"));
        assertFalse(cutSystemSource.contains("ArmorStand"));
        assertFalse(clientSource.contains("ArmorStand"));
    }

    @Test
    void cutConfigValuesArePresentWithRequestedDefaults() throws Exception {
        String configSource = readMainSource("AddonConfig.java");

        assertTrue(configSource.contains(".define(\"fiberCableCanBeCutByOwner\", true)"));
        assertTrue(configSource.contains(".define(\"fiberCableCanBeCutByOtherPlayers\", true)"));
        assertTrue(configSource.contains(".define(\"fiberCableCutRequiresShears\", true)"));
        assertTrue(configSource.contains(".defineInRange(\"fiberCableCutReach\", 5.0D"));
        assertTrue(configSource.contains(".defineInRange(\"fiberCableInteractionRadius\", 0.12D"));
        assertTrue(configSource.contains(".define(\"fiberCableCutLineOfSightRequired\", true)"));
        assertTrue(configSource.contains(".define(\"fiberCableTargetIndicator\", false)"));
    }

    @Test
    void networkRegistersCutRequestAndAuthoritativeEffectPackets() throws Exception {
        String networkSource = readMainSource("AddonNetwork.java");

        assertTrue(networkSource.contains("FiberCableCutRequestMessage.class"));
        assertTrue(networkSource.contains("NetworkDirection.PLAY_TO_SERVER"));
        assertTrue(networkSource.contains("FiberCableCutEffectMessage.class"));
        assertTrue(networkSource.contains("NetworkDirection.PLAY_TO_CLIENT"));
    }

    private static String readMainSource(String fileName) throws Exception {
        return Files.readString(Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                fileName
        ), StandardCharsets.UTF_8);
    }
}
