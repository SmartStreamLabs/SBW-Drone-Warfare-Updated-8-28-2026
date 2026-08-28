package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticDebugVisibilitySourceTest {
    @Test
    void droneFiberHudShowsTheTemporaryDebugStateReadout() throws Exception {
        Path clientSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "DroneFiberOpticClient.java"
        );
        String source = Files.readString(clientSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("fiber state active:"),
                "The client HUD should show whether the synced fiber state is active.");
        assertTrue(source.contains("anchorPos:"),
                "The client HUD should expose the synced anchor position while debugging cable visibility.");
        assertTrue(source.contains("droneFoundClient:"),
                "The client HUD should tell us whether the drone entity lookup succeeded on the client.");
        assertTrue(source.contains("rendererCalled:"),
                "The client HUD should show whether the level cable renderer actually ran.");
        assertTrue(source.contains("segmentCount:"),
                "The client HUD should expose the synced cable segment count.");
        assertFalse(source.contains("if (minecraft.player == null || !isActiveDroneView(minecraft.player))"),
                "Client fiber state should not be cleared just because the player is no longer in the active monitor view.");
    }

    @Test
    void cableSegmentRendererStaysHiddenEvenWhenLegacyEntitiesLoad() throws Exception {
        Path rendererSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticCableSegmentRenderer.java"
        );
        String source = Files.readString(rendererSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("Legacy save-compatibility entity: never render a visible cable segment."),
                "Cable segment entities should stay invisible after the client-only ribbon replacement.");
        assertFalse(source.contains("LevelRenderer.renderLineBox"),
                "Cable segment entities should no longer render hitbox markers in the Phase 1 zero-entity path.");
        assertFalse(source.contains("renderSingleBlock"),
                "Cable segment debug visuals should no longer reuse thick block meshes.");
    }
}
