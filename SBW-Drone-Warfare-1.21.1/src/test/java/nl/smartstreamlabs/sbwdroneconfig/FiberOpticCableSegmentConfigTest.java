package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableSegmentConfigTest {
    @Test
    void exposesFiberCableSegmentConfigAccessors() throws Exception {
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableVisualThickness"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableVisualAlpha"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableVisualColor"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableMaxRenderDistance"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("maxVisibleFiberCables"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableDebugVisualThickness"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableSegmentSpacing"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableSegmentHealth"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableSegmentHitboxSize"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableCanBeDamagedByPlayers"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableCanBeDamagedByProjectiles"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableCanBeDamagedByExplosions"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableProjectileRaycastBreak"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("fiberCableBreaksWhenAnySegmentDestroyed"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("maxFiberCableSegmentsPerDrone"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("debugFiberCableRender"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("debugFiberCableSegments"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("renderRedDebugCable"));
        assertNotNull(AddonConfig.class.getDeclaredMethod("renderLegacyFiberLine"));
    }

    @Test
    void commonConfigDefinesCableSegmentDefaultsAndDisablesLegacyLineByDefault() throws Exception {
        Path configSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonConfig.java");
        String source = Files.readString(configSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("defineInRange(\"fiberCableVisualThickness\", 0.015D, 0.008D, 0.025D"), "Fiber cable ribbon width should default to 0.015 blocks and stay tightly clamped.");
        assertTrue(source.contains("defineInRange(\"fiberCableVisualAlpha\", 0.72D"), "Fiber cable visual alpha should default to 0.72.");
        assertTrue(source.contains("define(\"fiberCableVisualColor\", \"charcoal\""), "Fiber cable visual color should default to charcoal.");
        assertTrue(source.contains("defineInRange(\"fiberCableMaxRenderDistance\", 192.0D"), "Fiber cable render distance should default to 192 blocks.");
        assertTrue(source.contains("defineInRange(\"maxVisibleFiberCables\", 16"), "Client rendering should cap visible fiber cables.");
        assertTrue(source.contains("defineInRange(\"fiberCableDebugVisualThickness\", 0.025D"), "Fiber cable debug visual thickness should stay inside the same thin ribbon range by default.");
        assertTrue(source.contains("defineInRange(\"fiberCableSegmentSpacing\", 3.0D"), "Fiber cable segment spacing should default to 3.0 blocks.");
        assertTrue(source.contains("defineInRange(\"fiberCableSegmentHealth\", 4.0D"), "Fiber cable segment health should default to 4.0.");
        assertTrue(source.contains("defineInRange(\"fiberCableSegmentHitboxSize\", 0.55D"), "Fiber cable segment hitbox size should default to 0.55.");
        assertTrue(source.contains("define(\"fiberCableCanBeDamagedByPlayers\", true)"), "Players should be able to damage fiber cable segments by default.");
        assertTrue(source.contains("define(\"fiberCableCanBeDamagedByProjectiles\", true)"), "Projectiles should be able to damage fiber cable segments by default.");
        assertTrue(source.contains("define(\"fiberCableCanBeDamagedByExplosions\", true)"), "Explosions should be able to damage fiber cable segments by default.");
        assertTrue(source.contains("define(\"fiberCableProjectileRaycastBreak\", true)"), "Projectile raycast backup breaking should be enabled by default.");
        assertTrue(source.contains("define(\"fiberCableBreaksWhenAnySegmentDestroyed\", true)"), "Any destroyed segment should sever the cable by default.");
        assertTrue(source.contains("defineInRange(\"maxFiberCableSegmentsPerDrone\", 96"), "Fiber cable segments should clamp to 96 per drone by default.");
        assertTrue(source.contains("define(\"debugFiberCableRender\", false)"), "Fiber cable render debug logs should stay off by default.");
        assertTrue(source.contains("define(\"debugFiberCableSegments\", false)"), "Fiber cable segment debug logs should stay off by default.");
        assertTrue(source.contains("define(\"renderRedDebugCable\", false)"), "The red debug cable should be disabled by default.");
        assertTrue(source.contains("define(\"renderLegacyFiberLine\", false)"), "The old legacy client-side line should be disabled by default.");
    }

    @Test
    void clientCableRenderingUsesSingleWorldRibbonHook() throws Exception {
        Path clientSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DroneFiberOpticClient.java");
        String source = Files.readString(clientSource, StandardCharsets.UTF_8);
        Path rendererSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "FiberOpticCableRenderer.java");
        String renderer = Files.readString(rendererSource, StandardCharsets.UTF_8);

        assertTrue(!source.contains("AddonConfig.renderLegacyFiberLine()"),
                "The old client-only cable line should no longer be rendered by the HUD state class.");
        assertTrue(renderer.contains("RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS"),
                "The replacement cable should render from one client-only world render hook.");
        assertTrue(renderer.contains("DroneFiberOpticClient.renderStates()"),
                "The renderer should draw from synced drone fiber state instead of cable segment entities.");
    }
}
