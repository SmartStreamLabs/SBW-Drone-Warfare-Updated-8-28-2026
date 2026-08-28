package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneTrackingSyncSourceTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    @Test
    void chunkMapTrackingMixinIsListedAndResilient() throws IOException {
        String mixinConfig = Files.readString(ROOT.resolve("src/main/resources/sbwdroneconfig.mixins.json"));
        String mixinSource = Files.readString(ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/ChunkMapTrackedEntityMixin.java"));
        String pluginSource = Files.readString(ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwDroneConfigMixinPlugin.java"));

        assertTrue(mixinConfig.contains("\"ChunkMapTrackedEntityMixin\""), "ChunkMap tracking mixin must be enabled in the server mixin config.");
        assertTrue(mixinSource.contains("require = 0"), "ChunkMap tracking mixin must not crash if the target invocation changes.");
        assertTrue(mixinSource.contains("getForcedTrackingRangeBlocks(player, trackedEntity)"), "Mixin must ask the hook for a player-specific forced range.");
        assertFalse(mixinSource.contains("Math.max(effectiveRange, DroneTrackingHooks.getDesiredTrackingRangeBlocks())"), "Mixin should not use one global range for every forced player.");
        assertTrue(pluginSource.contains(".ChunkMapTrackedEntityMixin"), "Mixin plugin should explicitly guard the ChunkMap tracking mixin.");
        assertTrue(pluginSource.contains("net.minecraft.server.level.ChunkMap$TrackedEntity"), "Mixin plugin should verify the TrackedEntity target exists.");
    }

    @Test
    void trackingHookCoversNearbyNonControllerPlayersSafely() throws IOException {
        String hookSource = Files.readString(ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneTrackingHooks.java"));

        assertTrue(hookSource.contains("getForcedTrackingRangeBlocks("), "Hook must expose a range result instead of controller-only true/false.");
        assertTrue(hookSource.contains("trackedEntity.isRemoved()"), "Removed drones must never be force-tracked.");
        assertTrue(hookSource.contains("player.level() != trackedEntity.level()"), "Tracking must never cross dimensions.");
        assertTrue(hookSource.contains("SbwCompat.isUsingLinkedMonitorForDrone(player, trackedEntity)"), "Controller tracking must still use the monitor link.");
        assertTrue(hookSource.contains("getNearbyPlayerTrackingRangeBlocks()"), "Nearby non-controller players need their own visual/audio range.");
        assertTrue(hookSource.contains("isWithinTrackingRange(player, trackedEntity, getNearbyPlayerTrackingRangeBlocks())"), "Nearby players should be force-tracked when the drone enters visual/audio range.");
    }

    @Test
    void activeClientMonitorDisconnectSuppressionMatchesForge1201Behavior() throws IOException {
        String hookSource = Files.readString(ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig/DroneTrackingHooks.java"));
        String monitorMixin = Files.readString(ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/MonitorItemMixin.java"));
        String monitor089Mixin = Files.readString(ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/MonitorItem089Mixin.java"));

        assertFalse(hookSource.contains("shouldSuppressClientMonitorDisconnect(ItemStack stack, Level level, Entity holder, boolean selected) {\r\n        return false;"), "The 1.21.1 monitor hook must not disable the 1.20.1 client monitor suppression.");
        assertTrue(hookSource.contains("!level.isClientSide() || !selected"), "Suppression must only run for the selected client-side monitor stack.");
        assertTrue(hookSource.contains("SbwCompat.isUsingLinkedMonitor(stack)"), "Suppression must only apply to an actively using monitor.");
        assertTrue(hookSource.contains("AddonStackTags.get(stack).contains(SbwCompat.TAG_LINKED_DRONE)"), "Suppression must only apply to a monitor linked to a drone.");
        assertTrue(hookSource.contains("return holder instanceof Player;"), "Suppression must only apply while the holder is a player.");
        assertTrue(monitorMixin.contains("DroneTrackingHooks.shouldSuppressClientMonitorDisconnect(itemstack, world, entity, selected)"), "Modern monitor mixin must call the 1.20.1 suppression hook.");
        assertTrue(monitor089Mixin.contains("DroneTrackingHooks.shouldSuppressClientMonitorDisconnect(itemstack, world, entity, selected)"), "0.8.9 monitor mixin must call the 1.20.1 suppression hook.");
    }
}
